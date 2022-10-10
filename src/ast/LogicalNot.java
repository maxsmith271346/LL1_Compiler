package ast;
import types.Type;

public class LogicalNot extends Node implements Expression {
    private Expression expr;

    public LogicalNot (int lineNum, int charPos, Expression expr) {
        super(lineNum, charPos);
        this.expr = expr;
    }

    @Override
    public Type accept(NodeVisitor visitor) {
        return visitor.visit(this);

    }

    public Expression expr() {
        return expr;
    }
}
