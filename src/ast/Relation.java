package ast;

import SSA.Operand;
import types.*;

public class Relation extends Node implements Expression {
    private String relOp;
    private Expression leftRelExpr;
    private Expression rightRelExpr;
    private Type type;

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
    public Operand getOperand() {
        // TODO Auto-generated method stub
        return null;
    }

}