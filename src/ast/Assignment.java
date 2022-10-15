package ast;

import pl434.Symbol;

public class Assignment extends Node implements Statement{
    // Designator equivalent to ident if not allowing arrays
    //private String designator;
    //private String type;
    private Expression designator; 
    private Expression expr; // RHS can be a Symbol (x, y, etc), a literal (1, 2, etc), or an Expression 

    public Assignment(int lineNum, int charPos, Expression designator, Expression expr) {
        super(lineNum, charPos);
        this.designator = designator;
        this.expr = expr; 
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    public Expression lhsDesignator() {
        return designator;
    }

    /*public String lhsType() {
        return designator.type();
    }*/

    public Expression rhsExpr() {
        return expr;
    }
}
