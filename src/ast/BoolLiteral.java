package ast;

public class BoolLiteral extends Node implements Expression{
    private String value; 
    public BoolLiteral(int lineNum, int charPos, String value) {
        super(lineNum, charPos);
        this.value = value;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);        
    }

    public String value(){
        return value;
    }
}