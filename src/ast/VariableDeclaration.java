package ast;

import pl434.Symbol;

public class VariableDeclaration extends Node implements Declaration {
    private String type; // TODO: enum type 
    private String ident; 

    public VariableDeclaration(int lineNum, int charPos, String type, String ident){
        super(lineNum, charPos);
        this.type = type; 
        this.ident = ident;

    }
    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
    
    public Symbol symbol(){
        return new Symbol(ident, type, "variable"); // TODO should this be returning a symbol 
    }
}
