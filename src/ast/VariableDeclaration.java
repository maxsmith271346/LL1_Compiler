package ast;

import pl434.Symbol;

public class VariableDeclaration extends Node implements Declaration {
    private Symbol var;

    public VariableDeclaration(int lineNum, int charPos, String type, String ident, int scope){
        super(lineNum, charPos);
        var = new Symbol(ident, type, "var", scope);

    }
    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    public Symbol symbol(){
        return var;
    }
}
