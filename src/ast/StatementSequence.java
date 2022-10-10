package ast;

import java.util.ArrayList;
import java.util.List;
import types.Type;


public class StatementSequence extends Node {

    public List<Statement> statSeq; 

    //should this be protected?
    public StatementSequence(int lineNum, int charPos) {
        super(lineNum, charPos);
        statSeq = new ArrayList<Statement>();
    }

    @Override
    public Type accept(NodeVisitor visitor) {
        return visitor.visit(this);
    }
    
}
