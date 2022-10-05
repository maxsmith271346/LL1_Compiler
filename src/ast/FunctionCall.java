package ast;

import pl434.Symbol;

public class FunctionCall extends Node implements Statement, Expression{
    private Symbol func;
    public ArgumentList argList;


    public FunctionCall(int lineNum, int charPos, Symbol func, ArgumentList argList) {
        super(lineNum, charPos);
        this.argList = argList;
        this.func = func;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    public Symbol function() {
        return func;
    }

    public ArgumentList arguments() {
        return argList;
    }
    
}
