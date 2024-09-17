package parser.semantic

import ast.AssignExpr
import ast.BooleanExpr
import ast.CallPrintExpr
import ast.ConditionalExpr
import ast.DeclareExpr
import ast.Expression
import ast.ExpressionVisitor
import ast.IdentifierExpr
import ast.NumberExpr
import ast.OperatorExpr
import ast.ReadEnvExpr
import ast.ReadInputExpr
import ast.StringExpr

class SemanticAnalyzer : ExpressionVisitor<SemanticResult, SymbolMap> {
    private val symbolMap: SymbolMap
    private val typeVisitor: TypeVisitor

    constructor(symbolMap: SymbolMap) {
        this.symbolMap = symbolMap
        this.typeVisitor = TypeVisitor()
    }

    constructor() {
        this.symbolMap = SymbolMap()
        this.typeVisitor = TypeVisitor()
    }

    fun analyze(expr: Expression): SemanticResult {
        return expr.accept(this, symbolMap)
    }

    override fun visit(
        expr: AssignExpr,
        context: SymbolMap,
    ): SemanticResult {
        val identifier = expr.left
        val value = expr.value

        if (identifier !is IdentifierExpr) {
            return SemanticResult(false, "Left side of assignment must be an identifier")
        }

        val identifierType = typeVisitor.getType(identifier, context)
        val valueType = typeVisitor.getType(value, context)

        if (identifierType == null) {
            return SemanticResult.failure("Identifier ${identifier.name} not declared")
        } else if (identifierType != valueType && valueType != VariableType.ANY) {
            return SemanticResult.failure("Cannot assign value of type $valueType to identifier of type $identifierType")
        }

        return SemanticResult.success()
    }

    override fun visit(
        expr: DeclareExpr,
        context: SymbolMap,
    ): SemanticResult {
        val value = expr.value
        val variableType = VariableType.fromString(expr.type)

        if (value != null) {
            val valueType = typeVisitor.getType(value, context) ?: return SemanticResult.failure("Cannot infer type of value")

            if (variableType != valueType && valueType != VariableType.ANY) {
                return SemanticResult.failure("Cannot assign value of type $valueType to variable of type $variableType")
            }
        }

        context.addSymbol(expr.name, variableType, expr.mutable)

        return SemanticResult.success()
    }

    override fun visit(
        expr: CallPrintExpr,
        context: SymbolMap,
    ): SemanticResult {
        return analyze(expr.arg)
    }

    override fun visit(
        expr: IdentifierExpr,
        context: SymbolMap,
    ): SemanticResult {
        if (context.getSymbol(expr.name) == null) {
            return SemanticResult.failure("Identifier ${expr.name} not declared")
        }

        return SemanticResult.success()
    }

    override fun visit(
        expr: OperatorExpr,
        context: SymbolMap,
    ): SemanticResult {
        val leftType = typeVisitor.getType(expr.left, context)
        val rightType = typeVisitor.getType(expr.right, context)

        if (leftType == null) {
            return SemanticResult.failure("Cannot infer type of left side of operator")
        }

        if (rightType == null) {
            return SemanticResult.failure("Cannot infer type of right side of operator")
        }

        if (leftType != rightType) {
            return SemanticResult.failure("Cannot apply operator ${expr.op} to types $leftType and $rightType")
        }

        return SemanticResult.success()
    }

    override fun visit(
        expr: NumberExpr,
        context: SymbolMap,
    ): SemanticResult {
        return SemanticResult.success()
    }

    override fun visit(
        expr: StringExpr,
        context: SymbolMap,
    ): SemanticResult {
        return SemanticResult.success()
    }

    override fun visit(
        expr: BooleanExpr,
        context: SymbolMap,
    ): SemanticResult {
        return SemanticResult.success()
    }

    override fun visit(
        expr: ReadEnvExpr,
        context: SymbolMap,
    ): SemanticResult {
        return analyze(expr.name)
    }

    override fun visit(
        expr: ConditionalExpr,
        context: SymbolMap,
    ): SemanticResult {
        val conditionType = typeVisitor.getType(expr.condition, context)

        if (conditionType != VariableType.BOOLEAN) {
            return SemanticResult.failure("Condition must be of type BOOLEAN")
        }

        for (statement in (expr.body + expr.elseBody)) {
            val statementResult = analyze(statement)
            if (!statementResult.success) {
                return statementResult
            }
        }

        return SemanticResult.success()
    }

    override fun visit(
        expr: ReadInputExpr,
        context: SymbolMap,
    ): SemanticResult {
        return analyze(expr.value)
    }
}
