package toy

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class ToyTest {

    fun tokenize(source: String): List<Token> {
        val lexer = Lexer(source)
        return lexer.tokenize()
    }

    fun parse(source: String): List<Statement> {
        val tokens = tokenize(source)
        val parser = Parser(tokens)
        return parser.parse()
    }

    fun evaluate(source: String): Any? {
        val ast = parse(source)
        val interpreter = Interpreter()
        var result: Any? = null
        for (statement in ast) {
            if (statement is ExpressionStatement) {
                result = interpreter.evaluate(statement.expression)
            } else {
                interpreter.execute(statement)
            }
        }
        return result
    }

    fun interpret(source: String): Interpreter {
        val ast = parse(source)
        val interpreter = Interpreter()
        interpreter.interpret(ast)
        return interpreter
    }

    @Test
    fun testLexerTokenize() {
        val source = """3 + 2; 
    var a = 1; """
        val tokens = tokenize(source)
        val expected =
                listOf(
                        Token(TokenType.NUMBER, "3", 1),
                        Token(TokenType.PLUS, "+", 1),
                        Token(TokenType.NUMBER, "2", 1),
                        Token(TokenType.SEMICOLON, ";", 1),
                        Token(TokenType.VAR, "var", 2),
                        Token(TokenType.IDENTIFIER, "a", 2),
                        Token(TokenType.EQUAL, "=", 2),
                        Token(TokenType.NUMBER, "1", 2),
                        Token(TokenType.SEMICOLON, ";", 2),
                        Token(TokenType.EOF, "", 2)
                )
        assertEquals(expected, tokens)
    }

    @Test
    fun testParseTerm() {
        val ast = parse("3 + 2;")
        val expected =
                listOf(
                        ExpressionStatement(
                                Binary(Literal(3.0), Token(TokenType.PLUS, "+", 1), Literal(2.0))
                        )
                )
        assertEquals(expected, ast)
    }

    @Test
    fun testParseFactor() {
        val ast = parse("3 + 2 * 4;")
        val expected =
                listOf(
                        ExpressionStatement(
                                Binary(
                                        Literal(3.0),
                                        Token(TokenType.PLUS, "+", 1),
                                        Binary(
                                                Literal(2.0),
                                                Token(TokenType.STAR, "*", 1),
                                                Literal(4.0)
                                        )
                                )
                        )
                )
        assertEquals(expected, ast)
    }

    @Test
    fun testParseFactorWithParenthesis() {
        val ast = parse("(3 + 2) * 4;")
        val expected =
                listOf(
                        ExpressionStatement(
                                Binary(
                                        Binary(
                                                Literal(3.0),
                                                Token(TokenType.PLUS, "+", 1),
                                                Literal(2.0)
                                        ),
                                        Token(TokenType.STAR, "*", 1),
                                        Literal(4.0)
                                )
                        )
                )
        assertEquals(expected, ast)
    }

    @Test
    fun testParseUnary() {
        val ast = parse("-3 + !2;")
        val expected =
                listOf(
                        ExpressionStatement(
                                Binary(
                                        Unary(Token(TokenType.MINUS, "-", 1), Literal(3.0)),
                                        Token(TokenType.PLUS, "+", 1),
                                        Unary(Token(TokenType.BANG, "!", 1), Literal(2.0))
                                )
                        )
                )
        assertEquals(expected, ast)
    }

    @Test
    fun testParseComparisonOperators() {
        val ast = parse("3 > 2;")
        val expected =
                listOf(
                        ExpressionStatement(
                                Binary(Literal(3.0), Token(TokenType.GREATER, ">", 1), Literal(2.0))
                        )
                )
        assertEquals(expected, ast)
    }

    @Test
    fun testParseEqualityOperators() {
        val ast = parse("3 == 2;")
        val expected =
                listOf(
                        ExpressionStatement(
                                Binary(
                                        Literal(3.0),
                                        Token(TokenType.EQUAL_EQUAL, "==", 1),
                                        Literal(2.0)
                                )
                        )
                )
        assertEquals(expected, ast)
    }

    @Test
    fun testParseComparisonWithEquality() {
        val ast = parse("3 > 2 == 4;")
        val expected =
                listOf(
                        ExpressionStatement(
                                Binary(
                                        Binary(
                                                Literal(3.0),
                                                Token(TokenType.GREATER, ">", 1),
                                                Literal(2.0)
                                        ),
                                        Token(TokenType.EQUAL_EQUAL, "==", 1),
                                        Literal(4.0)
                                )
                        )
                )
        assertEquals(expected, ast)
    }

    @Test
    fun testParseVarDeclaration() {
        val ast = parse("var a = 1;")
        val expected = listOf(VarStatement(Token(TokenType.IDENTIFIER, "a", 1), Literal(1.0)))
        assertEquals(expected, ast)
    }

    @Test
    fun testParseVarAssignment() {
        val ast = parse("var a = 1; a = 2;")
        val expected =
                listOf(
                        VarStatement(Token(TokenType.IDENTIFIER, "a", 1), Literal(1.0)),
                        ExpressionStatement(
                                VariableAssignment(
                                        Token(TokenType.IDENTIFIER, "a", 1),
                                        Literal(2.0)
                                )
                        )
                )
        assertEquals(expected, ast)
    }

    @ParameterizedTest
    @CsvSource(
            "3 + 2 * 4;, 11.0",
            "3 + 2 > 4;, true",
            "3 + 2 == 5;, true",
            "3 + 2 == 4;, false",
            "-2 + 3;, 1.0",
            "var a = 1; a + 1;, 2.0"
    )
    fun testEvaluateExpressions(source: String, expectedStr: String) {
        val result = evaluate(source)
        val expected: Any =
                when {
                    expectedStr == "true" -> true
                    expectedStr == "false" -> false
                    else -> expectedStr.toDouble()
                }
        assertEquals(expected, result)
    }

    @Test
    fun testParseIfStatement() {
        val ast =
                parse(
                        """if (1 == 2) {
                        print 3;
                    } else {
                        print 4;
                    }"""
                )

        val expected =
                listOf(
                        IfStatement(
                                condition =
                                        Binary(
                                                Literal(1.0),
                                                Token(TokenType.EQUAL_EQUAL, "==", 1),
                                                Literal(2.0)
                                        ),
                                thenBranch = BlockStatement(listOf(PrintStatement(Literal(3.0)))),
                                elseBranch = BlockStatement(listOf(PrintStatement(Literal(4.0))))
                        )
                )
        assertEquals(expected, ast)
    }

    @Test
    fun testBlockEnvironmentScope() {
        val source =
                """
        var a = 1;
        var b = 2;
        {
            var a = 2;
            var c = 3;
            b = 10;
        }
        """
        val interpreter = interpret(source)

        assertEquals(1.0, interpreter.environment.get("a"))
        assertEquals(10.0, interpreter.environment.get("b"))

        assertThrows(RuntimeException::class.java) { interpreter.environment.get("c") }
    }

    @Test
    fun testWhileStatement() {
        val source =
                """
        var i = 0;
        while (i < 5) {
            print i;
            i = i + 1;
        }
        """
        val output = StringBuilder()
        val interpreter = Interpreter { output.append("$it\n") }

        val parser = Parser(Lexer(source).tokenize())
        interpreter.interpret(parser.parse())

        assertEquals("0.0\n1.0\n2.0\n3.0\n4.0\n", output.toString())
    }
}
