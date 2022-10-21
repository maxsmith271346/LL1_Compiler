package SSA;

public class IntermediateInstruction {
    // operator - Type for this? It could just be a String 
    // operand1
    // operand2
    // operator1 & operator2 can be symbols, values (constants), or references to previous instructions return vals
    // should we make Integer Literal & Float Literal & Bool Literal extend an Operator interface? 
    // should we make a LineNumber class that also extends Operator interface? 

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
        WRITE_NL("WRITE_NL");

    
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

    public IntermediateInstruction(SSAOperator operator, Operand operand_one, Operand operand_two){
        this.operator = operator; 
        this.operand_one = operand_one; 
        this.operand_two = operand_two;
    }

    @Override
    public String toString(){
        return operator + " " + operand_one.toString() + " " + operand_two.toString() + " ";
    }
}
