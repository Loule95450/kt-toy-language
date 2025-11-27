package toy

enum class TokenType {
    LPAREN, RPAREN, LBRACE, RBRACE,
    SEMICOLON, COMMA, MINUS, PLUS,
    SLASH, STAR,

    BANG, BANG_EQUAL,
    EQUAL, EQUAL_EQUAL,
    GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL,

    IDENTIFIER, NUMBER, STRING,

    AND, ELSE, FALSE, FN, FOR, IF, NULL, OR,
    PRINT, RETURN, TRUE, VAR, WHILE,
    MATCH, CASE, ARROW,

    EOF
}

data class Token(
    val type: TokenType,
    val lexeme: String,
    val line: Int
) {
    override fun toString(): String {
        return "$type $lexeme $line"
    }
}

val KEYWORDS = mapOf(
    "and" to TokenType.AND,
    "else" to TokenType.ELSE,
    "false" to TokenType.FALSE,
    "fn" to TokenType.FN,
    "for" to TokenType.FOR,
    "if" to TokenType.IF,
    "null" to TokenType.NULL,
    "or" to TokenType.OR,
    "print" to TokenType.PRINT,
    "return" to TokenType.RETURN,
    "true" to TokenType.TRUE,
    "var" to TokenType.VAR,
    "while" to TokenType.WHILE,
    "match" to TokenType.MATCH,
    "case" to TokenType.CASE
)
