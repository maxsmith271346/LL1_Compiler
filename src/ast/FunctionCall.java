package ast;

import pl434.Symbol;
import types.Type;


public class FunctionCall extends Node implements Statement, Expression{
    private Symbol func;
    public ArgumentList argList;


    public FunctionCall(int lineNum, int charPos) {
        super(lineNum, charPos);
    }

    @Override
    public Type accept(NodeVisitor visitor) {
        return visitor.visit(this);
    }

    public Symbol function() {
        return func;
    }

    public ArgumentList arguments() {
        return argList;
    }

    public void putFunc(Symbol func){
        this.func = func; 
    }

    public void putArgs(ArgumentList argList){
        this.argList = argList;
    }
}
