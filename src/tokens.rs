pub struct Token;
impl Token {
    pub const BEGIN_OBJECT: char = '{';
    pub const END_OBJECT: char = '}';
    pub const QUOTE: char = '"';
    pub const ESCAPE: char = '\\';
    pub const COLON: char = ':';
    pub const COMMA: char = ',';
    pub const BEGIN_ARRAY: char = '[';
    pub const END_ARRAY: char = ']';
    pub const BEGIN_TRUE: char = 't';
    pub const BEGIN_FALSE: char = 'f';
    pub const BEGIN_NULL: char = 'n';
}

pub struct EscapeToken;
impl EscapeToken {
    pub const QUOTE: char = '"';
    pub const BACK_SLASH: char = '\\';
    pub const FORWARD_SLASH: char = '/';
    pub const BACKSPACE: char = 'b';
    pub const FORM_FEED: char = 'f';
    pub const LINE_FEED: char = 'n';
    pub const CAR_RETURN: char = 'r';
    pub const HORIZONTAL_TAB: char = 'r';
    pub const HEX: char = 'u';
}

pub struct NumberToken;
impl NumberToken {
    pub const ZERO: char = '0';
    pub const ONE: char = '1';
    pub const TWO: char = '2';
    pub const THREE: char = '3';
    pub const FOUR: char = '4';
    pub const FIVE: char = '5';
    pub const SIX: char = '6';
    pub const SEVEN: char = '7';
    pub const EIGHT: char = '8';
    pub const NINE: char = '9';
    pub const PLUS: char = '+';
    pub const MINUS: char = '-';
    pub const DOT: char = '.';
    pub const SMALL_EXPONENT: char = 'e';
    pub const CAPITAL_EXPONENT: char = 'E';
}
