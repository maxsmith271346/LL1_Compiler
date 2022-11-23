package pl434;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import SSA.*;
import SSA.BasicBlock.Transitions;
import SSA.IntermediateInstruction.SSAOperator;
import types.VoidType;

public class RegisterAllocator {
    public SSA ssa;
    public int numRegs;
    HashMap<Operand, Set<Operand>> interferenceGraph;
    HashMap<Operand, Integer> colorMap;
    List<Integer> allColors;
    //int spillOffset;

    public RegisterAllocator(SSA ssa, int numRegs){
        this.ssa = ssa;
        this.numRegs = numRegs;
        interferenceGraph = new HashMap<Operand, Set<Operand>>();
        colorMap = new HashMap<Operand, Integer>();
        allColors = new ArrayList<Integer>();
        //spillOffset = -4;

        allColors = IntStream.rangeClosed(1, numRegs - 2).boxed().collect(Collectors.toList());
        eliminatePhis();

        // need to clear out the previous liveness analysis because the graph has been changed after elimniating the phis
        

        buildInterferenceGraph();
        colorInterferenceGraph();
        
        /*for (Operand o : colorMap.keySet()){
            System.out.println("o " + o + " color " + colorMap.get(o));
        }*/
        
        insertRegisters();
        removeSillyMoves();
    }

    public void buildInterferenceGraph(){
        generateLivenessAnalysis();
        // based on the liveness analysis, build an interference graph 
        //System.out.println("liveness analysis: " + ssa.asDotGraph());

       //System.out.println("after liveness analysis " + ssa.asDotGraph());
        for (BasicBlock bb : ssa.getBasicBlockList()){
            if (bb.name().contains("elim")){continue;}
            for (IntermediateInstruction ii : bb.getIntInsList()){
                if (ii.isElim()){continue;}
                for (Operand o : ii.liveVars){
                    if (IntermediateInstruction.isConst(o)){continue;}
                    Set<Operand> adjSet;
                    if (interferenceGraph.containsKey(o)){
                        adjSet = interferenceGraph.get(o);
                    }
                    else{ 
                        adjSet = new HashSet<Operand>();
                    }
                    for (Operand oAdj : ii.liveVars){
                        if (IntermediateInstruction.isConst(oAdj)){continue;}
                        if (!o.equals(oAdj)){
                            adjSet.add(oAdj);
                        }
                    }
                    interferenceGraph.put(o, adjSet);
                }
            }
        }
        /*
        for (Operand o : interferenceGraph.keySet()){
            System.out.println("key: " + o + " value " + interferenceGraph.get(o));
        }
        */
    }

    public void generateLivenessAnalysis(){
        // track the live randes of all the individual values generated by the program being compiled
        HashMap<BasicBlock, Integer> exitSetCount = new HashMap<BasicBlock, Integer>();
        Set<BasicBlock> bbList = ssa.getBasicBlockList();

        for (BasicBlock bb : ssa.getBasicBlockList()) {
            if (bb.name().contains("elim")){continue;}
            exitSetCount.put(bb, 0);
            for (Transitions t : bb.transitionList) {
                t.toBB.addInEdge(bb);
            }
        }

        // Ensure proper visiting order for global liveness analysis
        Boolean change = false;
        do {
            // System.out.println("print");
            change = false;
            for (BasicBlock block: bbList) {
                if (block.name().contains("elim")){continue;}
                change |= block.liveAnalysis();
            }
        } while (change);
    }

    public void colorInterferenceGraph(){
        // Then, color the resulting graph, assign registers to the variables (& the instruction numbers? )
        // If spilling occurs, then map the values that cannot be accomodated onto virtual registers in memory

        List<Integer> availColors = new ArrayList<Integer>(allColors);

        // base case 
        if (interferenceGraph.size() == 0){
            return;
        }

        // Find a node with fewer than k outgoing edges 
        Operand node = null;
        for (Operand o : interferenceGraph.keySet()){
            if (interferenceGraph.get(o).size() < numRegs){
                node = o;
            }
        }
        HashMap<Operand, Set<Operand>> beforeGraph = new HashMap<Operand, Set<Operand>>(interferenceGraph);


        // remove it from the graph
        if (node != null){
            removeNode(interferenceGraph, node);
        }
        else{ 
            // if we cannot find a node with fewer than k neighbors
            // choose and remove an arbitrary node & mark it as "troublesome"
            node = interferenceGraph.keySet().iterator().next();
            removeNode(interferenceGraph, node);
        }

        // recursively color the rest of the graph
        colorInterferenceGraph();

        // add the node back in 
        interferenceGraph = beforeGraph;

        // Assign the node a valid color

        // remove all the colors that have already been assigned to the neighboring nodes
        for (Operand o : interferenceGraph.get(node)){
            if (colorMap.containsKey(o)){
                availColors.remove(colorMap.get(o));
            }
        }   

        if (availColors.size() != 0){ 
            colorMap.put(node, availColors.get(0));
        }
        else{ 
            colorMap.put(node, -1);
        }
    }

