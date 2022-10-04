package ast;

public class RepeatStatement extends Node implements Statement {
    private Relation relation;
    private StatementSequence statSeq;

    public RepeatStatement(int lineNum, int charPos, Relation relation, StatementSequence statSeq) {
        super(lineNum, charPos);
        this.relation = relation;
        this.statSeq = statSeq;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    public Relation condition() {
        return relation;
    }

    public StatementSequence statementSeq() {
        return statSeq; 
    }
}