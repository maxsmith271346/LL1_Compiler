package SSA;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import SSA.IntermediateInstruction.SSAOperator;
import ast.IntegerLiteral;
import pl434.Symbol;

public class BasicBlock implements Operand {
    private List<IntermediateInstruction> IntermediateInstructionList; 
    private String BBName;
    public List<Transitions> transitionList;    // Contains all the 'children' of the Basic Block
    public List<BasicBlock> inList;            // List of predecessors
    public int BBNumber = 0;                    // Keeps a running count of the number of Basic Blocks (used for identifying the BBs)
    public HashMap<Symbol, HashSet<Symbol>> varMap;      // Contains a map for each symbol where the value is a new symbol with subscript
    public static int insNumber = 0;            // Contains a running count of the instruction number
    public HashSet<Operand> lvEntry;
    public HashSet<Operand> lvExit;
    public Boolean inFunc;

    public Set<IntermediateInstruction> exitAvailableExpression;
    
    // This class is used to represent the children of a BB and their "relationship" to the parent BB (call, else, if, etc)
    public class Transitions{ 
        //public BasicBlock fromBB, toBB;
        public BasicBlock toBB;
        public String label; 
        public Transitions(BasicBlock to, String label){
            this.toBB = to; 
            this.label = label; 
        }
    }

    public BasicBlock(int BBNumber, HashMap<Symbol, HashSet<Symbol>> varMap, Boolean inFunc){
        IntermediateInstructionList = new ArrayList<IntermediateInstruction>();
        transitionList = new ArrayList<Transitions>();
        this.BBNumber = BBNumber;
        this.varMap = new HashMap<Symbol, HashSet<Symbol>>();
        for (Symbol key : varMap.keySet()){
            HashSet<Symbol> varSet = new HashSet<Symbol>(varMap.get(key));
            for (Symbol s : varSet){
                if (s.name().contains("-2")){
                    varSet.remove(s);
                }
            }
            this.varMap.put(key, varSet);
        }
        this.BBName = "";
        this.inList = new ArrayList<BasicBlock>();
        this.lvEntry = new HashSet<Operand>();
        this.lvExit = new HashSet<Operand>();
        this.exitAvailableExpression = new HashSet<IntermediateInstruction>();
        this.inFunc = inFunc;
    }

    public BasicBlock(int BBNumber, HashMap<Symbol, HashSet<Symbol>> varMap, String name, Boolean inFunc){
        this(BBNumber, varMap, inFunc);
        this.BBName = name;
    }

    public void addBasicBlockName(String name){
        this.BBName = name;
    }

    public void addMap(HashMap<Symbol, HashSet<Symbol>> varMap){
        for (Symbol key : varMap.keySet()){
            HashSet<Symbol> varSet = new HashSet<Symbol>(varMap.get(key));
            for (Symbol s : varSet){
                if (s.name().contains("-2")){
                    varSet.remove(s);
                }
            }
            this.varMap.put(key, varSet);
        }
    }

    public void addFullMap(HashMap<Symbol, HashSet<Symbol>> varMap){
        for (Symbol key : varMap.keySet()){
            HashSet<Symbol> varSet = new HashSet<Symbol>(varMap.get(key));
            this.varMap.put(key, varSet);
        }
    }
    
