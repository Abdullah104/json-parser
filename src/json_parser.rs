use std::collections::HashMap;

use regex::Regex;

use crate::{
    json_value::JsonValue,
    pair::Pair,
    tokens::{EscapeToken, NumberToken, Token},
};

#[derive(Clone)]
pub struct JsonParser {
    pos: usize,
    input: String,
    max_depth: Option<i8>,
}

impl JsonParser {
    pub fn new(input: String, depth: Option<i8>) -> Self {
        JsonParser {
            pos: 0,
            input,
            max_depth: depth,
        }
    }

    fn current_token(&self) -> Option<char> {
        return self.input.chars().nth(self.pos);
    }

    fn consume(&mut self, token: Option<char>) -> bool {
        if token.is_some() && token != self.current_token() {
            return false;
        }

        self.pos += 1;

        return true;
    }

    fn consume_empty_spaces(&mut self) -> bool {
        return match self.current_token() {
            Some(token) => {
                if token.is_whitespace() || token == Token::NEW_LINE {
                    if !self.consume(None) {
                        return false;
                    }

                    return self.consume_empty_spaces();
                } else {
                    return true;
                }
            }
            None => false,
        };
    }

    fn is_control_code(&self) -> Option<bool> {
        match self.current_token() {
            Some(token) => Some(
                Regex::new(
                    r"[\u0000-\u001F\u007F-\u009F\u061C\u200E\u200F\u202A-\u202E\u2066-\u2069]",
                )
                .unwrap()
                .is_match(token.to_string().as_str()),
            ),
            None => None,
        }
    }

    fn parse_string(&mut self) -> Option<String> {
        if !self.consume_empty_spaces() {
            return None;
        }

        if !self.consume(Some(Token::QUOTE)) {
            return None;
        }

        let mut string = String::new();

        loop {
            match self.current_token() {
                Some(token) => {
                    if token == Token::QUOTE {
                        self.consume(Some(Token::QUOTE));

                        break;
                    }

                    let mut pushed = false;

                    if self.is_control_code().is_none_or(|is_control| is_control) {
                        return None;
                    }

                    if token == Token::ESCAPE {
                        self.consume(None);

                        match self.current_token() {
                            Some(t) => match t {
                                EscapeToken::BACK_SLASH => {
                                    string.push(t);
                                    self.consume(None);

                                    pushed = true;
                                }
                                EscapeToken::LINE_FEED => {
                                    string.push(t);
                                    self.consume(None);

                                    pushed = true;
                                }
                                _ => return None,
                            },
                            None => return None,
                        }
                    }

                    if !pushed {
                        string.push(token);
                    }

                    if !self.consume(None) {
                        return None;
                    }
                }
                None => return None,
            }
        }

        return Some(string);
    }

    fn parse_pattern(&mut self, pattern: String) -> bool {
        for character in pattern.chars() {
            if !self.consume(Some(character)) {
                return false;
            }
        }

        true
    }

    fn parse_boolean(&mut self, boolean: bool) -> bool {
        self.parse_pattern(boolean.to_string())
    }

    fn parse_true(&mut self) -> Option<JsonValue> {
        self.parse_boolean(true)
            .then_some(JsonValue::Boolean(true))
            .or_else(|| None)
    }

    fn parse_false(&mut self) -> Option<JsonValue> {
        self.parse_boolean(false)
            .then_some(JsonValue::Boolean(false))
            .or_else(|| None)
    }

    fn parse_null(&mut self) -> Option<JsonValue> {
        self.parse_pattern("null".to_string())
            .then_some(JsonValue::Null)
            .or_else(|| None)
    }

    fn is_token_valid_numeric(&self, token: char) -> bool {
        Regex::new(r"[0-9]")
            .unwrap()
            .is_match(token.to_string().as_str())
    }

