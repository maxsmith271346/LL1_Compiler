package ast;
import SSA.InstructionNumber;
import SSA.Operand;
import types.*; 

public class LogicalOr extends Node implements Expression {
    private Expression leftRelExpr;
    private Expression rightRelExpr;
    private Type type;
    private InstructionNumber insNumber; 

    public LogicalOr(int lineNum, int charPos, Expression leftRelExpr, Expression rightRelExpr) {
        super(lineNum, charPos);
        this.leftRelExpr = leftRelExpr;
        this.rightRelExpr = rightRelExpr;
        this.type = type;
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
