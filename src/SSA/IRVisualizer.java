package SSA;

import java.util.List;

import SSA.BasicBlock.Transitions;

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
            enterBasicBlock(BB);
            for (IntermediateInstruction intIns: BB.getIntInsList()){
                dotGraph.append(currentLineNum + " : " + intIns.toString() + "|");

                currentLineNum++;
            }
            exitBasicBlock();

            for (Transitions transition : BB.transitionList){
                addTransition(transition);
            }
            BasicBlockCount++;
            dotGraph.append("\n");
        }
        dotGraph.append("}");
        return dotGraph.toString();
    }

    public void enterBasicBlock(BasicBlock BB){
        String BBLabel = "BB" + BB.BBNumber + "|{";
        if (BB.BBNumber == 1){
            BBLabel = BB.name() + "\\nBB1|{";
        }
        dotGraph.append("BB" + BB.BBNumber + "[shape=record, label=\"<b>" + BBLabel); 
    }

    public void exitBasicBlock(){
        dotGraph.delete(dotGraph.length() - 1, dotGraph.length());
        dotGraph.append("}\"];");
    }
    
    public void addTransition(Transitions transition){
        dotGraph.append("\nBB" + transition.fromBB.BBNumber + ":s -> BB" + transition.toBB.BBNumber + ":n [label=\"" + transition.label + "\"];");
    }
}
