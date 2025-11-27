package toy

class Parser(private val tokens: List<Token>) {
    private var current = 0

    fun parse(): List<Statement> {
        val statements = ArrayList<Statement>()
        while (!isAtEnd()) {
            statements.add(parseDeclaration())
        }
        return statements
    }

    private fun parseDeclaration(): Statement {
        if (match(TokenType.VAR)) {
            return parseVarDeclaration()
        }
        if (match(TokenType.FN)) {
            return parseFunctionDeclaration()
        }
        return parseStatement()
    }

    private fun parseVarDeclaration(): Statement {
        val name = consume(TokenType.IDENTIFIER, "Expect variable name.")
        var initializer: Expression? = null
        if (match(TokenType.EQUAL)) {
            initializer = parseExpression()
        }
        consume(TokenType.SEMICOLON, "Expect ';' after variable declaration.")
        return VarStatement(name, initializer)
    }

    private fun parseFunctionDeclaration(): Statement {
        val name = consume(TokenType.IDENTIFIER, "Expect function name.")
        consume(TokenType.LPAREN, "Expect '(' after function name.")
        
        val parameters = ArrayList<Token>()
        if (!check(TokenType.RPAREN)) {
            parameters.add(consume(TokenType.IDENTIFIER, "Expect parameter name."))
            while (match(TokenType.COMMA)) {
                parameters.add(consume(TokenType.IDENTIFIER, "Expect parameter name."))
            }
        }
        
        consume(TokenType.RPAREN, "Expect ')' after parameters.")
        consume(TokenType.LBRACE, "Expect '{' before function body.")
        
        val body = ArrayList<Statement>()
        while (!check(TokenType.RBRACE)) {
            body.add(parseDeclaration())
        }
        
        consume(TokenType.RBRACE, "Expect '}' after function body.")
        return FunctionDeclarationStatement(name, parameters, BlockStatement(body))
    }

    private fun parseStatement(): Statement {
        if (match(TokenType.PRINT)) return parsePrintStatement()
        if (match(TokenType.IF)) return parseIfStatement()
        if (match(TokenType.WHILE)) return parseWhileStatement()
        if (match(TokenType.FOR)) return parseForStatement()
        if (match(TokenType.LBRACE)) return parseBlockStatement()
        if (match(TokenType.RETURN)) return parseReturnStatement()
        
        return parseExpressionStatement()
    }

    private fun parsePrintStatement(): Statement {
        val expr = parseExpression()
        consume(TokenType.SEMICOLON, "Expect ';' after expression.")
        return PrintStatement(expr)
    }

    private fun parseIfStatement(): Statement {
        consume(TokenType.LPAREN, "Expect '(' after 'if'")
        val condition = parseExpression()
        consume(TokenType.RPAREN, "Expect ')' after 'if'")
        
        val thenBranch = parseStatement()
        var elseBranch: Statement? = null
        if (match(TokenType.ELSE)) {
            elseBranch = parseStatement()
        }
        
        return IfStatement(condition, thenBranch, elseBranch)
    }

    private fun parseWhileStatement(): Statement {
        consume(TokenType.LPAREN, "Expect '(' after 'while'")
        val condition = parseExpression()
        consume(TokenType.RPAREN, "Expect ')' after 'while'")
        
        val body = parseStatement()
        return WhileStatement(condition, body)
    }

    private fun parseForStatement(): Statement {
        consume(TokenType.LPAREN, "Expect '(' after 'for'.")
        
        val initializer: Statement?
        if (match(TokenType.SEMICOLON)) {
            initializer = null
        } else if (match(TokenType.VAR)) {
            initializer = parseVarDeclaration()
        } else {
            initializer = parseExpressionStatement()
        }
        
        var condition: Expression? = null
        if (!check(TokenType.SEMICOLON)) {
            condition = parseExpression()
        }
        consume(TokenType.SEMICOLON, "Expect ';' after loop condition.")
        
        var increment: Expression? = null
        if (!check(TokenType.RPAREN)) {
            increment = parseExpression()
        }
        consume(TokenType.RPAREN, "Expect ')' after for clauses.")
        
        var body = parseStatement()
        
        if (increment != null) {
            body = BlockStatement(listOf(body, ExpressionStatement(increment)))
        }
        
        val cond = condition ?: Literal(true)
        body = WhileStatement(cond, body)
        
        if (initializer != null) {
            body = BlockStatement(listOf(initializer, body))
        }
        
        return body
    }

