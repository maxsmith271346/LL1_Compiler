package ast;
import types.Type;


public class BoolLiteral extends Node implements Expression{
    private String value; 
    public BoolLiteral(int lineNum, int charPos, String value) {
        super(lineNum, charPos);
        this.value = value;
    }

    @Override
    public Type accept(NodeVisitor visitor) {
        return visitor.visit(this);        
    }

    public String value(){
        return value;
    }
}