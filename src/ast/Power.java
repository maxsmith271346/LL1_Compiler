package ast;

import SSA.Operand;
import SSA.InstructionNumber;
import types.*;
import java.util.HashMap;
import pl434.Symbol;
import java.util.HashSet;


public class Power extends Node implements Expression {
    private Expression leftRelExpr;
    private Expression rightRelExpr;
    private Type type;
    private InstructionNumber insNumber; 

    public Power(int lineNum, int charPos, Expression leftRelExpr, Expression rightRelExpr) {
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
    public Operand getOperand(HashMap<Symbol, HashSet<Symbol>>  varMap) {
        return insNumber;
    }

    public void setInsNumber(InstructionNumber insNumber){
        this.insNumber = insNumber;
    }
}
