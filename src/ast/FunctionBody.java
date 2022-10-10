package ast;
import types.Type;


public class FunctionBody extends Node {

    private DeclarationList varDecl; 
    private StatementSequence statSeq;

    public FunctionBody(int lineNum, int charPos, DeclarationList varDecl, StatementSequence statSeq) {
        super(lineNum, charPos);
        this.varDecl = varDecl;
        this.statSeq = statSeq;
    }

    @Override
    public Type accept(NodeVisitor visitor) {
        return visitor.visit(this);
    }

    public DeclarationList variables(){
        return varDecl;
    }

    public StatementSequence statements(){
        return statSeq;
    }
    
}
