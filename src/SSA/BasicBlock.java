package SSA;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import SSA.IntermediateInstruction.SSAOperator;
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

    public BasicBlock(int BBNumber, HashMap<Symbol, HashSet<Symbol>> varMap){
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
    }

    public BasicBlock(int BBNumber, HashMap<Symbol, HashSet<Symbol>> varMap, String name){
        this(BBNumber, varMap);
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
            outList.add(t.toBB);
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
            this.lvExit.addAll(successor.lvEntry);
        }
        
        

        HashSet<Operand> live = new HashSet<Operand>(this.lvExit);
        // determine liveness at entry and exit to BB
        for (int i = this.getIntInsList().size()-1; i >= 0; i--) {
            IntermediateInstruction ii = this.getIntInsList().get(i);            
            switch (ii.getOperator()) {
                case ADDA:
                    break;
                
                
                case BEQ:
                case BGE:
                case BGT:
                case BLE:
                case BLT:
                case BNE:
                    live.add(ii.getOperandOne());
                    // change = true;
                    break;

                case CALL:  // TODO:

                    break;
                case RET:
                    break;
                case END:
                    break;
                case LOAD:
                    break;
                case NEG:
                    break;
                case NONE:
                    break;
                case NOT:
                    break;
                case STORE:
                    break;

                case PHI:
                    String varName = ii.getOperandOne().toString();
                    Operand op = null;
                    varName = varName.split("_")[0];
                    varName = varName + "_" + ii.instNum().getInstructionNumber();
                    for (Operand opnd : live) {
                        if (opnd.toString().equals(varName)) {
                            op = opnd;
                            break;
                        }
                    }
                    live.remove(op);
                    // if (live.contains(ii.instNum())) {
                    //     live.remove(ii.instNum());
                    // }
                    live.add(ii.getOperandOne());
                    live.add(ii.getOperandTwo());
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
                    live.add(ii.getOperandOne());
                    live.add(ii.getOperandTwo());
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
                    live.add(ii.getOperandOne());
                    break;

                case MOVE:
                    if (live.contains(ii.getOperandTwo())) {
                        live.remove(ii.getOperandTwo());
                    }
                    live.add(ii.getOperandOne());
                    break;
                
                default:
                    break; 
            }
            // System.out.println(BBNumber);
            // System.out.println(lvEntry);
            // System.out.println(lvExit + "\n");
            System.out.println(ii.instNum().getInstructionNumber());
            // System.out.println(ii.liveVars);
            // ii.setLiveVars(new HashSet<Operand>(live));

            if (i == this.getIntInsList().size()-1 && i > 0) {
                // System.out.println(origExit);
                // System.out.println(lvExit);
                // System.out.println(origEntry);
                // System.out.println(lvEntry);
                change |= this.getIntInsList().get(i).setLiveVars(new HashSet<Operand>(lvExit));
                System.out.println("First: " + change);
            }
            if (i != 0) {
                change |= this.getIntInsList().get(i-1).setLiveVars(new HashSet<Operand>(live));
                System.out.println("Second: " + change);
            }
            
        }
        this.lvEntry = live;
        
        return change;
    }


}