    /**
     * Adds an IntermediateInstruction to the list and returns the current instruction number
     * 
     * @param intIns new IntermediateInstruction to add
     * @return current instruction number
    */
    public InstructionNumber add(IntermediateInstruction intIns){
        if (intIns.getOperator() != SSAOperator.PHI){
            if (intIns.getOperandOne() != null){
                if (intIns.getOperandOne() instanceof Symbol){
                    Symbol operandOneSymbol = (Symbol) intIns.getOperandOne();
                    String opOneName = (operandOneSymbol).name();
                    if (opOneName.contains("-1") || opOneName.contains("-2")){
                        System.out.println("warning: variable " + opOneName.substring(0, opOneName.indexOf("_")) + " has not been initialized!");
                        Symbol newSymbol = new Symbol(operandOneSymbol.name().substring(0, opOneName.indexOf("_")) + "_" + insNumber, operandOneSymbol.type().toString(), "var", operandOneSymbol.scope);
                        intIns.putOperandOne(newSymbol);
                        this.IntermediateInstructionList.add(new IntermediateInstruction(SSAOperator.MOVE, new IntegerLiteral(0, 0, "0"), newSymbol, insNumber));
                        insNumber++; 
                        // update varMap
                        HashSet<Symbol> newHash = new HashSet<Symbol>();
                        newHash.add(newSymbol);
                        for(Symbol s : varMap.keySet()){
                            //update varMap:
                            if (s.name().equals(opOneName.substring(0, opOneName.indexOf("_")))){
                                varMap.put(s, newHash);
                            }
                        }
                    }  
                }
            }
            if (intIns.getOperator() != SSAOperator.MOVE){
                if (intIns.getOperandTwo() != null){
                    if (intIns.getOperandTwo() instanceof Symbol){
                        Symbol operandTwoSymbol = (Symbol) intIns.getOperandTwo();
                        String opTwoName = (operandTwoSymbol).name();
                        if (opTwoName.contains("-1") || opTwoName.contains("-2")){
                            System.out.println("warning: variable " + opTwoName.substring(0, opTwoName.indexOf("_")) + " has not been initialized!");
                            Symbol newSymbol = new Symbol(operandTwoSymbol.name().substring(0, opTwoName.indexOf("_")) + "_" + insNumber, operandTwoSymbol.type().toString(), "var", operandTwoSymbol.scope);
                            intIns.putOperandTwo(newSymbol);
                            this.IntermediateInstructionList.add(new IntermediateInstruction(SSAOperator.MOVE, new IntegerLiteral(0, 0, "0"), newSymbol, insNumber));
                            insNumber++;
                            // update varMap
                            HashSet<Symbol> newHash = new HashSet<Symbol>();
                            newHash.add(newSymbol);
                            for(Symbol s : varMap.keySet()){
                                //update varMap:
                                if (s.name().equals(opTwoName.substring(0, opTwoName.indexOf("_")))){
                                    varMap.put(s, newHash);
                                }
                            }
                        }  
                    }
                }   
            }    
        }
        
        this.IntermediateInstructionList.add(intIns);
        insNumber++;
        return intIns.instNum();
    }
    
    public int addFront(IntermediateInstruction intIns){
        this.IntermediateInstructionList.add(0, intIns);
        return insNumber++;
    }

    public void addInEdge(BasicBlock bb) {
        inList.add(bb);
    }

    public List<BasicBlock> getInList() {
        return inList;
    }

    public ArrayList<BasicBlock> getOutList() {
        ArrayList<BasicBlock> outList = new ArrayList<>();
        for (Transitions t : this.transitionList) {
            if (!t.label.contains("call")) {
                outList.add(t.toBB);
            }
        }
        return outList;
    }

    /*public void addFront(IntermediateInstruction intIns) {
        this.IntermediateInstructionList.add(0, intIns);
    }*/
    

    public List<IntermediateInstruction> getIntInsList(){
        return IntermediateInstructionList;
    }

    public int size(){
        return IntermediateInstructionList.size();
    }

    public String name(){
        if(BBName != null){
            return BBName;
        }
        return "";
    }

    public void putName(String name){
        this.BBName = name;
    }

    @Override 
    public String toString(){
        return "[" + (BBNumber) + "]";
    }

