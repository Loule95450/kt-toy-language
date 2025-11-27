package toy

sealed interface ASTNode

//////////////////////////////////////////////////////////////////////////////
// Expressions
//////////////////////////////////////////////////////////////////////////////

sealed interface Expression : ASTNode

data class Literal(val value: Any?) : Expression

data class Binary(
    val left: Expression,
    val operator: Token,
    val right: Expression
) : Expression

data class Unary(
    val operator: Token,
    val right: Expression
) : Expression

data class Variable(val name: Token) : Expression

data class VariableAssignment(
    val name: Token,
    val value: Expression
) : Expression

data class FunctionCall(
    val callee: Expression,
    val arguments: List<Expression>
) : Expression

data class MatchCase(
    val pattern: Expression,
    val body: Expression
) : ASTNode

data class MatchExpression(
    val subject: Expression,
    val cases: List<MatchCase>
) : Expression

//////////////////////////////////////////////////////////////////////////////
// Statements
//////////////////////////////////////////////////////////////////////////////

sealed interface Statement : ASTNode

data class ExpressionStatement(val expression: Expression) : Statement

data class VarStatement(
    val name: Token,
    val initializer: Expression?
) : Statement

data class PrintStatement(val expression: Expression) : Statement

data class IfStatement(
    val condition: Expression,
    val thenBranch: Statement,
    val elseBranch: Statement?
) : Statement

data class WhileStatement(
    val condition: Expression,
    val body: Statement
) : Statement

data class ForStatement(
    val initializer: Statement,
    val condition: Expression,
    val body: Statement
) : Statement

data class BlockStatement(val statements: List<Statement>) : Statement

data class FunctionDeclarationStatement(
    val name: Token,
    val parameters: List<Token>,
    val body: BlockStatement
) : Statement

data class ReturnStatement(
    val keyword: Token,
    val value: Expression?
) : Statement
