package SSA;

import java.util.List;

import SSA.BasicBlock.Transitions;
import SSA.IntermediateInstruction.SSAOperator;

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
                if (intIns.getOperator().equals(SSAOperator.CALL)){
                    dotGraph.append("<c" + intIns.getFuncName() + ">");
                }
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
        if (!BB.name().equals("")){
            BBLabel = BB.name() + "\\nBB1|{";
        }
        dotGraph.append("BB" + BB.BBNumber + "[shape=record, label=\"<b>" + BBLabel); 
    }

    public void exitBasicBlock(){
        dotGraph.delete(dotGraph.length() - 1, dotGraph.length());
        dotGraph.append("}\"];");
    }
    
    public void addTransition(Transitions transition){
        if (transition.label.contains("call")){
            dotGraph.append("\nBB" + transition.fromBB.BBNumber + ":c" + transition.label.substring(4, transition.label.length())  +" -> BB" + transition.toBB.BBNumber + ":b [];");
        }
        else{ dotGraph.append("\nBB" + transition.fromBB.BBNumber + ":s -> BB" + transition.toBB.BBNumber + ":n [label=\"" + transition.label + "\"];");}
    }
}