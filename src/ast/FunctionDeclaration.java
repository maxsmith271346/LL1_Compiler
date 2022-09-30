package ast;

public class FunctionDeclaration implements Declaration {

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
    
}
