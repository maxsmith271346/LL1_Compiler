package types;

public abstract class Type {

    // arithmetic
    public Type mul (Type that) {
        if(!that.getClass().equals(this.getClass())){
            return new ErrorType("Cannot multiply " + this + " with " + that + ".");
        }
        else{ 
            return this;
        }
    }

    public Type div (Type that) {
        if(!that.getClass().equals(this.getClass())){
            return new ErrorType("Cannot divide " + this + " by " + that + ".");
        }
        else{ 
            return this;
        }
    }

    public Type add (Type that) {
        if(!that.getClass().equals(this.getClass())){
            return new ErrorType("Cannot add " + this + " to " + that + ".");
        }
        else{ 
            return this; 
        }
    }

    public Type sub (Type that) {
        if(!that.getClass().equals(this.getClass())){
            return new ErrorType("Cannot subtract " + that + " from " + this + ".");
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
        return new ErrorType("Cannot compare " + this + " with " + that + ".");
    }

    // designator
    public Type deref () {
        return new ErrorType("Cannot dereference " + this);
    }

    public Type index (Type that) {
        return new ErrorType("Cannot index " + this + " with " + that + ".");
    }

    // statements
    public Type assign (Type source) {
        if(!source.getClass().equals(this.getClass())){
            return new ErrorType("Cannot assign " + source + " to " + this + ".");
        }
        else{ 
            return this;
        }
    }

    // made this static
    public static Type call (Type args) {
        //return new ErrorType("Cannot call " + this + " using " + args + ".");
        return new ErrorType("Call with args " + args + " matches no function signature.");
    }

    @Override
    public String toString(){
       return this.getClass().getSimpleName().toString();
    }
}
