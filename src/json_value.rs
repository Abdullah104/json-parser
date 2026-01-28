use std::collections::{self, HashMap};

#[derive(Debug)]
pub enum JsonValue {
    Object(HashMap<String, JsonValue>),
    String(String),
    Boolean(bool),
    Null,
    Number(u128),
    // Array(Vec<JsonValue>),
}
