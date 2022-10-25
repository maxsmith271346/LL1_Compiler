package SSA;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pl434.Symbol;

public class BasicBlock implements Operand {
    private List<IntermediateInstruction> IntermediateInstructionList; 
    private String BBName;
    public List<Transitions> transitionList;
    public int BBNumber = 0; 
    public HashMap<Symbol, Symbol> varMap;
    public static int insNumber = 0;

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
        this.BBName = "";
        transitionList = new ArrayList<Transitions>();
        this.BBNumber = BBNumber;
        this.varMap = varMap;
    }

    public BasicBlock(int BBNumber, HashMap<Symbol, Symbol> varMap, String name){
        IntermediateInstructionList = new ArrayList<IntermediateInstruction>();
        this.BBName = name;
        transitionList = new ArrayList<Transitions>();
        this.BBNumber = BBNumber;
        this.varMap = varMap;
    }

    public void addBasicBlockName(String name){
        this.BBName = name;
    }
    
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
