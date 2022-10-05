package ast;

public class ArrayIndex extends Node implements Expression {
    public Expression leftExpr;
    public Expression rightExpr;

    public ArrayIndex(int lineNum, int charPos, Expression leftExpr, Expression rightExpr) {
        super(lineNum, charPos);
        this.leftExpr = leftExpr;
        this.rightExpr = rightExpr;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);

    }

    public Expression leftExpr() {
        return leftExpr;
    }

    public Expression rightExpr() {
        return rightExpr;
    }
}
