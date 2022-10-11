package types;

import java.util.List;

import pl434.Symbol;
import pl434.SymbolTable;

public abstract class Type {

    // arithmetic
    public Type mul (Type that) {
        Type thisType = this; 
        Type thatType = that;
        if (that instanceof AddressOf){
            thatType = ((AddressOf) that).getType();
        }
        if (this instanceof AddressOf){
            thisType = ((AddressOf) this).getType();
        }
        if(!(((thisType instanceof FloatType) && (thatType instanceof FloatType)) || ((thisType instanceof IntType) && (thatType instanceof IntType)))){
            return new ErrorType("Cannot multiply " + thisType + " with " + thatType + ".");
        }
        else{ 
            return this; 
        }
    }

    public Type pow (Type that) {
        Type thisType = this; 
        Type thatType = that;
        if (that instanceof AddressOf){
            thatType = ((AddressOf) that).getType();
        }
        if (this instanceof AddressOf){
            thisType = ((AddressOf) this).getType();
        }
        if(!(((thisType instanceof FloatType) && (thatType instanceof FloatType)) || ((thisType instanceof IntType) && (thatType instanceof IntType)))){
            return new ErrorType("Cannot power " + thisType + " with " + thatType + ".");
        }
        else{ 
            return this; 
        }
    } 

    public Type mod (Type that) {
        Type thisType = this; 
        Type thatType = that;
        if (that instanceof AddressOf){
            thatType = ((AddressOf) that).getType();
        }
        if (this instanceof AddressOf){
            thisType = ((AddressOf) this).getType();
        }
        if(!(((thisType instanceof FloatType) && (thatType instanceof FloatType)) || ((thisType instanceof IntType) && (thatType instanceof IntType)))){
            return new ErrorType("Cannot modulo " + thisType + " with " + thatType + ".");
        }
        else{ 
            return this; 
        }
    } 

    public Type div (Type that) {
        Type thisType = this; 
        Type thatType = that;
        if (that instanceof AddressOf){
            thatType = ((AddressOf) that).getType();
        }
        if (this instanceof AddressOf){
            thisType = ((AddressOf) this).getType();
        }
        if(!(((thisType instanceof FloatType) && (thatType instanceof FloatType)) || ((thisType instanceof IntType) && (thatType instanceof IntType)))){
            return new ErrorType("Cannot divide " + thisType + " by " + thatType + ".");
        }
        else{ 
            return this; 
        }
    }

    public Type add (Type that) {
        Type thisType = this; 
        Type thatType = that;
        if (that instanceof AddressOf){
            thatType = ((AddressOf) that).getType();
        }
        if (this instanceof AddressOf){
            thisType = ((AddressOf) this).getType();
        }
        if(!(((thisType instanceof FloatType) && (thatType instanceof FloatType)) || ((thisType instanceof IntType) && (thatType instanceof IntType)))){
            return new ErrorType("Cannot add " + thisType + " to " + thatType + ".");
        }
        else{ 
            return this; 
        }
    }

    public Type sub (Type that) {
        Type thisType = this; 
        Type thatType = that;
        if (that instanceof AddressOf){
            thatType = ((AddressOf) that).getType();
        }
        if (this instanceof AddressOf){
            thisType = ((AddressOf) this).getType();
        }
        if(!(((thisType instanceof FloatType) && (thatType instanceof FloatType)) || ((thisType instanceof IntType) && (thatType instanceof IntType)))){
            return new ErrorType("Cannot subtract " + thatType + " from " + thisType + ".");
        }
        else{ 
            return this; 
        }
    }

    // boolean
    public Type and (Type that) {
        if(!(that instanceof BoolType) || !(this instanceof BoolType)){
            return new ErrorType("Cannot compute " + this + " and " + that + ".");
        }
        else{ 
            return this;
        }
    }

    public Type or (Type that) {
        if(!(that instanceof BoolType) || !(this instanceof BoolType)){
            return new ErrorType("Cannot compute " + this + " or " + that + ".");
        }
        else{ 
            return this;
        }
    }

