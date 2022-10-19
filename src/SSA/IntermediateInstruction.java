package SSA;

public class IntermediateInstruction {
    // operator - Type for this? It could just be a String 
    // operand1
    // operand2
    // operator1 & operator2 can be symbols, values (constants), or references to previous instructions return vals
    // should we make Integer Literal & Float Literal & Bool Literal extend an Operator interface? 
    // should we make a LineNumber class that also extends Operator interface? 

    private String operator; 
    private Operand operand_one; 
    private Operand operand_two; 

    public IntermediateInstruction(String operator, Operand operand_one, Operand operand_two){
        this.operator = operator; 
        this.operand_one = operand_one; 
        this.operand_two = operand_two;
    }

    @Override
    public String toString(){
        return operator + " " + operand_one.toString() + " " + operand_two.toString() + " ";
    }
}
