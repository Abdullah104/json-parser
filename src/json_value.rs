use std::collections::HashMap;

#[derive(Debug)]
#[allow(dead_code)]
pub enum JsonValue {
    Object(HashMap<String, JsonValue>),
    String(String),
    Boolean(bool),
    Null,
    Number(u128),
    Array(Vec<JsonValue>),
}
