package SSA;

import java.util.List;

public class IRVisualizer {
    // this will contain the methods to generate the dot representation of the SSA IR
    private SSA ssa; 
    private StringBuilder dotGraph;
    private int BasicBlockCount;
    private int currentLineNum;

    public IRVisualizer(SSA ssa){
        dotGraph = new StringBuilder();
        this.ssa = ssa; 
        this.BasicBlockCount = 1;
        this.currentLineNum = 0;
    }

    public String generateDotGraph(){
        /*dotGraph.append("digraph G { \n");
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
        dotGraph.append("\n}");*/

        for (BasicBlock BB : ssa.getBasicBlockList()){
            for (IntermediateInstruction II: BB.getIntInsList()){
                dotGraph.append(II.toString() + "\n");
            }
        }

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
