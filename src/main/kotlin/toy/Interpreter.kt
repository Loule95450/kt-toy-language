package toy

class Return(val value: Any?) : RuntimeException(null, null, false, false)

class ToyFunction(
    private val interpreter: Interpreter,
    private val declaration: FunctionDeclarationStatement,
    private val closure: Environment
) {
    fun call(arguments: List<Any?>): Any? {
        val env = Environment(closure)
        for (i in declaration.parameters.indices) {
            env.define(declaration.parameters[i].lexeme, arguments[i])
        }

        try {
            interpreter.executeBlock(declaration.body.statements, env)
        } catch (ret: Return) {
            return ret.value
        }
        return null
    }

    override fun toString(): String {
        return "<fn ${declaration.name.lexeme}>"
    }
}

class Interpreter(private val printer: (Any?) -> Unit = ::println) {
    var environment = Environment()

    fun interpret(statements: List<Statement>) {
        for (statement in statements) {
            execute(statement)
        }
    }

    fun execute(stmt: Statement) {
        when (stmt) {
            is ExpressionStatement -> evaluate(stmt.expression)
            is VarStatement -> {
                var value: Any? = null
                if (stmt.initializer != null) {
                    value = evaluate(stmt.initializer)
                }
                environment.define(stmt.name.lexeme, value)
            }
            is FunctionDeclarationStatement -> {
                val function = ToyFunction(this, stmt, Environment(environment))
                environment.define(stmt.name.lexeme, function)
            }
            is PrintStatement -> {
                val value = evaluate(stmt.expression)
                printer(value)
            }
            is IfStatement -> {
                if (isTruthy(evaluate(stmt.condition))) {
                    execute(stmt.thenBranch)
                } else if (stmt.elseBranch != null) {
                    execute(stmt.elseBranch)
                }
            }
            is WhileStatement -> {
                while (isTruthy(evaluate(stmt.condition))) {
                    execute(stmt.body)
                }
            }
            is BlockStatement -> {
                executeBlock(stmt.statements, Environment(environment))
            }
            is ReturnStatement -> {
                var value: Any? = null
                if (stmt.value != null) {
                    value = evaluate(stmt.value)
                }
                throw Return(value)
            }
            is ForStatement -> {
                // Should be handled by parser desugaring
                throw RuntimeException("Unreachable ForStatement")
            }
        }
    }

    fun executeBlock(statements: List<Statement>, env: Environment) {
        val previous = environment
        try {
            environment = env
            for (statement in statements) {
                execute(statement)
            }
        } finally {
            environment = previous
        }
    }

    fun evaluate(expr: Expression): Any? {
        return when (expr) {
            is Literal -> expr.value
            is Binary -> {
                val left = evaluate(expr.left)
                val right = evaluate(expr.right)
                when (expr.operator.type) {
                    TokenType.PLUS -> (left as Double) + (right as Double)
                    TokenType.MINUS -> (left as Double) - (right as Double)
                    TokenType.STAR -> (left as Double) * (right as Double)
                    TokenType.SLASH -> (left as Double) / (right as Double)
                    TokenType.EQUAL_EQUAL -> left == right
                    TokenType.BANG_EQUAL -> left != right
                    TokenType.GREATER -> (left as Double) > (right as Double)
                    TokenType.GREATER_EQUAL -> (left as Double) >= (right as Double)
                    TokenType.LESS -> (left as Double) < (right as Double)
                    TokenType.LESS_EQUAL -> (left as Double) <= (right as Double)
                    else -> throw RuntimeException("Unknown operator: ${expr.operator}")
                }
            }
            is Unary -> {
                val right = evaluate(expr.right)
                when (expr.operator.type) {
                    TokenType.MINUS -> -(right as Double)
                    TokenType.BANG -> !isTruthy(right)
                    else -> throw RuntimeException("Unknown operator: ${expr.operator}")
                }
            }
            is Variable -> environment.get(expr.name.lexeme)
            is VariableAssignment -> {
                val value = evaluate(expr.value)
                environment.assign(expr.name.lexeme, value)
                return value
            }
            is FunctionCall -> {
                val function = evaluate(expr.callee)
                if (function !is ToyFunction) {
                    throw RuntimeException("Unknown function call: ${expr.callee}")
                }
                val args = expr.arguments.map { evaluate(it) }
                function.call(args)
            }
            is MatchExpression -> {
                val subjectValue = evaluate(expr.subject)
                for (matchCase in expr.cases) {
                    val patternValue = evaluate(matchCase.pattern)
                    if (subjectValue == patternValue) {
                        return evaluate(matchCase.body)
                    }
                }
                throw RuntimeException("No match for value: $subjectValue")
            }
        }
    }

    private fun isTruthy(value: Any?): Boolean {
        if (value == null) return false
        if (value is Boolean) return value
        if (value is Number) return value.toDouble() != 0.0
        if (value is String) return value.isNotEmpty()
        return true
    }
}
