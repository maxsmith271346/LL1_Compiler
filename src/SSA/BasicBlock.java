package SSA;

import java.util.ArrayList;
import java.util.List;

public class BasicBlock implements Operand {
    private List<IntermediateInstruction> IntermediateInstructionList; 
    private String BBName;
    public List<Transitions> transitionList;
    public int BBNumber = 0; 
    public static int insNumber = 0;

    public class Transitions{ 
        public BasicBlock fromBB, toBB;
        public String label; 
        public Transitions(BasicBlock from, BasicBlock to, String label){
            this.fromBB = from; 
            this.toBB = to; 
            this.label = label; 
        }
    }

    public BasicBlock(int BBNumber){
        IntermediateInstructionList = new ArrayList<IntermediateInstruction>();
        this.BBName = "";
        transitionList = new ArrayList<Transitions>();
        this.BBNumber = BBNumber;
    }

    public BasicBlock(int BBNumber, String name){
        IntermediateInstructionList = new ArrayList<IntermediateInstruction>();
        this.BBName = name;
        transitionList = new ArrayList<Transitions>();
        this.BBNumber = BBNumber;
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
