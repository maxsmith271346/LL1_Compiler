package ast;

import SSA.InstructionNumber;
import SSA.Operand;
import types.*;
import java.util.HashMap;
import pl434.Symbol;

public class LogicalAnd extends Node implements Expression {
    private Expression leftRelExpr;
    private Expression rightRelExpr;
    public Type type; 
    private InstructionNumber insNumber; 

    public LogicalAnd (int lineNum, int charPos, Expression leftRelExpr, Expression rightRelExpr) {
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

    public Type type(){
        return type;
    }

    public void addType(Type type){
        this.type = type;
    }

    @Override
    public Operand getOperand(HashMap<Symbol, Symbol> varMap) {
        return insNumber;
    }

    public void setInsNumber(int insNumber){
        this.insNumber = new InstructionNumber(insNumber);
    }

}
