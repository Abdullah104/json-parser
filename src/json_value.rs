use std::collections::{self, HashMap};

#[derive(Debug)]
pub enum JsonValue {
    Object(HashMap<String, JsonValue>),
    String(String),
    // Array(Vec<JsonValue>),
    // Boolean(bool),
    // Null,
    // Number(u128),
}
