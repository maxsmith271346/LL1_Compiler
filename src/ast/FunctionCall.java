package ast;

public class FunctionCall extends Node implements Statement, Expression{

    public FunctionCall(int lineNum, int charPos) {
        super(lineNum, charPos);
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
    
}
