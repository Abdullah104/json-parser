pub struct JsonParser {
    pos: i8,
    input: String,
}

impl JsonParser {
    pub fn new(input: String) -> Self {
        JsonParser { pos: 0, input }
    }
}
