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

//import types.*;

public class Symbol implements Expression {

    public enum primitiveType{ 
        VOID("void"),
        BOOL("bool"),
        INT("int"),
        FLOAT("float");

        private String defaultTypeStr; 

        primitiveType (String typeStr){
            defaultTypeStr = typeStr;
        }

        public String getDefaultTypeStr(){ 
            return this.defaultTypeStr;
        }

        public boolean matches (String type){
            if (this.defaultTypeStr.equals(type)){
                return true; 
            }
            else{ 
                return false; 
            }
        }

        @Override
        public String toString() {
            return this.defaultTypeStr;
        }
    }

    private String name;
    private primitiveType returnType; 
    private String symbolType; // function, variable, etc TODO enum here - maybe we want to make classes that extend symbol for the different kinds? 
    // ^ var, func, param, arr?
    private List<primitiveType> paramTypes;
    public ArrayIndex arrayIndex;
    public List<String> dimList; // for var[2][3], will store [2, 3]
    private Type type; // added this for type checker

    // TODO: Will need to assign addresses for symbols
    // private int address;

    public Symbol (String name, String returnType, String symbolType) {
        this.name = name;
        this.symbolType = symbolType; 
        this.paramTypes = new ArrayList<primitiveType>();
        this.dimList = new ArrayList<String>(); 

        for (primitiveType t: primitiveType.values()){
            if(returnType.equals(t.getDefaultTypeStr())){
                this.returnType = t; 
                break;
            }
        }

        this.type = getTypeFromPrimitiveType(this.returnType);

    }

    public Symbol (String name, String returnType, String symbolType, List<String> paramTypes) {
        this.name = name;
        this.symbolType = symbolType; 

        for (primitiveType t : primitiveType.values()){
            if(returnType.equals(t.getDefaultTypeStr())) {
                this.returnType = t; 
                break;
            }
        }

        this.paramTypes = new ArrayList<primitiveType>();

        for (String pt : paramTypes) {
            for (primitiveType t : primitiveType.values()) {
                if(pt.equals(t.getDefaultTypeStr())) {
                    this.paramTypes.add(t); 
                    break;
                }
            }
        }
    }
    
    public String name () {
        return name;
    }
    public String type (){
        return returnType.getDefaultTypeStr();
    }

    public void setReturnType(String type) {
        for (primitiveType t : primitiveType.values()){
            if(type.equals(t.getDefaultTypeStr())) {
                this.returnType = t; 
                break;
            }
        }
    }

    public void addParams (List<String> paramTypes) {
        this.paramTypes = new ArrayList<primitiveType>();

        for (String pt : paramTypes) {
            for (primitiveType t : primitiveType.values()) {
                if(pt.equals(t.getDefaultTypeStr())) {
                    this.paramTypes.add(t); 
                    break;
                }
            }
        }
    }


    public void addDims(List<String> dims){
        this.dimList = dims;
    }

    public List<String> getDims(){
        return dimList;
    }

    public List<primitiveType> getParamTypes(){
        return paramTypes;
    }

    public String getSymbolType(){
        return symbolType;
    }

    public Type getType(){
        return type;
    }

    public Type getTypeFromPrimitiveType(primitiveType pt){
        if (pt == primitiveType.INT){
            return new IntType();
        }
        else if (pt == primitiveType.FLOAT){
            return new FloatType();
        }
        else if (pt == primitiveType.BOOL){
            return new BoolType();
        }
        else if (pt == primitiveType.VOID){
            return new VoidType();
        }
        return null;
    }

    @Override
    public String toString(){
        if (symbolType.equals("func")){
            String paramTypesStr = paramTypes.toString();
            return name + ":(" + paramTypesStr.replace("[", "").replace("]", "") + ")->" + returnType.getDefaultTypeStr();
        } 
        else if (symbolType.equals("var") || symbolType.equals("param")){ 
            if (dimList.size() != 0){
                String dimListStr  = dimList.toString();
                return name + ":" + returnType.getDefaultTypeStr() + dimListStr.replace(", ", "][");
            }
            return name + ":" + returnType.getDefaultTypeStr();
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