    fn parse_number(&mut self) -> Option<JsonValue> {
        let mut number_string = String::new();

        loop {
            match self.current_token() {
                Some(token) => {
                    if self.is_token_valid_numeric(token) || token == NumberToken::DOT {
                        number_string.push(token);
                        self.consume(Some(token));
                    } else {
                        self.consume_empty_spaces();

                        match self.current_token() {
                            Some(token) => {
                                if Regex::new(r"[,}\]]")
                                    .unwrap()
                                    .is_match(token.to_string().as_str())
                                {
                                    break;
                                } else {
                                    return None;
                                }
                            }
                            None => return None,
                        }
                    }
                }
                None => return None,
            }
        }

        match number_string.parse::<u128>() {
            Ok(num) => {
                if number_string.starts_with("0")
                    && !Regex::new(r"[eE]")
                        .unwrap()
                        .is_match(number_string.as_str())
                {
                    return None;
                }

                Some(JsonValue::Number(num))
            }
            Err(_) => None,
        }
    }

    fn depth_valid(&self, depth: i8) -> bool {
        return self.max_depth.is_none_or(|max_depth| depth <= max_depth);
    }

    fn parse_array(&mut self, depth: i8) -> Option<JsonValue> {
        if !self.depth_valid(depth) {
            return None;
        }

        if !self.consume(Some(Token::BEGIN_ARRAY)) {
            return None;
        }

        let mut array = Vec::new();

        self.consume_empty_spaces();
        let mut more_items = self.current_token() != Some(Token::END_ARRAY);

        loop {
            if more_items {
                match self.parse_value(depth) {
                    Some(value) => {
                        array.push(value);
                        self.consume_empty_spaces();

                        more_items = self.current_token() == Some(Token::COMMA);
                        if more_items {
                            self.consume(Some(Token::COMMA));

                            if !self.consume_empty_spaces() {
                                return None;
                            }
                        } else {
                            break;
                        }
                    }
                    None => {
                        return None;
                    }
                }
            }

            if !more_items && self.current_token() == Some(Token::END_ARRAY) {
                break;
            }
        }

        if !self.consume(Some(Token::END_ARRAY)) {
            return None;
        }

        return Some(JsonValue::Array(array));
    }

    fn parse_value(&mut self, depth: i8) -> Option<JsonValue> {
        return match self.current_token() {
            Some(token) => {
                if token == Token::QUOTE {
                    return self.parse_string().map(JsonValue::String);
                }

                if token == Token::BEGIN_TRUE {
                    return self.parse_true();
                }

                if token == Token::BEGIN_FALSE {
                    return self.parse_false();
                }

                if token == Token::BEGIN_NULL {
                    return self.parse_null();
                }

                if self.is_token_valid_numeric(token) {
                    return self.parse_number();
                }

                if token == Token::BEGIN_OBJECT {
                    return self.parse_object(depth + 1);
                }

                if token == Token::BEGIN_ARRAY {
                    return self.parse_array(depth + 1);
                }

                return None;
            }
            None => None,
        };
    }

    fn parse_pair(&mut self, depth: i8) -> Option<Pair> {
        if !self.consume_empty_spaces() {
            return None;
        }

        let key = self.parse_string();
        if key.is_none() {
            return None;
        }

        if !self.consume(Some(Token::COLON)) {
            return None;
        }

        if !self.consume_empty_spaces() {
            return None;
        }

        let value = self.parse_value(depth);
        if value.is_none() {
            return None;
        }

        return Some(Pair {
            key: key.unwrap(),
            value: value.unwrap(),
        });
    }

    fn parse_object(&mut self, depth: i8) -> Option<JsonValue> {
        if !self.depth_valid(depth) {
            return None;
        }

        if !self.consume(Some(Token::BEGIN_OBJECT)) {
            return None;
        }

        let mut map = HashMap::new();

        self.consume_empty_spaces();
        let mut more_pairs = self.current_token() != Some(Token::END_OBJECT);

        loop {
            if more_pairs {
                match self.parse_pair(depth) {
                    Some(pair) => {
                        map.insert(pair.key, pair.value);

                        self.consume_empty_spaces();
                        more_pairs = self.current_token() == Some(Token::COMMA);

                        if more_pairs {
                            self.consume(Some(Token::COMMA));
                        } else {
                            break;
                        }
                    }
                    None => return None,
                }
            }

            if !more_pairs && self.current_token() == Some(Token::END_OBJECT) {
                break;
            }
        }

        if !self.consume(Some(Token::END_OBJECT)) {
            return None;
        }

        return Some(JsonValue::Object(map));
    }

    pub fn parse(mut self) -> Option<JsonValue> {
        return match self.current_token() {
            Some(_) => self.parse_value(0),
            None => None,
        };
    }
}
