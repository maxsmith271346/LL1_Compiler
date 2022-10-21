package SSA;

import java.util.ArrayList;
import java.util.List;

public class BasicBlock {
    private List<IntermediateInstruction> IntermediateInstructionList; 
    private String BBName;
    public static int BBNumber = 0; 
    public static int insNumber = 0;

    public BasicBlock(){
        IntermediateInstructionList = new ArrayList<IntermediateInstruction>();
        this.BBName = "";
    }

    public BasicBlock(String name){
        IntermediateInstructionList = new ArrayList<IntermediateInstruction>();
        this.BBName = name;
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
        return BBName;
    }
}
