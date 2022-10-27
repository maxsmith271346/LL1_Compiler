package ast;

import SSA.Operand;
import SSA.InstructionNumber;
import types.*;
import java.util.HashMap;
import pl434.Symbol;

public class Relation extends Node implements Expression {
    private String relOp;
    private Expression leftRelExpr;
    private Expression rightRelExpr;
    private Type type;
    private InstructionNumber insNumber;

    public Relation(int lineNum, int charPos, String relOp, Expression leftRelExpr, Expression rightRelExpr) {
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

    public String relOp(){
        return relOp;
    }

}