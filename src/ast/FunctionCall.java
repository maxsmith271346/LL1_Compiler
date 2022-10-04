package ast;

import pl434.Symbol;

public class FunctionCall extends Node implements Statement, Expression{
    private Symbol func;
    public ArgumentList argList;


    public FunctionCall(int lineNum, int charPos, Symbol func) {
        super(lineNum, charPos);
        argList = new ArgumentList(lineNum, charPos);
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
