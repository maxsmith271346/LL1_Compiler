package ast;

import java.util.ArrayList;
import java.util.List;

import pl434.Symbol;

public class FunctionDeclaration extends Node implements Declaration {
    private Symbol func;
    private List<String> formalParam; // TODO: enum type
    FunctionBody funcBody; 

    public FunctionDeclaration(int lineNum, int charPos, String type, String ident, FunctionBody funcBody){
        super(lineNum, charPos);
        func = new Symbol(ident, type, "function");
        this.funcBody = funcBody;
        formalParam = new ArrayList<String>();
        func.paramTypes.addAll(formalParam);
    }
    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    public Symbol function() {
        return func;
    }
    
    public String returnType() {
        return func.type();
    }

    public String name() {
        return func.name();
    }

    public FunctionBody body(){
        return funcBody; 
    }

    public List<String> formalParameters() {
        return formalParam;
    }
}
