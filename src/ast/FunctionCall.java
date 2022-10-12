package ast;

import java.util.List;

import pl434.Symbol;
import types.Type;


public class FunctionCall extends Node implements Statement, Expression{
    private List<Symbol> func;
    public ArgumentList argList;


    public FunctionCall(int lineNum, int charPos) {
        super(lineNum, charPos);
    }

    @Override
    public Type accept(NodeVisitor visitor) {
        return visitor.visit(this);
    }

    public List<Symbol> function() {
        return func;
    }

    public ArgumentList arguments() {
        return argList;
    }

    public void putFunc(List<Symbol> func){
        this.func = func; 
    }

    public void putArgs(ArgumentList argList){
        this.argList = argList;
    }
}
