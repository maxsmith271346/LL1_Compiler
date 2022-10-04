package ast;

import java.util.ArrayList;
import java.util.List;

import pl434.Symbol;

public class FunctionDeclaration extends Node implements Declaration {
    private String type; // TODO: enum type 
    private String ident; 
    private List<String> formalParam; // TODO: enum type
    FunctionBody funcBody; 

    public FunctionDeclaration(int lineNum, int charPos, String type, String ident, FunctionBody funcBody){
        super(lineNum, charPos);
        this.type = type;
        this.ident = ident; 
        this.funcBody = funcBody;
        formalParam = new ArrayList<String>();
    }
    
    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    public String returnType() {
        return type;
    }
    
    public FunctionBody body() {
        return funcBody; 
    }

    public Symbol function(){
        Symbol function = new Symbol(ident, type, "function");
        function.paramTypes.addAll(formalParam);
        return function;
    }

    public List<String> formalParameters() {
        return formalParam;
    }
}
