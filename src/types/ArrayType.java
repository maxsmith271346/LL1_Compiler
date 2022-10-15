package types;

import java.util.List;

import ast.Expression;

public class ArrayType extends Type {
    private Type type; 
    List<String> dimList;
    int indicesCount;

    /*public ArrayType(Type type, List<String> dimList, List<Expression> indices){
        this.type = type;
        this.dimList = dimList;
        this.indicesCount = indices.size(); // how many indices the user has tried to access
    }*/

    public ArrayType(Type type, List<String> dimList){
        this.type = type;
        this.dimList = dimList;
    }

    public Type type(){
        return type;
    }
    @Override
    public String toString(){
        if (indicesCount - dimList.size() == 0){
            return  type.toString();
        }
        return type + dimList.toString().replace(", ", "][") ;
    }

    public int indicesCount(){
        return indicesCount;
    }
    
    public List<String> dimList(){
        return dimList;
    }

    public void addIndices(List<Expression> indices){
        this.indicesCount = indices.size(); 
    }
}
