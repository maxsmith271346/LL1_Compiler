package ast;

import types.*;

public class IntegerLiteral extends Node implements Expression{
    private String value; 
    private Type type; 

    public IntegerLiteral(int lineNum, int charPos, String value) {
        super(lineNum, charPos);
        this.value = value;
        this.type = new IntType();
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);        
    }

    public String value(){
        return value;
    }

    public Type type(){
        return type;
    }
}
