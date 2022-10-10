package ast;

import pl434.Symbol;
import types.Type;


public class VariableDeclaration extends Node implements Declaration {
    private Symbol var;

    public VariableDeclaration(int lineNum, int charPos, String type, String ident){
        super(lineNum, charPos);
        var = new Symbol(ident, type, "var");

    }
    @Override
    public Type accept(NodeVisitor visitor) {
        return visitor.visit(this);
    }

    public Symbol symbol(){
        return var;
    }
}