    private fun parseReturnStatement(): Statement {
        val keyword = previous()
        var value: Expression? = null
        if (!check(TokenType.SEMICOLON)) {
            value = parseExpression()
        }
        
        consume(TokenType.SEMICOLON, "Expect ';' after return value.")
        return ReturnStatement(keyword, value)
    }

    private fun parseBlockStatement(): BlockStatement {
        val statements = ArrayList<Statement>()
        while (!check(TokenType.RBRACE) && !isAtEnd()) {
            statements.add(parseDeclaration())
        }
        consume(TokenType.RBRACE, "Expect '}' after block.")
        return BlockStatement(statements)
    }

    private fun parseExpressionStatement(): Statement {
        val expr = parseExpression()
        consume(TokenType.SEMICOLON, "Expect ';' after expression.")
        return ExpressionStatement(expr)
    }

    private fun parseExpression(): Expression {
        return parseAssignment()
    }

    private fun parseAssignment(): Expression {
        val expr = parseEquality()
        
        if (match(TokenType.EQUAL)) {
            val equals = previous()
            val value = parseAssignment()
            
            if (expr is Variable) {
                return VariableAssignment(expr.name, value)
            }
            
            throw RuntimeException("Invalid assignment target. token: ${equals.lexeme}")
        }
        
        return expr
    }

    private fun parseEquality(): Expression {
        return binaryLeft(::parseComparison, TokenType.EQUAL_EQUAL, TokenType.BANG_EQUAL)
    }

    private fun parseComparison(): Expression {
        return binaryLeft(::parseTerm, TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)
    }

    private fun parseTerm(): Expression {
        return binaryLeft(::parseFactor, TokenType.PLUS, TokenType.MINUS)
    }

    private fun parseFactor(): Expression {
        return binaryLeft(::parseUnary, TokenType.STAR, TokenType.SLASH)
    }

    private fun parseUnary(): Expression {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            val operator = previous()
            val right = parseUnary()
            return Unary(operator, right)
        }
        return parsePrimary()
    }

    private fun parsePrimary(): Expression {
        if (match(TokenType.NUMBER)) {
            return Literal(previous().lexeme.toDouble())
        }
        if (match(TokenType.IDENTIFIER)) {
            return Variable(previous())
        }
        if (match(TokenType.LPAREN)) {
            val expr = parseExpression()
            consume(TokenType.RPAREN, "Expected ')' after expression.")
            return expr
        }
        if (match(TokenType.MATCH)) {
            return parseMatchExpression()
        }
        
        throw RuntimeException("Unexpected token. token: ${peek().lexeme}, line: ${peek().line}")
    }

    private fun parseMatchExpression(): Expression {
        val subject = parseExpression()
        consume(TokenType.LBRACE, "Expect '{' after match subject.")
        
        val cases = ArrayList<MatchCase>()
        while (!check(TokenType.RBRACE) && !isAtEnd()) {
            consume(TokenType.CASE, "Expect 'case' before match pattern.")
            val pattern = parseExpression()
            consume(TokenType.ARROW, "Expect '=>' after match pattern.")
            val body = parseExpression()
            cases.add(MatchCase(pattern, body))
            
            if (match(TokenType.COMMA)) {
                // consume comma
            }
        }
        consume(TokenType.RBRACE, "Expect '}' after match cases.")
        return MatchExpression(subject, cases)
    }

    private fun binaryLeft(operandFn: () -> Expression, vararg operators: TokenType): Expression {
        var left = operandFn()
        while (match(*operators)) {
            val operator = previous()
            val right = operandFn()
            left = Binary(left, operator, right)
        }
        return left
    }

    private fun match(vararg types: TokenType): Boolean {
        for (type in types) {
            if (check(type)) {
                advance()
                return true
            }
        }
        return false
    }

    private fun check(type: TokenType): Boolean {
        if (isAtEnd()) return false
        return peek().type == type
    }

    private fun advance(): Token {
        if (!isAtEnd()) current++
        return previous()
    }

    private fun isAtEnd(): Boolean {
        return peek().type == TokenType.EOF
    }

    private fun peek(): Token {
        return tokens[current]
    }

    private fun previous(): Token {
        return tokens[current - 1]
    }

    private fun consume(type: TokenType, message: String): Token {
        if (check(type)) return advance()
        throw RuntimeException("$message at line ${peek().line}")
    }
}
