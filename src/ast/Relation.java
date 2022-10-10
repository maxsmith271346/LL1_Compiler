package ast;
import types.Type;

public class Relation extends Node implements Expression {
    private String relOp;
    private Expression leftRelExpr;
    private Expression rightRelExpr;

    public Relation(int lineNum, int charPos, String relOp, Expression leftRelExpr, Expression rightRelExpr) {
        super(lineNum, charPos);
        this.relOp = relOp;
        this.leftRelExpr = leftRelExpr;
        this.rightRelExpr = rightRelExpr;
    }

    @Override
    public Type accept(NodeVisitor visitor) {
        return visitor.visit(this);

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