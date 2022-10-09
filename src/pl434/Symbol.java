package pl434;

import ast.ArrayIndex;
import ast.Expression;
import ast.NodeVisitor;

import java.util.ArrayList;
import java.util.List;

public class Symbol implements Expression {

    public enum Type{ 
        VOID("void"),
        BOOL("bool"),
        INT("int"),
        FLOAT("float");

        private String defaultTypeStr; 

        Type (String typeStr){
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

        for (Type t: Type.values()){
            if(returnType.equals(t.getDefaultTypeStr())){
                this.returnType = t; 
                break;
            }
        }
    }

    public Symbol (String name, String returnType, String symbolType, List<String> paramTypes) {
        this.name = name;
        this.symbolType = symbolType; 

        for (Type t : Type.values()){
            if(returnType.equals(t.getDefaultTypeStr())) {
                this.returnType = t; 
                break;
            }
        }

        this.paramTypes = new ArrayList<Type>();

        for (String pt : paramTypes) {
            for (Type t : Type.values()) {
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
        for (Type t : Type.values()){
            if(type.equals(t.getDefaultTypeStr())) {
                this.returnType = t; 
                break;
            }
        }
    }

    public void addParams (List<String> paramTypes) {
        this.paramTypes = new ArrayList<Type>();

        for (String pt : paramTypes) {
            for (Type t : Type.values()) {
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

    public List<Type> getParamTypes(){
        return paramTypes;
    }

    public String getSymbolType(){
        return symbolType;
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
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
        
    }

}