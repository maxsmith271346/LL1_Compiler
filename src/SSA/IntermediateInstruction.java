package SSA;

import java.util.List;

import pl434.Symbol;

public class IntermediateInstruction {
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
        RET("RET");

    
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
    private List<Operand> extraOperands;

    public IntermediateInstruction(SSAOperator operator, Operand operand_one, Operand operand_two, int insNum){
        this.operator = operator; 
        this.operand_one = operand_one; 
        this.operand_two = operand_two;
        this.insNum = insNum;
    }

    @Override
    public String toString(){
        if (operand_one == null){
            return operator + " " + operand_two.toString() + " ";
        }
        if (operand_two == null){
            return operator + " " + operand_one.toString() + " ";
        }
        if (operand_two == null && operand_one == null){ 
            return operator + " " ;
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
}
