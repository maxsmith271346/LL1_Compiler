package ast;

public class VariableDeclaration extends Node implements Declaration {
    private String type; // TODO: enum type 
    private String ident; 

    protected VariableDeclaration(){
        //TODO: finish constructor
    }
    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
    
}
