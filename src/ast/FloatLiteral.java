package ast;

import SSA.Operand;
import types.*;
import java.util.HashMap;
import pl434.Symbol;

public class FloatLiteral extends Node implements Expression, Operand{
    private String value; 
    private Type type;

    public FloatLiteral(int lineNum, int charPos, String value) {
        super(lineNum, charPos);
        this.value = value;
        this.type = new FloatType();
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

    @Override
    public Operand getOperand(HashMap<Symbol, Symbol> varMap) {
        return this;
    }

    @Override
    public String toString(){
        return value;
    }
    
}