    public void removeNode(HashMap<Operand, Set<Operand>> graph, Operand node){
        Set<Operand> toRemove = new HashSet<Operand>();

        graph.remove(node);
        for (Operand o : graph.keySet()){
            for (Operand oAdj : graph.get(o)){
                if (oAdj.equals(node)){
                    toRemove.add(oAdj);
                }
            }
            graph.get(o).removeAll(toRemove);
        }
    }
    public void eliminatePhis(){
        // need this for inList
        generateLivenessAnalysis();

        // Eliminate all phi instructions, inserting move instructions wherever necessary
        for (BasicBlock bb : ssa.getBasicBlockList()){
            if (bb.name().contains("elim")){continue;}
            List<IntermediateInstruction> toRemove = new ArrayList<IntermediateInstruction>();

            for (IntermediateInstruction ii : bb.getIntInsList()){
                if (ii.isElim()){continue;}
                if (ii.getOperator() == SSAOperator.PHI){
                    if (ii.getOperandOne() instanceof Symbol){
                        Symbol opOne = (Symbol) ii.getOperandOne();
                        int opOneInsNum = Integer.parseInt(opOne.name().substring(opOne.name().indexOf("_") + 1, opOne.name().length()));
                        Symbol opTwo = (Symbol) ii.getOperandTwo();
                        int opTwoInsNum = Integer.parseInt(opTwo.name().substring(opTwo.name().indexOf("_") + 1, opTwo.name().length()));

                        IntermediateInstruction moveInsOne = new IntermediateInstruction(SSAOperator.MOVE, opOne, ii.phiSymbol, BasicBlock.insNumber, new VoidType());
                        IntermediateInstruction moveInsTwo = new IntermediateInstruction(SSAOperator.MOVE, ii.getOperandTwo(), ii.phiSymbol, BasicBlock.insNumber, new VoidType());

                        if (bb.inList.get(0).hasInsNum(opOneInsNum) || bb.inList.get(1).hasInsNum(opTwoInsNum) ){
                            bb.inList.get(0).addEnd(moveInsOne);
                            bb.inList.get(1).addEnd(moveInsTwo);
                        }
                        else { 
                            bb.inList.get(1).addEnd(moveInsOne);
                            bb.inList.get(0).addEnd(moveInsTwo);
                        }
                        ii.eliminate();
                    }
                }
            }

            //bb.getIntInsList().removeAll(toRemove);
        }

        System.out.println("After Eliminating PHIs " + ssa.asDotGraph());
    }

    public void insertRegisters(){
        // replace operands with their registers 
        for (BasicBlock bb : ssa.getBasicBlockList()){
            if (bb.name().contains("elim")){continue;}
            for (IntermediateInstruction ii : bb.getIntInsList()){
                if (ii.isElim()){continue;}
                
                if(ii.getOperandOne() != null){
                    if (colorMap.containsKey(ii.getOperandOne())){
                        if (colorMap.get(ii.getOperandOne()) > 0){
                            ii.putRegisterOne(colorMap.get(ii.getOperandOne()));
                        }
                    }
                    else{
                        for (Operand o : colorMap.keySet()){
                            if (!IntermediateInstruction.checkOperand(o, ii.getOperandOne())){
                                if (colorMap.get(o) > 0){
                                    ii.putRegisterOne(colorMap.get(o));
                                }
                            }
                        }
                    }
                }
                

                if (ii.getOperandTwo() != null){
                    if (colorMap.containsKey(ii.getOperandTwo())){
                        if (colorMap.get(ii.getOperandTwo()) > 0){
                            ii.putRegisterTwo(colorMap.get(ii.getOperandTwo()));
                        }
                    }
                    else{
                        for (Operand o : colorMap.keySet()){
                            if (!IntermediateInstruction.checkOperand(o, ii.getOperandTwo())){
                                if (colorMap.get(o) > 0){
                                    ii.putRegisterTwo(colorMap.get(o));
                                }
                            }
                        }
                    }
                }
                
                if (colorMap.containsKey(ii.instNum())){
                    //ii.putOperandTwo(colorMap.get(ii.instNum()));
                    ii.returnReg = colorMap.get(ii.instNum());
                    if (colorMap.get(ii.instNum()) < 0){ 
                        // need to add a store instruction here
                    }
                }
            }
        }

        //System.out.println("after inserting registers " + ssa.asDotGraph());
    }

    public void removeSillyMoves(){
        // Remove all silly move instructions after register assignment (e.g., R5 = R5)
        for (BasicBlock bb : ssa.getBasicBlockList()){
            if (bb.name().contains("elim")){continue;}
            for (IntermediateInstruction ii : bb.getIntInsList()){
                if (ii.isElim()){continue;}
                if (ii.getOperator() == SSAOperator.MOVE){
                    if (ii.getRegisterOne() != null && ii.getRegisterTwo() != null){
                        if (ii.getRegisterOne().equals(ii.getRegisterTwo())){
                            ii.eliminate();
                        }
                    }
                }
            }
        }
        //System.out.println("after removing silly moves " + ssa.asDotGraph());
    }
}
