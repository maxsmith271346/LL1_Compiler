package ast;

import java.util.List;

import SSA.InstructionNumber;
import SSA.Operand;
import pl434.Symbol;
import types.Type;
import java.util.HashMap;
import pl434.Symbol;

public class ArrayIndex extends Node implements Expression {
    // originally used leftExpr and rightExpr for pretty printer, but instead I will just be using a list of expressions
    //public Expression leftExpr;
    //public Expression rightExpr;
    private Symbol arrayIdent;
    private List<Expression> indices;
    private Type type;
    private InstructionNumber insNumber;

    //public Symbol arrayIdent;
    /*public ArrayIndex(int lineNum, int charPos, Expression leftExpr, Expression rightExpr) {
        super(lineNum, charPos);
        this.arrayIdent = ((Symbol) leftExpr);
        this.leftExpr = leftExpr;
        this.rightExpr = rightExpr;
    }*/

    public ArrayIndex(int lineNum, int charPos, Symbol arrayIdent, List<Expression> indices) {
        super(lineNum, charPos);
        this.arrayIdent = arrayIdent;
        this.indices = indices;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);

    }

    /*public Expression leftExpr() {
        return leftExpr;
    }

    public Expression rightExpr() {
        return rightExpr;
    }*/
    
    public List<String> dimList(){
        return arrayIdent.dimList;
    }

    public Type getType(){
        return arrayIdent.getType();
    }

    public String name(){
        return arrayIdent.name();
    }

    public List<Expression> indices(){
        return indices;
    }

    public Symbol arrayIdent(){
        return arrayIdent;
    }

    @Override
    public Type type() {
        return type;
    }

    public void addType(Type type){
        this.type = type;
    }

    @Override
    public Operand getOperand(HashMap<Symbol, Symbol> varMap) {
        return insNumber;
    }

    public void setInsNumber(int insNumber){
        this.insNumber = new InstructionNumber(insNumber);
    }
}
