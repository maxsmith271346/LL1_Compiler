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

        // Iterate through the BasicBlock List
        for (BasicBlock BB : ssa.getBasicBlockList()){
            
            enterBasicBlock(BB);
            for (IntermediateInstruction intIns: BB.getIntInsList()){
                
                // "Call" instructions need a "tag"
                if (intIns.getOperator().equals(SSAOperator.CALL)){
                    dotGraph.append("<c" + intIns.getFuncName() + intIns.instNum().getInstructionNumber() +  ">");
                }
                if (intIns.isElim()) {
                    dotGraph.append("elim: ");
                }
                //dotGraph.append(intIns.instNum().getInstructionNumber() + " : " + intIns.toString() + "" + intIns.getLiveVars() + "|");
                dotGraph.append(intIns.insNum() + " : "  + intIns.toString() + "|");
                currentLineNum++;
            }
            exitBasicBlock();

            // Iterate through the transitions for each basic block 
            for (Transitions transition : BB.transitionList){
                addTransition(transition, BB);
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
            BBLabel = BB.name() + "\\nBB" + BB.BBNumber + "|{";
        }
        dotGraph.append("BB" + BB.BBNumber + "[shape=record, label=\"<b>" + BBLabel); 
    }

    public void exitBasicBlock(){
        dotGraph.delete(dotGraph.length() - 1, dotGraph.length());
        dotGraph.append("}\"];");
    }
    
    public void addTransition(Transitions transition, BasicBlock BB){
        if (transition.label.contains("call")){
            dotGraph.append("\nBB" + BB.BBNumber + ":c" + transition.label.substring(4, transition.label.length())  +" -> BB" + transition.toBB.BBNumber + ":b [];");
        }
        else{ dotGraph.append("\nBB" + BB.BBNumber + ":s -> BB" + transition.toBB.BBNumber + ":n [label=\"" + transition.label + "\"];");}
    }
}
