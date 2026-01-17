#[derive(Debug)]
pub enum JsonValue {
    String(String),
    Number(u128),
    Boolean(bool),
    Object(std::collections::HashMap<String, JsonValue>),
    Null,
    Array(Vec<JsonValue>),
}
