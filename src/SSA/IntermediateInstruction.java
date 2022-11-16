package SSA;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ast.*;
import ast.BoolLiteral;
import ast.FloatLiteral;
import ast.IntegerLiteral;
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
    // private int insNum;
    private List<Operand> extraOperands; // In the case of function calls there can be more than two operands, so this will keep track of the extras
    private Boolean elim;
    private InstructionNumber instNum;
    public HashSet<Operand> liveVars;

    public Set<IntermediateInstruction> availableExpressions;
    public boolean branchHandled;
    //blic boolean elim;

    public IntermediateInstruction(SSAOperator operator, Operand operand_one, Operand operand_two, int insNum){
        this.operator = operator; 
        this.operand_one = operand_one; 
        this.operand_two = operand_two;
        // this.insNum = insNum;
        this.elim = false;
        this.instNum = new InstructionNumber(insNum);
        this.liveVars = new HashSet<Operand>();

        this.availableExpressions = new HashSet<IntermediateInstruction>();
        if (this.isBranch()){
            branchHandled = false;
        }
        
    }

    @Override
    public String toString(){
        // The operands can be null (sometimes there are less than 2 operands)
        if (operand_two == null && operand_one == null){ 
            return  operator + " " ;
        }
        else if (operand_one == null){
            return operator + " " + operand_two.toString() + " ";
        }
        else if (operand_two == null){
            return operator + " " + operand_one.toString() + " ";
        }

        if (extraOperands == null){
            return  operator + " " + operand_one.toString() + " " + operand_two.toString() + " ";
        }
        else{ 
            String retStr = operator + " " + operand_one.toString() + " " + operand_two.toString();
            for (Operand extra : extraOperands){
                retStr += " " + extra.toString();
            }

            return retStr;
        }
       
    }

    public Boolean isElim() {
        return elim;
    }

    public void eliminate() {
        elim = true;
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

    public InstructionNumber instNum(){
        return instNum;
    }
        
    /*blic void setInsNum(int insNum){
        this.insNum = insNum;
    }*/
    public int insNum(){
        return instNum.getInstructionNumber();
    }

    public Operand getOperandOne(){
        return operand_one;
    }

    public void setOperandOne(Operand newOp){
        this.operand_one = newOp;
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

    public HashSet<Operand> getLiveVars() {
        return liveVars;
    }

    public void addLiveVars(HashSet<Operand> liveVars) {
        this.liveVars.addAll(liveVars);
    }

    public boolean containsOperand(Operand operand){
        if (operand_one != null){
            if (operand_one instanceof Symbol && operand instanceof Symbol){
                String operandOneName = ((Symbol) operand_one).name();
                operandOneName = operandOneName.substring(0, operandOneName.indexOf("_"));

                String operandName = ((Symbol) operand).name();
                operandName = operandName.substring(0, operandName.indexOf("_"));

                if(operandOneName.equals(operandName)){
                    return true;
                }
            }   
            else if (operand_one instanceof InstructionNumber && operand instanceof InstructionNumber){
                if(((InstructionNumber) operand_one).getInstructionNumber() == ((InstructionNumber) operand).getInstructionNumber()){
                    return true;
                }
            }   
        }
        if (operand_two != null){
            if (operand_two instanceof Symbol && operand instanceof Symbol){
                String operandTwoName = ((Symbol) operand_two).name();
                if (operandTwoName.contains("_")){
                    operandTwoName = operandTwoName.substring(0, operandTwoName.indexOf("_"));
                }

                String operandName = ((Symbol) operand).name();
                if (operandName.contains("_")){
                    operandName = operandName.substring(0, operandName.indexOf("_"));
                }
                
                if(operandTwoName.equals(operandName)){
                    return true;
                }
            } 
            else if (operand_two instanceof InstructionNumber && operand instanceof InstructionNumber){
                if(((InstructionNumber) operand_two).getInstructionNumber() == ((InstructionNumber) operand).getInstructionNumber()){
                    return true;
                }
            }     
        }
        if (extraOperands != null){
            for (Operand e : extraOperands){
                if (e.equals(operand)){
                    return true;
                }
            }
        }

        return false;
    }

    public boolean setMatchingOperand(Operand matchingOperand, Operand settingOperand){
        if (operand_one != null){
            if (operand_one instanceof Symbol){
                String operandOneName = ((Symbol) operand_one).name();
                if (operandOneName.contains("_")){
                    operandOneName = operandOneName.substring(0, operandOneName.indexOf("_"));
                }

                String operandName = ((Symbol) matchingOperand).name();
                if (operandName.contains("_")){
                    operandName = operandName.substring(0, operandName.indexOf("_"));
                }

                if(operandOneName.equals(operandName)){
                    operand_one = settingOperand;
                    return true;
                }
            }   
        }
        if (operand_two != null){
            if (operand_two instanceof Symbol){
                String operandTwoName = ((Symbol) operand_two).name();
                if (operandTwoName.contains("_")){
                    operandTwoName = operandTwoName.substring(0, operandTwoName.indexOf("_"));
                }

                String operandName = ((Symbol) matchingOperand).name();
                if (operandName.contains("_")){
                    operandName = operandName.substring(0, operandName.indexOf("_"));
                }
                
                if(operandTwoName.equals(operandName)){
                    operand_two = settingOperand;
                    return true;
                }
            }   
        }
        if (extraOperands != null){
            for (Operand e : extraOperands){
                if (e.equals(matchingOperand)){
                    return true;
                }
            }
        }

        return false;
    }

    public boolean checkOperand(Operand opOne, Operand opTwo){
        if (!opOne.getClass().toString().equals(opTwo.getClass().toString())){
            return true;
        }
        if (opTwo instanceof Symbol && opOne instanceof Symbol){
            String operandOneName = ((Symbol) opOne).name();
            if (operandOneName.contains("_")){
                operandOneName = operandOneName.substring(0, operandOneName.indexOf("_"));
            }

            String operandName = ((Symbol) opTwo).name();
            if (operandName.contains("_")){
                operandName = operandName.substring(0, operandName.indexOf("_"));
            }

            if(!operandOneName.equals(operandName)){
                return true;
            }
        }
        else if (opTwo instanceof BoolLiteral && opOne instanceof BoolLiteral){
            if (!((BoolLiteral) opTwo).value().equals(((BoolLiteral) opOne).value())){
                return true;
            }
        }
        else if (opTwo instanceof IntegerLiteral && opOne instanceof IntegerLiteral){
            if (!((IntegerLiteral ) opTwo).value().equals(((IntegerLiteral) opOne).value())){
                return true;
            }
        }
        else if (opTwo instanceof FloatLiteral && opOne instanceof FloatLiteral){
            if (!((FloatLiteral) opTwo).value().equals(((FloatLiteral) opOne).value())){
                return true;
            }
        }
        else if (opTwo instanceof InstructionNumber && opOne instanceof InstructionNumber){
            //System.out.println(opTwo); 
            //System.out.println(opOne);
            if (((InstructionNumber) opTwo).getInstructionNumber() != ((InstructionNumber) opOne).getInstructionNumber()){
                return true;
            }
        }
        return false;
    }

    public Boolean setLiveVars(HashSet<Operand> liveVars) {

        //System.out.println(this.liveVars);
        //System.out.println(liveVars);
        Boolean exists = false;

        for (Operand o : liveVars) {
            exists = false;
            for (Operand o1 : this.liveVars) {
                exists |= !checkOperand(o, o1);
            }
            if (!exists) {
                this.liveVars = liveVars;
                return true;
            }
        }
        this.liveVars = liveVars;
        return false;
    }

    public boolean conflicts(IntermediateInstruction intIns){
        boolean conflict = true;
        if (intIns.getOperator() == operator && intIns.numberOperators() == this.numberOperators()){
            //System.out.println("one " + intIns);
            //System.out.println("two " + this);
            conflict = false; 
            if (operand_one != null && intIns.getOperandOne() != null){
                if (checkOperand(operand_one, intIns.getOperandOne())){
                    conflict = true;
                    //return true;
                }
                
            }
            if (operand_two != null && intIns.getOperandTwo() != null){
                if (checkOperand(operand_two, intIns.getOperandTwo())){
                    conflict = true;
                    //return true;
                }
            }
            //return false; 
        }
        //conflict = true; 
        //return true;
        /*if (merge){
            if (conflict == false && intIns.getOperator() == SSAOperator.ADDA){
                if (intIns.insNum() != insNum){
                    conflict = true; 
                }
            }
        } */
        return conflict;
    }

    public int numberOperators(){
        int number = 0;
        if (operand_one != null){
            number++;
        }
        if (operand_two != null){
            number++; 
        }
        if (extraOperands != null){
            number += extraOperands.size();
        }
        return number;
    }
}
