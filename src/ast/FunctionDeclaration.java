package ast;

import java.util.ArrayList;
import java.util.List;

import pl434.Symbol;
import pl434.Token;

public class FunctionDeclaration extends Node implements Declaration {
    private Symbol func;
    private List<String> paramTypes; // TODO: enum type
    FunctionBody funcBody; 

    public FunctionDeclaration(int lineNum, int charPos, Symbol func, FunctionBody funcBody){
        super(lineNum, charPos);
        this.func = func;
        this.funcBody = funcBody;
    }
    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
    // TODO: consistent naming convention w/ getters and attributes b/t classes (esp. varDecl)
    public Symbol function() {
        return func;
    }
    
    public String returnType() {
        return func.type();
    }

    // public void setReturnType(Token type)  {
    //     func.setReturnType(type.lexeme());
    // }

    public String name() {
        return func.name();
    }

    public FunctionBody body(){
        return funcBody; 
    }

    public List<String> formalParameters() {
        return paramTypes;
    }
}