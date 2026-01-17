use crate::json_value::JsonValue;

pub struct JsonParser {
    pos: usize,
    input: String,
}

impl JsonParser {
    pub fn new(input: String) -> Self {
        JsonParser { pos: 0, input }
    }

    fn currentToken(&self) -> char {
        return self.input.chars().nth(self.pos).unwrap();
    }
}
