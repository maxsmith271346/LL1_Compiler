package SSA;

import java.util.List;

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

        MOVE("MOVE");

    
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
    private List<Operand> extraOperands;

    public IntermediateInstruction(SSAOperator operator, Operand operand_one, Operand operand_two){
        this.operator = operator; 
        this.operand_one = operand_one; 
        this.operand_two = operand_two;
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
        return operator + " " + operand_one.toString() + " " + operand_two.toString() + " ";
    }

    public void addExtraOperands(List<Operand> extraOperands){
        this.extraOperands = extraOperands;
    }
}
