package ast;

public class IfStatement extends Node implements Statement {
    private Expression relation;
    private StatementSequence thenStatSeq;
    private StatementSequence elseStatSeq;

    public IfStatement(int lineNum, int charPos, Expression relation, StatementSequence thenStatSeq, StatementSequence elseStatSeq) {
        super(lineNum, charPos);
        this.relation = relation;
        this.thenStatSeq = thenStatSeq;
        this.elseStatSeq = elseStatSeq;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    public Expression condition() {
        return relation;
    }

    public StatementSequence thenStatementSeq() {
        return thenStatSeq; 
    }

    public StatementSequence elseStatementSeq() {
        return elseStatSeq;
    }
}