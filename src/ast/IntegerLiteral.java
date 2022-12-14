package ast;

import SSA.Operand;
import types.*;
import java.util.HashMap;
import pl434.Symbol;
import java.util.HashSet;


public class IntegerLiteral extends Node implements Expression, Operand{
    private String value; 
    private Type type; 

    public IntegerLiteral(int lineNum, int charPos, String value) {
        super(lineNum, charPos);
        this.value = value;
        this.type = new IntType();
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);        
    }

    public String value(){
        return value;
    }

    public Type type(){
        return type;
    }

    public void setIntValue(Integer intVal) {
        value = Integer.toString(intVal);
    }

    public Integer valueAsInt() {
        return Integer.valueOf(value);
    }

    @Override
    public Operand getOperand(HashMap<Symbol, HashSet<Symbol>>  varMap) {
        return this;
    }

    @Override
    public String toString(){
        return value;
    }
}