    public Type not () {
        if(!(this instanceof BoolType)){
            return new ErrorType("Cannot negate " + this + ".");
        }
        else{ 
            return this;
        }
    }

    // relational
    public Type compare (Type that) {
        Type thisType = this; 
        Type thatType = that;
        if (that instanceof AddressOf){
            thatType = ((AddressOf) that).getType();
        }
        if (this instanceof AddressOf){
            thisType = ((AddressOf) this).getType();
        }
        if(!thisType.getClass().equals(thatType.getClass())){
            return new ErrorType("Cannot compare " + thisType + " with " + thatType + ".");
        }
        else{ 
            return this;
        }
    }

    // designator
    public Type deref () {
        return new ErrorType("Cannot dereference " + this);
    }

    public Type index (Type that) {
        if (!(that instanceof IntType)){
            return new ErrorType("Cannot index " + this + " with " + that + ".");
        }
        return null;
       
    }

    // statements
    public Type assign (Type source) {
        if(!this.getClass().equals(source.getClass())){
            if (this instanceof AddressOf){
                if(!((AddressOf)this).getType().getClass().equals(source.getClass())){
                    return new ErrorType("Cannot assign " + source + " to " + this + ".");
                }
                else{ 
                    return this;
                }
            }
            return new ErrorType("Cannot assign " + source + " to " + this + ".");
        }
        else{ 
            if (this instanceof AddressOf && source instanceof AddressOf){
                if (!((AddressOf) this).getType().toString().equals(((AddressOf) source).getType().toString())){
                    return new ErrorType("Cannot assign " + source + " to " + this + ".");
                }
            }
            return this;
        }
    }

    // made this static
    public static Type call (Type args, Symbol function) {
        try{
            // check that the args are the same
            if (function.getParamTypes().size() == ((TypeList) args).getList().size()){
                for (int i = 0; i < function.getParamTypes().size(); i++){
                    if (!function.getParamTypes().get(i).toString().equals(((TypeList) args).getList().get(i).toString())){
                        return new ErrorType("Call with args " + ((TypeList) args).toString() + " matches no function signature.");
                    }
                }
            }
            else{ 
                return new ErrorType("Call with args " + ((TypeList) args).toString() + " matches no function signature.");
            }
            
            return function.getType();
        }catch (Error e){
            return new ErrorType("Call with args " + ((TypeList) args).toString() + " matches no function signature.");
        }
        //return new ErrorType("Cannot call " + this + " using " + args + "."); -- original but doesnt match tests
    }

    public static Type whileStat (Type condition){
        if (condition instanceof AddressOf){
            condition = ((AddressOf) condition).getType();
        }
        if (!(condition instanceof BoolType)){
            return new ErrorType("WhileStat requires bool condition not " + condition + ".");
        }
        return null;
    }

    public static Type ifStat (Type condition){
        if (condition instanceof AddressOf){
            condition = ((AddressOf) condition).getType();
        }
        if (!(condition instanceof BoolType)){
            return new ErrorType("IfStat requires bool condition not " + condition + ".");
        }
        return null;
    }

    public static Type repeatStat (Type condition){
        if (condition instanceof AddressOf){
            condition = ((AddressOf) condition).getType();
        }
        if (!(condition instanceof BoolType)){
            return new ErrorType("RepeatStat requires bool condition not " + condition + ".");
        }
        return null;
    }

    public static Type returnStat (Type returnVal, Symbol currentFunction){
        if (returnVal instanceof AddressOf){
            returnVal = ((AddressOf) returnVal).getType();
        }
        if (!currentFunction.getType().toString().equals(returnVal.toString())){
            return new ErrorType("Function " + currentFunction.name() + " returns " + returnVal +  " instead of " + currentFunction.getType());
        }
        return null;
    }

    public static Type dimList (List<String> dimList, String arrayName){
        for (String d : dimList){
            if (Integer.parseInt(d) <= 0){ 
                return new ErrorType("Array " + arrayName + " has invalid size " + d);
            } 
        }
        return null;
    }
    /*@Override
    public String toString(){
       return this.getClass().getSimpleName().toString();
    }*/
}
