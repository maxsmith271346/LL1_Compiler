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
import types.*;

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
    public Symbol function;

    public Set<IntermediateInstruction> exitAvailableExpression;
    
    // This class is used to represent the children of a BB and their "relationship" to the parent BB (call, else, if, etc)
    public class Transitions{ 
        //public BasicBlock fromBB, toBB;
        public BasicBlock toBB;
        public String label; 
        public Boolean backEdge;
        public Transitions(BasicBlock to, String label){
            this.toBB = to; 
            this.label = label; 
            backEdge = false;
        }
        public Transitions(BasicBlock to, String label, boolean backEdge){
            this(to, label);
            this.backEdge = backEdge;
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
        this.function = null;
    }

    public BasicBlock(int BBNumber, HashMap<Symbol, HashSet<Symbol>> varMap, String name,Symbol func,  Boolean inFunc){
        this(BBNumber, varMap, inFunc);
        this.BBName = name;
        this.function = func;
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
        IntermediateInstruction uninitMoveIns = createMoveForUninitializedVars(intIns, SSA.inFunc);
        if (uninitMoveIns != null){
            //this.IntermediateInstructionList.add(uninitMoveIns);
            this.addFront(uninitMoveIns);
            //insNumber++;
        }
        this.IntermediateInstructionList.add(intIns);
        intIns.setInsNum(insNumber);
        insNumber++;
        return intIns.instNum();
    }
    
    public int addFront(IntermediateInstruction intIns){
        this.IntermediateInstructionList.add(0, intIns);
        return insNumber++;
    }

    public int addEnd(IntermediateInstruction intIns, boolean checkUnit){
        IntermediateInstruction uninitMoveIns = null;
        if (checkUnit){
            uninitMoveIns = createMoveForUninitializedVars(intIns, SSA.inFunc);
        }
        
        int index = this.IntermediateInstructionList.size();
        if (this.IntermediateInstructionList.get(index - 1).isBranch()){
            index--;
        }

        if (uninitMoveIns != null){
            this.IntermediateInstructionList.add(index, uninitMoveIns);
            index++;
            insNumber++;
        }
        this.IntermediateInstructionList.add(index, intIns);
        return insNumber++;
    }


    public IntermediateInstruction createMoveForUninitializedVars(IntermediateInstruction intIns, boolean inFunc){
        if (intIns.getOperator() != SSAOperator.PHI){
            if (intIns.getOperandOne() != null){
                if (intIns.getOperandOne() instanceof Symbol){
                    Symbol operandOneSymbol = (Symbol) intIns.getOperandOne();
                    String opOneName = (operandOneSymbol).name();
                    if ((opOneName.contains("-1") && !inFunc)|| opOneName.contains("-2")){
                        //System.out.println("intIns " + intIns);
                        System.out.println("warning: variable " + opOneName.substring(0, opOneName.lastIndexOf("_")) + " has not been initialized!");
                        Symbol newSymbol = new Symbol(operandOneSymbol.name().substring(0, opOneName.lastIndexOf("_")) + "_" + insNumber, operandOneSymbol.type().toString(), "var", operandOneSymbol.scope);
                        intIns.putOperandOne(newSymbol);
                        //this.IntermediateInstructionList.add(new IntermediateInstruction(SSAOperator.MOVE, new IntegerLiteral(0, 0, "0"), newSymbol, insNumber));
                        //insNumber++; 
                        // update varMap
                        HashSet<Symbol> newHash = new HashSet<Symbol>();
                        newHash.add(newSymbol);
                        for(Symbol s : varMap.keySet()){
                            //update varMap:
                            if (s.name().equals(opOneName.substring(0, opOneName.lastIndexOf("_")))){
                                varMap.put(s, newHash);
                            }
                        }

                        return new IntermediateInstruction(SSAOperator.MOVE, new IntegerLiteral(0, 0, "0"), newSymbol, insNumber, new VoidType());
                    }  
                }
            }
            if (intIns.getOperator() != SSAOperator.MOVE){
                if (intIns.getOperandTwo() != null){
                    if (intIns.getOperandTwo() instanceof Symbol){
                        Symbol operandTwoSymbol = (Symbol) intIns.getOperandTwo();
                        String opTwoName = (operandTwoSymbol).name();
                        if ((opTwoName.contains("-1") && !inFunc) || opTwoName.contains("-2")){
                            //System.out.println("intIns " + intIns);
                            System.out.println("warning: variable " + opTwoName.substring(0, opTwoName.lastIndexOf("_")) + " has not been initialized!");
                            Symbol newSymbol = new Symbol(operandTwoSymbol.name().substring(0, opTwoName.lastIndexOf("_")) + "_" + insNumber, operandTwoSymbol.type().toString(), "var", operandTwoSymbol.scope);
                            intIns.putOperandTwo(newSymbol);
                            //this.IntermediateInstructionList.add(new IntermediateInstruction(SSAOperator.MOVE, new IntegerLiteral(0, 0, "0"), newSymbol, insNumber));
                            //insNumber++;
                            // update varMap
                            HashSet<Symbol> newHash = new HashSet<Symbol>();
                            newHash.add(newSymbol);
                            for(Symbol s : varMap.keySet()){
                                //update varMap:
                                if (s.name().equals(opTwoName.substring(0, opTwoName.lastIndexOf("_")))){
                                    varMap.put(s, newHash);
                                }
                            }

                            return new IntermediateInstruction(SSAOperator.MOVE, new IntegerLiteral(0, 0, "0"), newSymbol, insNumber, new VoidType());
                        }  
                    }
                }   
            }    
        }
        return null;
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
                        if (ii.getOperandOne().toString().contains(var.name()) && ii.getOperandOne().toString().charAt(0) == var.name().charAt(0)){
                            ii.putOperandOne(varMap.get(var).iterator().next());
                        }
                    }
                    if (ii.getOperandTwo() != null){
                        if (ii.getOperandTwo().toString().contains(var.name()) && ii.getOperandTwo().toString().charAt(0) == var.name().charAt(0)){
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
                            if (ii.getOperandOne().toString().contains(var.name()) && ii.getOperandOne().toString().charAt(0) == var.name().charAt(0)){
                                ii.putOperandOne(varMap.get(var).iterator().next());
                            }
                        }
                        if (ii.getOperandTwo() != null){
                            if (ii.getOperandTwo().toString().contains(var.name()) && ii.getOperandTwo().toString().charAt(0) == var.name().charAt(0)){
                                ii.putOperandTwo(varMap.get(var).iterator().next());
                            }
                        }
                    }
                }
            }
        }
    }



    public Boolean hasInsNum(int insNum){
        for (IntermediateInstruction ii : IntermediateInstructionList){
            if (ii.insNum() == insNum){
                return true;
            }
        }
        return false;
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
            if (ii.isElim()){
                if (i == this.getIntInsList().size()-1 && i >= 0) {
                    change |= this.getIntInsList().get(i).setLiveVars(new HashSet<Operand>(lvExit));
                }
                if (i != 0) {
                    change |= this.getIntInsList().get(i-1).setLiveVars(new HashSet<Operand>(live));
                }
                continue;
            }       
         
            switch (ii.getOperator()) {
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
                    live.addAll(ii.getParams());            
                    if (ii.getFunc().type().toString() != "void" && live.contains(ii.instNum())) {
                        live.remove(ii.instNum());
                    }
                    /*for (Symbol s : this.varMap.keySet()){
                        if (s.scope == 1){
                            live.addAll(varMap.get(s));
                        }
                    }*/
                    live.addAll(getPreviousGlobalVars(i));
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
                    live.add(ii.instNum());
                    break;

                case END:
                    break;
                case LOAD:
                case NONE:// this used to be a break statement 
                case NEG:
                    if (live.contains(ii.instNum())) {
                        live.remove(ii.instNum());
                    }
                    if (ii.getOperandOne() != null) {
                        live.add(ii.getOperandOne());
                    }
                    break;

                case NOT:
                    if (live.contains(ii.instNum())) {
                        live.remove(ii.instNum());
                    }
                    if (ii.getOperandOne() != null) {
                        live.add(ii.getOperandOne());
                    }
                    break;
                //case STORE:
                //    break;

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
                case STORE:
                case ADDA:
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

                case READ_I:
                case READ_B:
                case READ_F:
                    if (live.contains(ii.instNum())) {
                        live.remove(ii.instNum());
                    }
                    break;

                case WRITE_I:
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

            if (i == this.getIntInsList().size()-1 && i >= 0) {
                change |= this.getIntInsList().get(i).setLiveVars(new HashSet<Operand>(lvExit));
            }
            if (i != 0) {
                change |= this.getIntInsList().get(i-1).setLiveVars(new HashSet<Operand>(live));
            }
            
        }
        this.lvEntry = live;
        
        
        return change;
    }
    
    public Set<Operand> getPreviousGlobalVars(int i ){
        Set<Operand> newSet = new HashSet<Operand>();
        Boolean found = true;
        for (int j = 0; j < i; j++){
            if (this.getIntInsList().get(j).getOperandOne() != null){
                if (this.getIntInsList().get(j).getOperandOne() instanceof Symbol){
                    if (((Symbol) this.getIntInsList().get(j).getOperandOne()).scope == 1 && !((Symbol) this.getIntInsList().get(j).getOperandOne()).getSymbolType().equals("func")){
                        Set<Operand> toRemove = new HashSet<Operand>();
                        Set<Operand> toAdd = new HashSet<Operand>();
                        found = false;
                        for (Operand o : newSet){
                            if (getBaseString(o).equals(getBaseString(this.getIntInsList().get(j).getOperandOne()))){
                               // System.out.println("one " + o + " two " + this.getIntInsList().get(j).getOperandOne());
                                if (getNumber(o) < getNumber(this.getIntInsList().get(j).getOperandOne())){
                                    toRemove.add(o);
                                    toAdd.add(this.getIntInsList().get(j).getOperandOne());
                                }
                                found = true;
                            }
                        }
                        if (!found){
                            newSet.add(this.getIntInsList().get(j).getOperandOne());
                        }

                        newSet.addAll(toAdd);
                        newSet.removeAll(toRemove);
                        //System.out.println("new Set " + newSet );
                    }
                }
            }

            if (this.getIntInsList().get(j).getOperandTwo() != null){
                if (this.getIntInsList().get(j).getOperandTwo() instanceof Symbol ){
                    if (((Symbol) this.getIntInsList().get(j).getOperandTwo()).scope == 1 && !((Symbol) this.getIntInsList().get(j).getOperandTwo()).getSymbolType().equals("func")){
                        Set<Operand> toRemove = new HashSet<Operand>();
                        Set<Operand> toAdd = new HashSet<Operand>();
                        found = false;
                        for (Operand o : newSet){
                            if (getBaseString(o).equals(getBaseString(this.getIntInsList().get(j).getOperandTwo()))){
                                //System.out.println("one " + o + " two " + this.getIntInsList().get(j).getOperandTwo());

                                if (getNumber(o) < getNumber(this.getIntInsList().get(j).getOperandTwo())){
                                    toRemove.add(o);
                                    toAdd.add(this.getIntInsList().get(j).getOperandTwo());
                                }
                                found = true;
                            }
                        }
                        if (!found){
                            newSet.add(this.getIntInsList().get(j).getOperandTwo());
                        }
                        newSet.addAll(toAdd);
                        newSet.removeAll(toRemove);
                       // System.out.println("new Set " + newSet );
                    }
                }
            } 
        }

        return newSet;
    }

    public String getBaseString(Operand o){
        if (o instanceof Symbol && o.toString().contains("_")){
            return o.toString().substring(0, o.toString().lastIndexOf("_"));
        }
        return o.toString();
    }

    public int getNumber(Operand o ){
        if (o instanceof Symbol && o.toString().contains("_")){
            return Integer.parseInt(o.toString().substring(o.toString().lastIndexOf("_") + 1, o.toString().length()));
        }
        return 0;
    }
}


