package ast;

public class IfStatement extends Node implements Statement {
    private Relation relation;
    private StatementSequence thenStatSeq;
    private StatementSequence elseStatSeq;

    public IfStatement(int lineNum, int charPos, Relation relation, StatementSequence thenStatSeq, StatementSequence elseStatSeq) {
        super(lineNum, charPos);
        this.relation = relation;
        this.thenStatSeq = thenStatSeq;
        this.elseStatSeq = elseStatSeq;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    public Relation condition() {
        return relation;
    }
    
    public StatementSequence thenStatementSeq() {
        return thenStatSeq; 
    }

    public StatementSequence elseStatementSeq() {
        return elseStatSeq;
    }
}
