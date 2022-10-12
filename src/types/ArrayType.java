package types;

import java.util.List;

public class ArrayType extends Type {
    private Type type; 
    List<String> dimList;

    public ArrayType(Type type, List<String> dimList){
        this.type = type;
        this.dimList = dimList;
    }

    public Type type(){
        return type;
    }
    @Override
    public String toString(){
        return type + dimList.toString().replace(", ", "][");
    }
}
