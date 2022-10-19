package SSA;

import java.util.List;

public class IRVisualizer {
    // this will contain the methods to generate the dot representation of the SSA IR
    private List<IntermediateInstruction> SSA; 
    private StringBuilder dotGraph;
    private int BasicBlockCount;
    private int currentLineNum;

    public IRVisualizer(List<IntermediateInstruction> SSA){
        dotGraph = new StringBuilder();
        this.SSA = SSA; 
        this.BasicBlockCount = 1;
        this.currentLineNum = 0;
    }

    public String generateDotGraph(){
        dotGraph.append("digraph G { \n");
        dotGraph.append(enterBasicBlock()); // enter basic block at the beginning
        for (IntermediateInstruction intIns : SSA){ // need to add conditional to enter basic block each time a branch is found? 
            if (currentLineNum != SSA.size() - 1){
                dotGraph.append(currentLineNum + " : " + intIns.toString() + "|");
            }
            else{ 
                dotGraph.append(currentLineNum + " : " + intIns.toString());
            }
            
            currentLineNum++;
        }
        dotGraph.append(exitBasicBlock());
        dotGraph.append("\n}");

        return dotGraph.toString();
    }

    public String enterBasicBlock(){
        String BBLabel = "BB" + BasicBlockCount + "|{";
        if (BasicBlockCount == 1){
            BBLabel = "main\\nBB1|{";
        }
        return "BB" + BasicBlockCount + "[shape=record, label=\"<b>" + BBLabel; 
    }

    public String exitBasicBlock(){
        return "}\"];";
    }
}
