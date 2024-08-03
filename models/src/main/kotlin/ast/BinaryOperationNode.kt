package org.example.ast

class BinaryOperationNode(val left: ASTNode, val right: ASTNode, val start: Int, val end: Int, val operator: String) : ASTNode{
    override fun getType(): String {
        return "BinaryOperation"
    }

    override fun accept(visitor: ASTVisitor) {
        visitor.visit(this)
    }
}