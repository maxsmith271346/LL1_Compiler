package ast;

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
    
    public String symbol(){
        return ident + ":" + type; // TODO should this be returning a symbol 
    }
}
