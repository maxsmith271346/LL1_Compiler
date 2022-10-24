package ast;

import SSA.InstructionNumber;
import SSA.Operand;
import types.*;

public class LogicalNot extends Node implements Expression {
    private Expression expr;
    private Type type;
    private InstructionNumber insNumber; 


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
        return insNumber;
    }

    public void setInsNumber(int insNumber){
        this.insNumber = new InstructionNumber(insNumber);
    }
}
