package ast;

import pl434.Symbol;

public class Assignment {
    // Designator equivalent to ident if not allowing arrays
    private String designator;
    private String type;
    private Expression expr;


    public Assignment(int lineNum, int charPos, String designator, String type, Expression expr) {
        super(lineNum, charPos);
        this.designator = designator;
        this.type = type;
        this.expr = expr;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    public String lhsDesignator() {
        return designator;
    }

    public String lhsType() {
        return type;
    }

    public Expression rhsExpr() {
        return expr;
    }

}
