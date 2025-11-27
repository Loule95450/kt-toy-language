package toy

class Lexer(val source: String) {
    private var start = 0
    private var current = 0
    private var line = 1
    private val tokens = ArrayList<Token>()

    fun tokenize(): List<Token> {
        while (!isAtEnd()) {
            start = current
            scanToken()
        }

        tokens.add(Token(TokenType.EOF, "", line))
        return tokens
    }

    private fun scanToken() {
        val c = advance()
        when (c) {
            '+' -> addToken(TokenType.PLUS)
            '-' -> addToken(TokenType.MINUS)
            '*' -> addToken(TokenType.STAR)
            '/' -> addToken(TokenType.SLASH)
            '(' -> addToken(TokenType.LPAREN)
            ')' -> addToken(TokenType.RPAREN)
            '{' -> addToken(TokenType.LBRACE)
            '}' -> addToken(TokenType.RBRACE)
            ';' -> addToken(TokenType.SEMICOLON)
            ',' -> addToken(TokenType.COMMA)
            ' ', '\r', '\t' -> {}
            '\n' -> line++
            '=' -> addToken(if (match('=')) TokenType.EQUAL_EQUAL else if (match('>')) TokenType.ARROW else TokenType.EQUAL)
            '<' -> addToken(if (match('=')) TokenType.LESS_EQUAL else TokenType.LESS)
            '>' -> addToken(if (match('=')) TokenType.GREATER_EQUAL else TokenType.GREATER)
            '!' -> addToken(if (match('=')) TokenType.BANG_EQUAL else TokenType.BANG)
            else -> {
                if (c.isDigit()) {
                    number()
                } else if (c.isLetter()) {
                    identifier()
                } else {
                    throw RuntimeException("Unexpected character. character: '$c', line: $line")
                }
            }
        }
    }

    private fun number() {
        while (peek().isDigit()) {
            advance()
        }

        if (peek() == '.' && peekNext().isDigit()) {
            advance() // Consume the "."
            while (peek().isDigit()) {
                advance()
            }
        }

        addToken(TokenType.NUMBER)
    }

    private fun identifier() {
        while (peek().isLetterOrDigit() || peek() == '_') {
            advance()
        }

        val text = source.substring(start, current)
        val type = KEYWORDS[text] ?: TokenType.IDENTIFIER
        addToken(type)
    }

    private fun isAtEnd(): Boolean {
        return current >= source.length
    }

    private fun advance(): Char {
        return source[current++]
    }

    private fun addToken(type: TokenType) {
        val text = source.substring(start, current)
        tokens.add(Token(type, text, line))
    }

    private fun peek(): Char {
        if (isAtEnd()) return '\u0000'
        return source[current]
    }

    private fun peekNext(): Char {
        if (current + 1 >= source.length) return '\u0000'
        return source[current + 1]
    }

    private fun match(expected: Char): Boolean {
        if (isAtEnd()) return false
        if (source[current] != expected) return false
        current++
        return true
    }
}
