use std::collections::HashMap;

use crate::{json_value::JsonValue, pair::Pair, tokens::Token};

pub struct JsonParser {
    pos: usize,
    input: String,
}

impl JsonParser {
    pub fn new(input: String) -> Self {
        JsonParser { pos: 0, input }
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

                    string.push(token);

                    if !self.consume(None) {
                        return None;
                    }
                }
                None => return None,
            }
        }

        return Some(string);
    }

    fn parse_pair(&mut self) -> Option<Pair> {
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

        let value = self.parse_string();
        if value.is_none() {
            return None;
        }

        return Some(Pair {
            key: key.unwrap(),
            value: JsonValue::String(value.unwrap()),
        });
    }

    fn parse_object(mut self) -> Option<JsonValue> {
        if !self.consume(Some(Token::BEGIN_OBJECT)) {
            return None;
        }

        let mut map = HashMap::new();

        self.consume_empty_spaces();
        let mut more_pairs = self.current_token() != Some(Token::END_OBJECT);

        loop {
            if more_pairs {
                match self.parse_pair() {
                    Some(pair) => {
                        map.insert(pair.key, pair.value);
                        self.consume_empty_spaces();

                        more_pairs = self.current_token() == Some(Token::COMMA);
                        self.consume(Some(Token::COMMA));

                        if !more_pairs {
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

        return Some(JsonValue::Object(map));
    }

    pub fn parse(self) -> Option<JsonValue> {
        return match self.current_token() {
            Some(_) => self.parse_object(),
            None => None,
        };
    }
}
