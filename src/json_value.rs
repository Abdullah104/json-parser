use std::collections;

#[derive(Debug)]
pub enum JsonValue {
    // String(String),
    // Number(u128),
    // Boolean(bool),
    Object(collections::HashMap<String, JsonValue>),
    // Null,
    // Array(Vec<JsonValue>),
}
