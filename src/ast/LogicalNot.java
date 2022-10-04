package ast;

public class LogicalNot extends Node implements Expression {
    private Expression expr;

    public LogicalNot (int lineNum, int charPos, Expression expr) {
        super(lineNum, charPos);
        this.expr = expr;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);

    }

    public Expression expr() {
        return expr;
    }
}
