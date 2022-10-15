package ast;

public class FunctionBody extends Node {

    private DeclarationList varDecl; 
    private StatementSequence statSeq;

    public FunctionBody(int lineNum, int charPos, DeclarationList varDecl, StatementSequence statSeq) {
        super(lineNum, charPos);
        this.varDecl = varDecl;
        this.statSeq = statSeq;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    public DeclarationList variables(){
        return varDecl;
    }

    public StatementSequence statements(){
        return statSeq;
    }
    
}
