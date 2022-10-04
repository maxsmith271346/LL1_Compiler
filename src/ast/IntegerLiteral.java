package ast;

public class IntegerLiteral extends Node implements Expression{
    private String value; 
    public IntegerLiteral(int lineNum, int charPos, String value) {
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
