package types;

import java.util.List;

import pl434.Symbol;

public abstract class Type {

    // arithmetic
    public Type mul (Type that) {
        if (this instanceof ArrayType || that instanceof ArrayType){
            if(!checkArrayDimensions(this, that)){
                return new ErrorType("Cannot multiply " + this + " with " + that + ".");
            }
            else{ 
                return this;
            }
        }
        if(!(((this instanceof FloatType) && (that instanceof FloatType)) || ((this instanceof IntType) && (that instanceof IntType)))){
            return new ErrorType("Cannot multiply " + this + " with " + that + ".");
        }
        else{ 
            // If the types are as expected, return this, which will be the resulting type for mult
            return this; 
        }
    }

    public Type div (Type that) {
        if (this instanceof ArrayType || that instanceof ArrayType){
            if(!checkArrayDimensions(this, that)){
                return new ErrorType("Cannot divide " + this + " by " + that + ".");
            }
            else{ 
                return this;
            }
        }
        if(!(((this instanceof FloatType) && (that instanceof FloatType)) || ((this instanceof IntType) && (that instanceof IntType)))){
            return new ErrorType("Cannot divide " + this + " by " + that + ".");
        }
        else{ 
            // If the types are as expected, return this, which will be the resulting type for mult
            return this; 
        }
    }

    public Type add (Type that) {
        if (this instanceof ArrayType || that instanceof ArrayType){
            if(!checkArrayDimensions(this, that)){
                return new ErrorType("Cannot add " + this + " to " + that + ".");
            }
            else{ 
                return this;
            }
        }
        if(!(((this instanceof FloatType) && (that instanceof FloatType)) || ((this instanceof IntType) && (that instanceof IntType)))){
            return new ErrorType("Cannot add " + this + " to " + that + ".");
        }
        else{ 
            // If the types are as expected, return this, which will be the resulting type for mult
            return this; 
        }
    }

    public Type sub (Type that) {
        if (this instanceof ArrayType || that instanceof ArrayType){
            if(!checkArrayDimensions(this, that)){
                return new ErrorType("Cannot subtact " + this + " from " + that + ".");
            }
            else{ 
                return this;
            }
        }
        if(!(((this instanceof FloatType) && (that instanceof FloatType)) || ((this instanceof IntType) && (that instanceof IntType)))){
            return new ErrorType("Cannot subtract " + that + " from " + this + ".");
        }
        else{ 
            // If the types are as expected, return this, which will be the resulting type for mult
            return this; 
        }
    }

    public Type pow (Type that) {
        if (this instanceof ArrayType || that instanceof ArrayType){
            if(!checkArrayDimensions(this, that)){
                return new ErrorType("Cannot raise " + this + " to " + that + ".");
            }
            else{ 
                return this;
            }
        }
        if(!(((this instanceof FloatType) && (that instanceof FloatType)) || ((this instanceof IntType) && (that instanceof IntType)))){
        //if(!((this instanceof FloatType) || (this instanceof IntType)) || !((that instanceof FloatType) || (that instanceof IntType))){
            return new ErrorType("Cannot raise " + this + " to " + that + ".");
        }
        else{ 
            return this; 
        }
    } 

    public Type mod (Type that) {
        if (this instanceof ArrayType || that instanceof ArrayType){
            if(!checkArrayDimensions(this, that)){
                return new ErrorType("Cannot modulo " + this + " by " + that + ".");
            }
            else{ 
                return this;
            }
        }
        if(!(((this instanceof FloatType) && (that instanceof FloatType)) || ((this instanceof IntType) && (that instanceof IntType)))){
            return new ErrorType("Cannot modulo " + this + " by " + that + ".");
        }
        else{ 
            return this; 
        }
    } 

    // boolean
    public Type and (Type that) {
        if (this instanceof ArrayType || that instanceof ArrayType){
            if(!checkArrayDimensions(this, that)){
                return new ErrorType("Cannot compute " + this + " and " + that + ".");
            }
            else{ 
                return this;
            }
        }
        if(!(that instanceof BoolType) || !(this instanceof BoolType)){
            return new ErrorType("Cannot compute " + this + " and " + that + ".");
        }
        else{ 
            // result will also be of type bool
            return this;
        }
    }

