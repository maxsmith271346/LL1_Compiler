package ast;

import java.util.List;

import SSA.Operand;
import pl434.Symbol;
import types.Type;


public class FunctionCall extends Node implements Statement, Expression{
    private List<Symbol> func;
    public ArgumentList argList;
    public Type type; 

    public FunctionCall(int lineNum, int charPos) {
        super(lineNum, charPos);
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
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

    @Override
    public Type type() {
        return type;
    }

    public void addType(Type type){
        this.type = type;
    }

    @Override
    public Operand getOperand() {
        // TODO Auto-generated method stub
        return null;
    }
}
