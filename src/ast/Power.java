package ast;
import types.Type;


public class Power extends Node implements Expression {
    private Expression leftRelExpr;
    private Expression rightRelExpr;

    public Power(int lineNum, int charPos, Expression leftRelExpr, Expression rightRelExpr) {
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
