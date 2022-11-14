package SSA;

public class InstructionNumber implements Operand{
    private int instructionNumber; 
    
    public InstructionNumber(int instructionNumber){
        this.instructionNumber = instructionNumber;
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
}
