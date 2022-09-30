package ast;

import java.util.List;

public class StatementSequence extends Node {

    public List<Statement> statSeq; 

    protected StatementSequence(int lineNum, int charPos) {
        super(lineNum, charPos);
        //TODO Auto-generated constructor stub
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
    
}