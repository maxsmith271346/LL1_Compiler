package SSA;

import java.security.spec.EllipticCurve;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ast.*;
import ast.BoolLiteral;
import ast.FloatLiteral;
import ast.IntegerLiteral;
import pl434.Symbol;
import pl434.Token;
import types.Type;

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

        READ_I("READ_I"),
        WRITE_I("WRITE_I"),
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
    private Operand operandOne; 
    private Operand operandTwo; 
    private Integer registerOne; 
    private Integer registerTwo;
    private List<Integer> extraRegisters;
    // private int insNum;
    private List<Operand> extraOperands; // In the case of function calls there can be more than two operands, so this will keep track of the extras
    private Boolean elim;
    private InstructionNumber instNum;
    public HashSet<Operand> liveVars;
    public Integer returnReg;
    public Symbol phiSymbol;

    public Set<IntermediateInstruction> availableExpressions;
    public boolean branchHandled;
    public String cmp;
    private boolean elimSilly;

    public IntermediateInstruction(SSAOperator operator, Operand operandOne, Operand operandTwo, int insNum, Type type){
        this.operator = operator; 
        this.operandOne = operandOne; 
        this.operandTwo = operandTwo;
        // this.insNum = insNum;
        this.elim = false;
        this.elimSilly = false;
        this.instNum = new InstructionNumber(insNum, type);
        this.liveVars = new HashSet<Operand>();
        this.returnReg = null;
        this.phiSymbol = null;

        this.availableExpressions = new HashSet<IntermediateInstruction>();
        if (this.isBranch()){
            branchHandled = false;
        }
        
    }

    @Override
    public String toString(){
        // The operands can be null (sometimes there are less than 2 operands)
        if (operandTwo == null && operandOne == null){ 
            return  operator + " " ;
        }
        else if (operandOne == null){
            return operator + " " + operandTwo.toString()  + " ";
        }
        else if (operandTwo == null){
            return operator + " " + operandOne.toString() + " ";
        }

        if (extraOperands == null){
            return  operator + " " + operandOne.toString() +  " " + operandTwo.toString()  + " ";
        }
        else{ 
            String retStr = operator + " " + operandOne.toString() + " " + operandTwo.toString();
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
    
    public void eliminateSilly(){
        elimSilly = true;
    }

    public boolean isElimSilly(){
        return elimSilly;
    }

    public void addExtraOperands(List<Operand> extraOperands){
        this.extraOperands = extraOperands;
    }

    public SSAOperator getOperator(){
        return operator;
    }

    // These branch-related methods are used in the pruning portion of the SSA construction to "repair" any branch instructions 
    public void updateBranchIns(BasicBlock BB){
        if (operandTwo == null){
            operandOne = BB;
        }
        else{ 
            operandTwo = BB;
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
    public Symbol getFunc(){
        if (extraOperands == null){
            if (operandTwo == null){
                if (operandOne instanceof Symbol){
                    return ((Symbol) operandOne);
                }
            }
            else{ 
                return ((Symbol) operandTwo);
            }
        }
        else{ 
            if (extraOperands.get(extraOperands.size() - 1) instanceof Symbol){
                return ((Symbol) extraOperands.get(extraOperands.size() - 1));
            }
        }
        return null;
    }

    public String getFuncName(){
        if (getFunc() != null) {
            return getFunc().name();
        }
        return "";
    }

    public List<Operand> getParams(){
        List<Operand> params = new ArrayList<Operand>(); 
        if (operandOne != null){
            params.add(operandOne);
        }
        if (operandTwo != null){
            params.add(operandTwo);
        }
        if (extraOperands != null){
            params.addAll(extraOperands);
        }

        params.remove(params.size() - 1);
        return params;
    }

    // public String getFuncName(){
    //     if (extraOperands == null){
    //         if (operandTwo == null){
    //             if (operandOne instanceof Symbol){
    //                 return ((Symbol) operandOne).name();
    //             }
    //         }
    //         else{ 
    //             return ((Symbol) operandTwo).name();
    //         }
    //     }
    //     else{ 
    //         if (extraOperands.get(extraOperands.size() - 1) instanceof Symbol){
    //             return ((Symbol) extraOperands.get(extraOperands.size() - 1)).name();
    //         }
    //     }
    //     return "";
    // }

    public InstructionNumber instNum(){
        return instNum;
    }
        
    public void setInsNum(int insNum){
        this.instNum = new InstructionNumber(insNum, instNum.type());
    }
    public int insNum(){
        return instNum.getInstructionNumber();
    }

    public Operand getOperandOne(){
        return operandOne;
    }

    public void setOperandOne(Operand newOp){
        this.operandOne = newOp;
    }

    public Operand getOperandTwo(){
        return operandTwo;
    }

    public List<Operand> getExtraOperands() {
        return extraOperands;
    }

    public void putOperator(SSAOperator operator){
        this.operator = operator;
    }

    public void putOperandOne(Operand operandOne){
        this.operandOne = operandOne;
    }

    public void putOperandTwo(Operand operandTwo){
        this.operandTwo = operandTwo ;
    }

    public HashSet<Operand> getLiveVars() {
        return liveVars;
    }
    
    public Integer getRegisterOne(){
        return this.registerOne; 
    }

    public Integer getRegisterTwo(){
        return this.registerTwo; 
    }

    public void putRegisterOne(Integer newReg){
        this.registerOne = newReg;
    }

    public void putRegisterTwo(Integer newReg){
        this.registerTwo = newReg;
    }

    public List<Integer> getExtraRegisters() {
        return this.extraRegisters;
    }

    public void addLiveVars(HashSet<Operand> liveVars) {
        this.liveVars.addAll(liveVars);
    }

    public static Boolean isConst(Operand opnd) {
        return (opnd instanceof BoolLiteral) || (opnd instanceof IntegerLiteral) || (opnd instanceof FloatLiteral);
    }
    
    public static Boolean isIntLit(Operand opnd) {
        return opnd instanceof IntegerLiteral;
    }

    public static Boolean isFloatLit(Operand opnd) {
        return opnd instanceof FloatLiteral;
    }

    public static Boolean isBoolLit(Operand opnd) {
        return opnd instanceof BoolLiteral;
    }

    public static Boolean numericOpndEquals(Operand opnd, int num) {
        return isIntLit(opnd) && ((IntegerLiteral) opnd).valueAsInt() == num || 
            isFloatLit(opnd) && ((FloatLiteral) opnd).valueAsFloat() == num;
    }

    public boolean containsOperand(Operand operand){
        if (operandOne != null){
            if (operandOne instanceof Symbol && operand instanceof Symbol){
                String operandOneName = ((Symbol) operandOne).name();
                operandOneName = operandOneName.substring(0, operandOneName.lastIndexOf("_"));

                String operandName = ((Symbol) operand).name();
                operandName = operandName.substring(0, operandName.lastIndexOf("_"));

                if(operandOneName.equals(operandName)){
                    return true;
                }
            }   
            else if (operandOne instanceof InstructionNumber && operand instanceof InstructionNumber){
                if(((InstructionNumber) operandOne).getInstructionNumber() == ((InstructionNumber) operand).getInstructionNumber()){
                    return true;
                }
            }   
        }
        if (operandTwo != null){
            if (operandTwo instanceof Symbol && operand instanceof Symbol){
                String operandTwoName = ((Symbol) operandTwo).name();
                if (operandTwoName.contains("_")){
                    operandTwoName = operandTwoName.substring(0, operandTwoName.lastIndexOf("_"));
                }

                String operandName = ((Symbol) operand).name();
                if (operandName.contains("_")){
                    operandName = operandName.substring(0, operandName.lastIndexOf("_"));
                }
                
                if(operandTwoName.equals(operandName)){
                    return true;
                }
            } 
            else if (operandTwo instanceof InstructionNumber && operand instanceof InstructionNumber){
                if(((InstructionNumber) operandTwo).getInstructionNumber() == ((InstructionNumber) operand).getInstructionNumber()){
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
        if (operandOne != null){
            if (operandOne instanceof Symbol){
                String operandOneName = ((Symbol) operandOne).name();
                if (operandOneName.contains("_")){
                    operandOneName = operandOneName.substring(0, operandOneName.lastIndexOf("_"));
                }

                String operandName = ((Symbol) matchingOperand).name();
                if (operandName.contains("_")){
                    operandName = operandName.substring(0, operandName.lastIndexOf("_"));
                }

                if(operandOneName.equals(operandName)){
                    operandOne = settingOperand;
                    return true;
                }
            }   
        }
        if (operandTwo != null){
            if (operandTwo instanceof Symbol){
                String operandTwoName = ((Symbol) operandTwo).name();
                if (operandTwoName.contains("_")){
                    operandTwoName = operandTwoName.substring(0, operandTwoName.lastIndexOf("_"));
                }

                String operandName = ((Symbol) matchingOperand).name();
                if (operandName.contains("_")){
                    operandName = operandName.substring(0, operandName.lastIndexOf("_"));
                }
                
                if(operandTwoName.equals(operandName)){
                    operandTwo = settingOperand;
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

    public static boolean checkOperand(Operand opOne, Operand opTwo){
        if (!opOne.getClass().toString().equals(opTwo.getClass().toString())){
            return true;
        }
        if (opTwo instanceof Symbol && opOne instanceof Symbol){
            String operandOneName = ((Symbol) opOne).name();
            if (operandOneName.contains("_")){
                operandOneName = operandOneName.substring(0, operandOneName.lastIndexOf("_"));
            }

            String operandName = ((Symbol) opTwo).name();
            if (operandName.contains("_")){
                operandName = operandName.substring(0, operandName.lastIndexOf("_"));
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

        // System.out.println(this.liveVars);
        // System.out.println(liveVars);
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
        if (intIns.getOperator() == operator && intIns.numberOperands() == this.numberOperands()){
            //System.out.println("one " + intIns);
            //System.out.println("two " + this);
            conflict = false; 
            if (operandOne != null && intIns.getOperandOne() != null){
                if (checkOperand(operandOne, intIns.getOperandOne())){
                    conflict = true;
                    //return true;
                }
                
            }
            if (operandTwo != null && intIns.getOperandTwo() != null){
                if (checkOperand(operandTwo, intIns.getOperandTwo())){
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

    public int numberOperands(){
        int number = 0;
        if (operandOne != null){
            number++;
        }
        if (operandTwo != null){
            number++; 
        }
        if (extraOperands != null){
            number += extraOperands.size();
        }
        return number;
    }

    public boolean operandOneIsFunc() {
        return operator == SSAOperator.CALL && numberOperands() == 1;
    }

    public boolean operandTwoIsFunc() {
        return operator == SSAOperator.CALL && numberOperands() == 2;
    }

    public void putExtraRegisters(List<Integer> extraRegisters){
        this.extraRegisters = extraRegisters;
    }
}
