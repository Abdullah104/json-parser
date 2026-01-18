use crate::{json_value::JsonValue, pair, tokens::Token};

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

    fn consume(&mut self, expected: Option<char>) {
        let current_token = self.current_token();

        if expected.is_some() && current_token != expected {
            println!(
                "Expected {:?} but found {:?} at position {}",
                expected, current_token, self.pos
            );

            std::process::exit(1)
        }

        self.pos += 1;

        while current_token.is_some()
            && regex::Regex::new(r"\s|\t|\r")
                .unwrap()
                .is_match(&current_token.unwrap().to_string())
        {
            self.pos += 1
        }
    }

    fn consume_whitespace(&mut self) {
        while self
            .current_token()
            .is_some_and(|token| token.is_whitespace())
        {
            self.consume(None);
        }
    }

    fn parse_string(self) -> String {
        let mut string = "";

        self.consume(Some(Token::QUOTE));
    }

    fn parse_pair(&self) -> pair::Pair {
        let key = self.parse_string();
    }

    fn parse_object(&mut self) -> Option<JsonValue> {
        let object = JsonValue::Object(std::collections::HashMap::new());

        self.consume(Some(Token::BEGIN_OBJECT));

        // used to check if there are more pairs.
        // while loop will not end in this case: "{,}"
        let more_pairs: Option<bool> = None;

        while self.current_token() != Some(Token::END_OBJECT) || more_pairs.unwrap_or(false) {
            let pair = self.parse_pair();
        }

        return Some(object);
    }

    fn parse_value(mut self) -> Option<JsonValue> {
        return match self.current_token() {
            Some(token) => match token {
                Token::BEGIN_OBJECT => self.parse_object(),
                _ => None,
            },
            None => None,
        };
    }

    pub fn parse(mut self) -> Option<JsonValue> {
        self.consume_whitespace();

        let value = self.parse_value();

        return value;
    }
}
