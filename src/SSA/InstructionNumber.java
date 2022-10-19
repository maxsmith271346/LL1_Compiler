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
        return Integer.toString(instructionNumber);
    }
}
