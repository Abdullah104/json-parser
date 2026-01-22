use std::collections;

#[derive(Debug)]
pub enum JsonValue {
    Object(collections::HashMap<String, JsonValue>),
    // Array(Vec<JsonValue>),
    // String(String),
    // Boolean(bool),
    // Null,
    // Number(u128),
}
