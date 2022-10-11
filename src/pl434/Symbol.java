package pl434;

import ast.ArrayIndex;
import ast.Expression;
import ast.NodeVisitor;
import types.BoolType;
import types.FloatType;
import types.IntType;
import types.Type;
import types.VoidType;

import java.util.ArrayList;
import java.util.List;

import javax.print.FlavorException;

//import types.*;

public class Symbol implements Expression {

    private String name;
    private Type returnType; 
    private String symbolType; // function, variable, etc TODO enum here - maybe we want to make classes that extend symbol for the different kinds? 
    // ^ var, func, param, arr?
    private List<Type> paramTypes;
    public ArrayIndex arrayIndex;
    public List<String> dimList; // for var[2][3], will store [2, 3]

    // TODO: Will need to assign addresses for symbols
    // private int address;

    public Symbol (String name, String returnType, String symbolType) {
        this.name = name;
        this.symbolType = symbolType; 
        this.paramTypes = new ArrayList<Type>();
        this.dimList = new ArrayList<String>(); 

        switch (returnType){
            case "int": 
                this.returnType = new IntType();
                break;
            case "float":
                this.returnType = new FloatType();
                break;
            case "void":
                this.returnType = new VoidType();
                break;
            case "bool":
                this.returnType = new BoolType();
                break;
            };
    }

    public Symbol (String name, String returnType, String symbolType, List<String> paramTypes) {
        this.name = name;
        this.symbolType = symbolType; 

        switch (returnType){
            case "int": 
                this.returnType = new IntType();
                break;
            case "float":
                this.returnType = new FloatType();
                break;
            case "void":
                this.returnType = new VoidType();
                break;
            case "bool":
                this.returnType = new BoolType();
                break;
            };

        this.paramTypes = new ArrayList<Type>();

        for (String pt : paramTypes) {
            switch (pt){
                case "int": 
                    this.paramTypes.add(new IntType());
                    break;
                case "float":
                    this.paramTypes.add(new FloatType());
                    break;
                case "void":
                    this.paramTypes.add(new VoidType());
                    break;
                case "bool":
                    this.paramTypes.add(new BoolType());
                    break;
                };
        }
    }
    
    public String name () {
        return name;
    }
    public String type (){
        return returnType.toString();
    }

    public void setReturnType(String type) {
        switch (type){
            case "int": 
                this.returnType = new IntType();
                break;
            case "float":
                this.returnType = new FloatType();
                break;
            case "void":
                this.returnType = new VoidType();
                break;
            case "bool":
                this.returnType = new BoolType();
                break;
            };
    }

    public void addParams (List<String> paramTypes) {
        this.paramTypes = new ArrayList<Type>();
        for (String pt : paramTypes) {
            switch (pt){
                case "int": 
                    this.paramTypes.add(new IntType());
                    break;
                case "float":
                    this.paramTypes.add(new FloatType());
                    break; 
                case "void":
                    this.paramTypes.add(new VoidType());
                    break;
                case "bool":
                    this.paramTypes.add(new BoolType());
                    break;
                };
        }
    }


    public void addDims(List<String> dims){
        this.dimList = dims;
    }

    public List<String> getDims(){
        return dimList;
    }

    public List<Type> getParamTypes(){
        return paramTypes;
    }

    public String getSymbolType(){
        return symbolType;
    }

    public Type getType(){
        return returnType;
    }
    @Override
    public String toString(){
        if (symbolType.equals("func")){
            String paramTypesStr = paramTypes.toString();
            return name + ":(" + paramTypesStr.replace("[", "").replace("]", "") + ")->" + returnType;
        } 
        else if (symbolType.equals("var") || symbolType.equals("param")){ 
            if (dimList.size() != 0){
                String dimListStr  = dimList.toString();
                return name + ":" + returnType + dimListStr.replace(", ", "][");
            }
            return name + ":" + returnType;
        }  
        else{ 
            return "";
        }
    }
    @Override
    public Type accept(NodeVisitor visitor) {
        return visitor.visit(this);
        
    }

}