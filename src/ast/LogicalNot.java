package ast;

import SSA.Operand;
import types.*;

public class LogicalNot extends Node implements Expression {
    private Expression expr;
    private Type type;

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

    public Type type(){
        return type;
    }

    public void addType(Type type){
        this.type = type;
    }

    @Override
    public Operand getOperand() {
        // TODO Auto-generated method stub
        return null;
    }
}
