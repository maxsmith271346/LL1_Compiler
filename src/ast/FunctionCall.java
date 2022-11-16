package ast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import SSA.IntermediateInstruction;
import SSA.Operand;
import SSA.InstructionNumber;
import pl434.Symbol;
import types.Type;
import java.util.HashMap;
import pl434.Symbol;
import java.util.HashSet;



public class FunctionCall extends Node implements Statement, Expression{
    private List<Symbol> func;
    public ArgumentList argList;
    public Type type; 
    public List<Symbol> predefinedFunctions;
    private InstructionNumber insNumber; 

    public FunctionCall(int lineNum, int charPos) {
        super(lineNum, charPos);
        predefinedFunctions = new ArrayList<Symbol>();
        predefinedFunctions.add(new Symbol("readInt", "int", "func"));
        predefinedFunctions.add(new Symbol("readFloat", "float", "func"));
        predefinedFunctions.add(new Symbol("readBool", "bool", "func"));
        predefinedFunctions.add(new Symbol("printInt", "void", "func", new ArrayList<String>(Arrays.asList("int"))));
        predefinedFunctions.add(new Symbol("printFloat", "void", "func", new ArrayList<String>(Arrays.asList("float"))));
        predefinedFunctions.add(new Symbol("printBool", "void", "func", new ArrayList<String>(Arrays.asList("bool"))));
        predefinedFunctions.add(new Symbol("println", "void", "func"));
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
    public Operand getOperand(HashMap<Symbol, HashSet<Symbol>>  varMap) {
        return insNumber;
    }

    public void setInsNumber(InstructionNumber insNumber){
        this.insNumber = insNumber;
    }

    public Symbol getFunctionFromType(){
        boolean paramsNotEqual = false;
        for (Symbol function: func){
            paramsNotEqual = false;
            if (function.getParamTypes().size() == (argList.type().getList().size())){
                 // if they are, iterate through and check that they are the same 
                 for (int i = 0; i < function.getParamTypes().size(); i++){
                    if (!function.getParamTypes().get(i).toString().equals(argList.type().getList().get(i).toString())){
                        paramsNotEqual = true;
                        break;
                    }
                }
                if (!paramsNotEqual){
                    return function;
                }
            }
        }
        return null;
    }
}
