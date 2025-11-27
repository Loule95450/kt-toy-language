package toy

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class MatchTest {

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

    @Test
    fun testMatchTokenization() {
        val source = "match x { case 1 => 2 }"
        val tokens = tokenize(source)
        val tokenTypes = tokens.map { it.type }
        val expected = listOf(
            TokenType.MATCH, TokenType.IDENTIFIER, TokenType.LBRACE,
            TokenType.CASE, TokenType.NUMBER, TokenType.ARROW, TokenType.NUMBER,
            TokenType.RBRACE, TokenType.EOF
        )
        assertEquals(expected, tokenTypes)
    }

    @Test
    fun testMatchParsing() {
        val source = "match 1 { case 1 => 2, case 3 => 4 };"
        val ast = parse(source)
        assertTrue(ast[0] is ExpressionStatement)
        val matchExpr = (ast[0] as ExpressionStatement).expression
        assertTrue(matchExpr is MatchExpression)
        val expr = matchExpr as MatchExpression
        assertEquals(2, expr.cases.size)
        assertEquals(1.0, (expr.cases[0].pattern as Literal).value)
        assertEquals(2.0, (expr.cases[0].body as Literal).value)
    }

    @Test
    fun testMatchEvaluationSuccess() {
        val source = """
        var x = 2;
        match x {
            case 1 => 10,
            case 2 => 20,
            case 3 => 30
        };
        """
        val result = evaluate(source)
        assertEquals(20.0, result)
    }

    @Test
    fun testMatchEvaluationExpressions() {
        val source = """
        match 1 + 1 {
            case 2 => 2 * 2,
            case 3 => 0
        };
        """
        val result = evaluate(source)
        assertEquals(4.0, result)
    }

    @Test
    fun testMatchNoMatchError() {
        val source = """
        match 5 {
            case 1 => 10
        };
        """
        val exception = assertThrows(RuntimeException::class.java) {
            evaluate(source)
        }
        assertTrue(exception.message!!.contains("No match for value"))
    }
}
