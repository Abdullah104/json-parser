use std::collections::HashMap;

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

    fn parse_object(self) -> Option<JsonValue> {
        return Some(JsonValue::Object(HashMap::new()));
    }

    pub fn parse(self) -> Option<JsonValue> {
        return match self.current_token() {
            Some(_) => self.parse_object(),
            None => None,
        };
    }
}
