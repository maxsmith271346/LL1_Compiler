package ast;

public class FunctionBody extends Node {

    DeclarationList varDecl; 
    StatementSequence statSeq;

    protected FunctionBody(int lineNum, int charPos) {
        super(lineNum, charPos);
        //TODO Auto-generated constructor stub
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
    
}
