use crate::json_value::JsonValue;

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

    fn consume(&mut self) {
        self.pos += 1;
    }

    fn consume_whitespace(&mut self) {
        while self
            .current_token()
            .is_some_and(|token| token.is_whitespace())
        {
            self.consume();
        }
    }

    fn parse_object(self) -> Option<JsonValue> {
        return Some(JsonValue::Object());
    }

    fn parse_value(self) -> Option<JsonValue> {
        return match self.current_token() {
            Some(token) => match token {
                '{' => self.parse_object(),
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
