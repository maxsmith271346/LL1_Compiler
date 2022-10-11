package types;

public class AddressOf extends Type{
    private Type type;

    public AddressOf(Type type){
        this.type = type;
    }

    public Type getType(){
        return type; 
    }
    @Override
    public String toString(){
        return this.getClass().getSimpleName() + "(" + type + ")";
    }
}
