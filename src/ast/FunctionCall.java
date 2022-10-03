package ast;

public class FunctionCall extends Node implements Statement{

    public FunctionCall(int lineNum, int charPos) {
        super(lineNum, charPos);
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
    
}