    public Type or (Type that) {
        if (this instanceof ArrayType || that instanceof ArrayType){
            if(!checkArrayDimensions(this, that)){
                return new ErrorType("Cannot compute " + this + " or " + that + ".");
            }
            else{ 
                return this;
            }
        }
        else if(!(that instanceof BoolType) || !(this instanceof BoolType)){
            return new ErrorType("Cannot compute " + this + " or " + that + ".");
        }
        else{ 
            return this;
        }
    }

    public Type not () {
        Type thisType = this; 
        if (this instanceof ArrayType){
            thisType = ((ArrayType) this).type();
        }
        if(!(thisType instanceof BoolType)){
            return new ErrorType("Cannot negate " + this + ".");
        }
        else{ 
            return this;
        }
    }

    // relational
    public Type compare (Type that) {
        if(!this.getClass().equals(that.getClass())){
            return new ErrorType("Cannot compare " + this + " with " + that + ".");
        }
        else{ 
            // Comparisons will return BoolType
            return new BoolType();
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
        if (this instanceof ArrayType || source instanceof ArrayType){
            if(!checkArrayDimensions(this, source)){
                if (this instanceof ErrorType){
                    return new ErrorType("Cannot assign " + source + " to " + this + ".");
                }
                if (!this.toString().contains("[")){
                    return new ErrorType("Cannot assign " + source + " to AddressOf(" + this + ").");
                }
                return new ErrorType("Cannot assign " + source + " to " + this + ".");
            }
            else{ 
                return this; 
            }
        }

        if(!this.getClass().equals(source.getClass())){
            if (this instanceof ErrorType){
                return new ErrorType("Cannot assign " + source + " to " + this + ".");
            }
            return new ErrorType("Cannot assign " + source + " to AddressOf(" + this + ").");
        }
        else{ 
            return this; 
        }
    }


    public boolean checkArrayDimensions(Type LHS, Type RHS){
        int LHSdims = 0; 
        int RHSdims = 0;
        Type LHSType = LHS; 
        Type RHSType = RHS;

        if (LHS instanceof ArrayType){
            LHSdims = ((ArrayType) LHS).dimList().size()  - ((ArrayType) LHS).indicesCount();
            LHSType = ((ArrayType) LHS).type();
        }
        if (RHS instanceof ArrayType){
            RHSdims = ((ArrayType) RHS).dimList().size()  - ((ArrayType) RHS).indicesCount();
            RHSType = ((ArrayType) RHS).type();
        }

        if (RHSdims != LHSdims){
            return false;
        }
        if (!RHSType.getClass().equals(LHSType.getClass())){
            return false;
        }
        return true; 
    }

    public Type call (List<Symbol> functions) {
        boolean paramsNotEqual = false;
        // check that the args are the same
        for (Symbol function : functions){
            paramsNotEqual = false;
            // check that the sizes of the parameter lists are the same 
           if (function.getParamTypes().size() == ((TypeList) this).getList().size()){
                // if they are, iterate through and check that they are the same 
                for (int i = 0; i < function.getParamTypes().size(); i++){
                    if (!function.getParamTypes().get(i).toString().equals(((TypeList) this).getList().get(i).toString())){
                        paramsNotEqual = true;
                        break;
                    }
                }
                if (!paramsNotEqual){
                    return function.getType();
                }
            }
         }
        // If no match was found, return an error
        return new ErrorType("Call with args " + ((TypeList) this).toString() + " matches no function signature.");
    }

    public Type whileStat(){
        if (!(this instanceof BoolType)){
            return new ErrorType("WhileStat requires bool condition not " + this + ".");
        }
        return null;
    }

    public Type ifStat (){
        if (!(this instanceof BoolType)){
            return new ErrorType("IfStat requires bool condition not " + this + ".");
        }
        return null;
    }

    public Type repeatStat (){
        if (!(this instanceof BoolType)){
            return new ErrorType("RepeatStat requires bool condition not " + this + ".");
        }
        return null;
    }

    public Type returnStat (Symbol currentFunction){
        if (!currentFunction.getType().getClass().equals(this.getClass())){
            return new ErrorType("Function " + currentFunction.name() + " returns " + this +  " instead of " + currentFunction.getType() + ".");
        }
        return null;
    }

    public static Type dimList (List<String> dimList, String arrayName){
        // This is used to check for valid dimensions when the array is instantiated
        for (String d : dimList){
            if (Integer.parseInt(d) <= 0){ 
                return new ErrorType("Array " + arrayName + " has invalid size " + d + ".");
            } 
        }
        return null;
    }
}