package SSA;

import java.util.List;

import pl434.Symbol;

public class IntermediateInstruction {

    // Possible Operators for the TACs
    public enum SSAOperator {
        NEG("NEG"),
        ADD("ADD"),
        SUB("SUB"),
        MUL("MUL"),
        DIV("DIV"),
        MOD("MOD"),
        POW("POW"),
        CMP("CMP"),
        NOT("NOT"),
        OR("OR"),
        AND("AND"),

        ADDA("ADDA"),
        LOAD("LOAD"),
        STORE("STORE"),
        PHI("PHI"),
        END("END"),
        BRA("BRA"),
        BNE("BNE"),
        BEQ("BEQ"),
        BLE("BLE"),
        BLT("BLT"),
        BGE("BGE"),
        BGT("BGT"),

        READ("READ"),
        WRITE("WRITE"),
        READ_B("READ_B"),
        WRITE_B("WRITE_B"),
        READ_F("READ_F"),
        WRITE_F("WRITE_F"),
        WRITE_NL("WRITE_NL"),

        MOVE("MOVE"),
        CALL("CALL"), 
        RET("RET"),

        NONE("");

    
        private String opString;

        SSAOperator () {
            opString = "";
        }
        
        SSAOperator (String opString) {
            this.opString = opString;
        }

        public String getOpString () {
            return this.opString;
        }

    }

    private SSAOperator operator; 
    private Operand operand_one; 
    private Operand operand_two; 
    private int insNum;
    private List<Operand> extraOperands; // In the case of function calls there can be more than two operands, so this will keep track of the extras

    public IntermediateInstruction(SSAOperator operator, Operand operand_one, Operand operand_two, int insNum){
        this.operator = operator; 
        this.operand_one = operand_one; 
        this.operand_two = operand_two;
        this.insNum = insNum;
    }

    @Override
    public String toString(){
        // The operands can be null (sometimes there are less than 2 operands)
        if (operand_two == null && operand_one == null){ 
            return operator + " " ;
        }
        else if (operand_one == null){
            return operator + " " + operand_two.toString() + " ";
        }
        else if (operand_two == null){
            return operator + " " + operand_one.toString() + " ";
        }

        if (extraOperands == null){
            return operator + " " + operand_one.toString() + " " + operand_two.toString() + " ";
        }
        else{ 
            String retStr = operator + " " + operand_one.toString() + " " + operand_two.toString();
            for (Operand extra : extraOperands){
                retStr += " " + extra.toString();
            }

            return retStr;
        }
       
    }

    public void addExtraOperands(List<Operand> extraOperands){
        this.extraOperands = extraOperands;
    }

    public SSAOperator getOperator(){
        return operator;
    }

    // These branch-related methods are used in the pruning portion of the SSA construction to "repair" any branch instructions 
    public void updateBranchIns(BasicBlock BB){
        if (operand_two == null){
            operand_one = BB;
        }
        else{ 
            operand_two = BB;
        }
    }

    public Boolean isBranch(){
        if (operator.equals(SSAOperator.BRA) || operator.equals(SSAOperator.BNE) || operator.equals(SSAOperator.BEQ) || operator.equals(SSAOperator.BLE) || operator.equals(SSAOperator.BLT) || operator.equals(SSAOperator.BGE)  || operator.equals(SSAOperator.BGT)){
            return true; 
        }
        return false;
    }

    // This wll get the function name from a "call" instruction
    // Complicated because the function name is at the end of the TAC 
    public String getFuncName(){
        if (extraOperands == null){
            if (operand_two == null){
                if (operand_one instanceof Symbol){
                    return ((Symbol) operand_one).name();
                }
            }
            else{ 
                return ((Symbol) operand_two).name();
            }
        }
        else{ 
            if (extraOperands.get(extraOperands.size() - 1) instanceof Symbol){
                return ((Symbol) extraOperands.get(extraOperands.size() - 1)).name();
            }
        }
        return "";
    }

    public int insNum(){
        return insNum;
    }

    public Operand getOperandOne(){
        return operand_one;
    }

    public Operand getOperandTwo(){
        return operand_two;
    }

    public void putOperator(SSAOperator operator){
        this.operator = operator;
    }

    public void putOperandOne(Operand operand_one){
        this.operand_one = operand_one;
    }

    public void putOperandTwo(Operand operand_two){
        this.operand_two = operand_two ;
    }
}
