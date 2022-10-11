package types;

public class ErrorType extends Type {

    private String message;

    public ErrorType(String message){
        this.message = message;
    }
    
    public String getMessage(){
        return message; 
    }

    @Override
    public String toString(){
        return this.getClass().getSimpleName() + "(" + message + ")";
    }

}
