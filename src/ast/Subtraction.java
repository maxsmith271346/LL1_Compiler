package ast;

import types.*;

public class Subtraction extends Node implements Expression {
    private Expression leftRelExpr;
    private Expression rightRelExpr;
    private Type type;

    public Subtraction(int lineNum, int charPos, Expression leftRelExpr, Expression rightRelExpr) {
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
}
