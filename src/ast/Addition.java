package ast;

public class Addition extends Node implements Expression {
    private String relOp;
    private Expression leftRelExpr;
    private Expression rightRelExpr;

    public Addition(int lineNum, int charPos, String relOp, Expression leftRelExpr, Expression rightRelExpr) {
        super(lineNum, charPos);
        this.relOp = relOp;
        this.leftRelExpr = leftRelExpr;
        this.rightRelExpr = rightRelExpr;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);

    }

    public String operator() {
        return relOp;
    }

    public Expression leftExpression() {
        return leftRelExpr;
    }

    public Expression rightExpression() {
        return rightRelExpr;
    }
}