    public void resolveVars(HashMap<Symbol, HashSet<Symbol>> varMap){
        for (Symbol var : varMap.keySet()){
            for (IntermediateInstruction ii : IntermediateInstructionList){
                if (ii.getOperator() != SSAOperator.MOVE && ii.getOperator() != SSAOperator.PHI){
                    if (ii.getOperandOne() != null){
                        if (ii.getOperandOne().toString().contains(var.name())){
                            ii.putOperandOne(varMap.get(var).iterator().next());
                        }
                    }
                    if (ii.getOperandTwo() != null){
                        if (ii.getOperandTwo().toString().contains(var.name())){
                            ii.putOperandTwo(varMap.get(var).iterator().next());
                        }
                    }
                }
            }
        }
        
        for (Transitions t : transitionList){
           // System.out.println(t.toBB);
            for (Symbol var : varMap.keySet()){
                for (IntermediateInstruction ii : t.toBB.IntermediateInstructionList){
                    if (ii.getOperator() != SSAOperator.MOVE && ii.getOperator() != SSAOperator.PHI){
                        if (ii.getOperandOne() != null){
                            if (ii.getOperandOne().toString().contains(var.name())){
                                ii.putOperandOne(varMap.get(var).iterator().next());
                            }
                        }
                        if (ii.getOperandTwo() != null){
                            if (ii.getOperandTwo().toString().contains(var.name())){
                                ii.putOperandTwo(varMap.get(var).iterator().next());
                            }
                        }
                    }
                }
            }
        }
    }


    
    // return whether or not live sets were changed
    public Boolean liveAnalysis() {
        Boolean change = false;
        HashSet<Operand> origEntry = new HashSet<Operand>(this.lvEntry);
        HashSet<Operand> origExit = new HashSet<Operand>(this.lvExit);

        // Add all successor lvEntry sets to this's lvExit
        for (BasicBlock successor : this.getOutList()) {
            if (successor.name().contains("elim")){continue;}
            this.lvExit.addAll(successor.lvEntry);
        }
        
        

        HashSet<Operand> live = new HashSet<Operand>(this.lvExit);
        // determine liveness at entry and exit to BB
        for (int i = this.getIntInsList().size()-1; i >= 0; i--) {
            IntermediateInstruction ii = this.getIntInsList().get(i);
            if (ii.isElim()){continue;}          
            switch (ii.getOperator()) {
                case ADDA:
                    break;
                case BEQ:
                case BGE:
                case BGT:
                case BLE:
                case BLT:
                case BNE:
                    if (ii.getOperandOne() != null) {
                        live.add(ii.getOperandOne());
                    }
                    break;

                case CALL:  // TODO:
                    if (ii.getFunc().type().toString() != "void" && live.contains(ii.instNum())) {
                        live.remove(ii.instNum());
                    }
                    // for (Transitions t : this.transitionList) {
                    //     BasicBlock successor = t.toBB;
                    //     if (t.label.contains("call") && successor.name().equals(ii.getOperandOne().toString())) {
                    //         for (Operand o : successor.lvEntry){
                    //             if ((o instanceof Symbol) && ((Symbol) o).scope == 1){
                    //                 live.add(o);
                    //             }
                    //         }
                    //     }
                    // }
                    break;

                case RET:
                    if (ii.getOperandOne() != null) {
                        live.add(ii.getOperandOne());
                    }
                    for (Symbol s : varMap.keySet()){
                        if (s.scope == 1 && s != null){
                            live.add(s);
                        }
                    }
                    break;

                case END:
                    break;
                case LOAD:
                    break;

                case NEG:
                    if (live.contains(ii.instNum())) {
                        live.remove(ii.instNum());
                    }
                    if (ii.getOperandOne() != null) {
                        live.add(ii.getOperandOne());
                    }
                    break;

                case NONE:
                    break;

                case NOT:
                    if (live.contains(ii.instNum())) {
                        live.remove(ii.instNum());
                    }
                    if (ii.getOperandOne() != null) {
                        live.add(ii.getOperandOne());
                    }
                    break;
                case STORE:
                    break;

                case PHI:
                    String varName = ii.getOperandOne().toString();
                    Operand op = null;
                    varName = varName.split("_")[0];
                    varName = varName + "_" + ii.instNum().getInstructionNumber();
                    for (Operand opnd : live) {
                        if (opnd != null && opnd.toString().equals(varName)) {
                            op = opnd;
                            break;
                        }
                    }
                    live.remove(op);
                    // if (live.contains(ii.instNum())) {
                    //     live.remove(ii.instNum());
                    // }
                    if (ii.getOperandOne() != null) {
                        live.add(ii.getOperandOne());
                    }
                    if (ii.getOperandOne() != null) {
                        live.add(ii.getOperandTwo());
                    }
                    break;

                case ADD:
                case AND:
                case CMP:
                case DIV:
                case MOD:
                case MUL:
                case OR:
                case POW:
                case SUB:
                    if (live.contains(ii.instNum())) {
                        live.remove(ii.instNum());
                    }
                    if (ii.getOperandOne() != null) {
                        live.add(ii.getOperandOne());
                    }
                    if (ii.getOperandOne() != null) {
                        live.add(ii.getOperandTwo());
                    }                    
                    break;

                case READ:
                case READ_B:
                case READ_F:
                    if (live.contains(ii.instNum())) {
                        live.remove(ii.instNum());
                    }
                    break;

                case WRITE:
                case WRITE_B:
                case WRITE_F:
                    if (ii.getOperandOne() != null) {
                        live.add(ii.getOperandOne());
                    }
                    break;

                case MOVE:
                    if (live.contains(ii.getOperandTwo())) {
                        live.remove(ii.getOperandTwo());
                    }
                    if (ii.getOperandOne() != null) {
                        live.add(ii.getOperandOne());
                    }
                    break;
                
                default:
                    break; 
            }

            if (i == this.getIntInsList().size()-1 && i > 0) {
                change |= this.getIntInsList().get(i).setLiveVars(new HashSet<Operand>(lvExit));
            }
            if (i != 0) {
                change |= this.getIntInsList().get(i-1).setLiveVars(new HashSet<Operand>(live));
            }
            
        }
        this.lvEntry = live;
        
        
        return change;
    }


}


