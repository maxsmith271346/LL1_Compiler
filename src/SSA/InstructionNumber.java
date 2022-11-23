package SSA;

import types.Type;
import types.VoidType;

public class InstructionNumber implements Operand{
    private int instructionNumber; 
    private Type type;

    public InstructionNumber(int instructionNumber, Type type){
        this.instructionNumber = instructionNumber;
        this.type = type;
    }

    public int getInstructionNumber(){
        return instructionNumber;
    }

    @Override
    public String toString(){
        return "(" + Integer.toString(instructionNumber) + ")";
    }

    public boolean matches(Operand op){
        if (!(op instanceof InstructionNumber)){
            return false; 
        }
        else{ 
            if (((InstructionNumber) op).getInstructionNumber() != instructionNumber){ 
                return false; 
            }
        }
        return true;
    }

    public Type type(){
        return type;
    }

    public void setType(Type type){
        this.type = type;
    }
}
