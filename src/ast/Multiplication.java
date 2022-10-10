package ast;
import types.Type;


public class Multiplication extends Node implements Expression {
    private Expression leftRelExpr;
    private Expression rightRelExpr;

    public Multiplication (int lineNum, int charPos, Expression leftRelExpr, Expression rightRelExpr) {
        super(lineNum, charPos);
        this.leftRelExpr = leftRelExpr;
        this.rightRelExpr = rightRelExpr;
    }

    @Override
    public Type accept(NodeVisitor visitor) {
        return visitor.visit(this);

    }

    public Expression leftExpression() {
        return leftRelExpr;
    }

    public Expression rightExpression() {
        return rightRelExpr;
    }
}
