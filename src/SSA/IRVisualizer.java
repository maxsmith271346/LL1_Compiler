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
        dotGraph.append("digraph G { \n");
        for (BasicBlock BB : ssa.getBasicBlockList()){
            enterBasicBlock(BB.name());
            for (IntermediateInstruction intIns: BB.getIntInsList()){
                if (currentLineNum != BB.size() - 1){
                    dotGraph.append(currentLineNum + " : " + intIns.toString() + "|");
                }
                else{ 
                    dotGraph.append(currentLineNum + " : " + intIns.toString());
                }
                
                currentLineNum++;
            }
            exitBasicBlock();
        }
        dotGraph.append("\n}");
        return dotGraph.toString();
    }

    public void enterBasicBlock(String name){
        String BBLabel = "BB" + BasicBlockCount + "|{";
        if (BasicBlockCount == 1){
            BBLabel = name + "\\nBB1|{";
        }
        dotGraph.append("BB" + BasicBlockCount + "[shape=record, label=\"<b>" + BBLabel); 
    }

    public void exitBasicBlock(){
        dotGraph.append("}\"];");
    }
}
