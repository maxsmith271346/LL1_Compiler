package ast;

public class Division extends Node implements Expression {
    private Expression leftRelExpr;
    private Expression rightRelExpr;

    public Division(int lineNum, int charPos, Expression leftRelExpr, Expression rightRelExpr) {
        super(lineNum, charPos);
        this.leftRelExpr = leftRelExpr;
        this.rightRelExpr = rightRelExpr;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);

    }

    public Expression leftExpression() {
        return leftRelExpr;
    }

    public Expression rightExpression() {
        return rightRelExpr;
    }
}
