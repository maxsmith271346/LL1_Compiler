package SSA;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pl434.Symbol;

public class BasicBlock implements Operand {
    private List<IntermediateInstruction> IntermediateInstructionList; 
    private String BBName;
    public List<Transitions> transitionList;    // Contains all the 'children' of the Basic Block
    public int BBNumber = 0;                    // Keeps a running count of the number of Basic Blocks (used for identifying the BBs)
    public HashMap<Symbol, Symbol> varMap;      // Contains a map for each symbol where the value is a new symbol with subscript
    public static int insNumber = 0;            // Contains a running count of the instruction number

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

    public BasicBlock(int BBNumber, HashMap<Symbol, Symbol> varMap){
        IntermediateInstructionList = new ArrayList<IntermediateInstruction>();
        transitionList = new ArrayList<Transitions>();
        this.BBNumber = BBNumber;
        this.varMap = new HashMap<Symbol, Symbol>();
        for (Symbol key : varMap.keySet()){
            this.varMap.put(key, varMap.get(key));
        }
        this.BBName = "";
    }

    public BasicBlock(int BBNumber, HashMap<Symbol, Symbol> varMap, String name){
        this(BBNumber, varMap);
        this.BBName = name;
    }

    public void addBasicBlockName(String name){
        this.BBName = name;
    }
    
    /**
     * Adds an IntermediateInstruction to the list and returns the current instruction number
     * 
     * @param intIns new IntermediateInstruction to add
     * @return current instruction number
    */
    public int add(IntermediateInstruction intIns){
        this.IntermediateInstructionList.add(intIns);
        return insNumber++;
    }

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

    @Override 
    public String toString(){
        return "[" + (BBNumber) + "]";
    }

}
