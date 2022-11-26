package SSA;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import ast.*;
import pl434.Symbol;
import types.BoolType;
import types.FloatType;
import types.IntType;
import types.VoidType;
import SSA.BasicBlock.Transitions;
import SSA.IntermediateInstruction.SSAOperator;


/*for (BasicBlock phiBlock : phiList){
    // get all the parent nodes for the phi block
    parentnodes = new list
    for (BasicBlock bb : BasicBlockList){
        for (Transitions t : bb.transitionLIst){
            if (t.toBB == phiBlock){
                parentnodes.append(bb);
            }
        }
    }

    // get the values of the variables from these parent nodes
    varlist = new list
    for (vardec : p.vardeclist){
        for (p : parentnodes){
            if (p.varMap.containskey(vardec)){
                varlist.add(varMap.get(vardec))
            }
        }
    }
}*/


// go through the dominance frontier, create a hash set of the phi nodes, go through all the phi
// nodes, for each phi node, iterate through the declared variables, check the size of the varMap sets 
// if its greater then one, then insert a phi with all the operands

public class SSA implements NodeVisitor{
    private Set<BasicBlock> BasicBlockList; 
    private BasicBlock currentBB;
    private int BBNumber = 1;

    public BasicBlock rootBB;
    private BasicBlock endBB;

    public List<Edge> edgeList; 

    public static Boolean inFunc = false;

    public class Edge{ 
        BasicBlock BB1; 
        BasicBlock BB2; 
        public Edge (BasicBlock BB1, BasicBlock BB2){
            this.BB1 = BB1; 
            this.BB2 = BB2; 
        }
    }
    
    public SSA(AST ast){
        BasicBlockList = new HashSet<BasicBlock>();
        visit(ast.computation);
        pruneEmpty();
        //insertPhi();
        //System.out.println(getDominanceFrontier(rootBB));

        //edgeList = new ArrayList<Edge>();
        //generateEdgeList();
    }

    public BasicBlock getEndBB() {
        return endBB;
    }
    
    public SSA(){
        BasicBlockList = new HashSet<BasicBlock>();
    }

    public void copyBasicBlockList(SSA ssa){
        this.BasicBlockList.addAll(ssa.getBasicBlockList());
    }

    public void clearAvailExpressions(){
        for (BasicBlock bb : BasicBlockList){
            for (IntermediateInstruction ii : bb.getIntInsList()){
                ii.availableExpressions.clear();
            }
        }
    }

    public void removeBB(BasicBlock BB){
        if (BB.name().contains("elim")){
            return;
        }
        BB.putName(BB.name() + " elim");
    }

    /**
     * Get the dominance frontier of a control flow graph
     * starting at root
     * 
     * @param root root node of control flow graph
     * @return dominance frontier as mapping from basic block to set of basic blocks
     */


    public void insertPhi() {
        HashMap<BasicBlock, HashSet<BasicBlock>> dfMap = getDominanceFrontier(rootBB);
        // 
        for (BasicBlock bb : BasicBlockList) {
            HashSet<BasicBlock> df = dfMap.get(bb);
            for (BasicBlock j : df) {
                j.addFront(new IntermediateInstruction(SSAOperator.PHI, null, null, -1, new VoidType()));
            }
        }
    }

    public HashSet<BasicBlock> getChildren(BasicBlock x) {
        HashSet<BasicBlock> children = new HashSet<BasicBlock>();
        for (Transitions t : x.transitionList) {
            children.add(t.toBB);
        }
        return children;
    }

    public HashMap<BasicBlock, HashSet<BasicBlock>> getDominanceFrontier(BasicBlock root) {
        HashMap<BasicBlock, HashSet<BasicBlock>> dfMap = new HashMap<BasicBlock, HashSet<BasicBlock>>();

        for (BasicBlock bb : BasicBlockList) {
            // dominators.put(bb, getDominatedNodes(root, bb));
            HashSet<BasicBlock> DominatorSet = getDominatedNodes(root, bb);
            HashSet<BasicBlock> df = new HashSet<BasicBlock>();
            // df = {successors of dominated nodes} - {dominatorSet}
            for (BasicBlock d : DominatorSet) {
                // add all nodes for which d is a successor
                df.addAll(getChildren(d));
            }
            // make DominatorSet strictly dominated nodes only
            DominatorSet.remove(bb);
            // remove all strictly dominated nodes
            df.removeAll(DominatorSet);

            dfMap.put(bb, df);
        }

        return dfMap;
    }

    /**
     * Gets set of nodes in a control flow graph starting at root
     * that are dominated by x
     * 
     * @param root of the control flow graph
     * @param x node in the control flow graph
     * @return set of nodes dominated by x
    */
    public HashSet<BasicBlock> getDominatedNodes(BasicBlock root, BasicBlock x) {
        HashSet<BasicBlock> reachable = new HashSet<BasicBlock>();
        HashMap<BasicBlock, Boolean> discovered = new HashMap<BasicBlock, Boolean>();
        Stack<BasicBlock> s = new Stack<BasicBlock>();

        BasicBlock v;

        s.push(root);

        // initialize all nodes except 
        for (BasicBlock bb : BasicBlockList) {
            if (bb == x) {
                discovered.put(bb, true);
            }
            else {
                discovered.put(bb, false);
            }
        }

        while (!s.empty()) {
            v = s.pop();
            // if not discovered, visit node and add to reachable
            if (!discovered.get(v)) {
                // label v as discovered
                discovered.put(v, true);
                reachable.add(v);
                for (Transitions t : v.transitionList) {
                    s.push(t.toBB);
                }
            }
        }
        
        HashSet<BasicBlock> DominatorSet = new HashSet<BasicBlock>(BasicBlockList);
        DominatorSet.removeAll(reachable);

        return DominatorSet;
    }
    /**
     * Removes the empty Basic Blocks from the SSA
     * 
     * @return void
    */
    public void pruneEmpty(){
        List<BasicBlock> toRemove = new ArrayList<BasicBlock>();
        List<Transitions> transitionsToRemove; 

        // Iterate through all of the BasicBlocks
        for (BasicBlock BB1 : BasicBlockList){

            // Check if the Basic Block is empty
            if (BB1.size() == 0){
                //System.out.println("empty " + BB1);
                // If it is, then look for the Basic Blocks that point to it
                // Iterate through all the basic blocks again
                for (BasicBlock BB2 : BasicBlockList){
                    transitionsToRemove = new ArrayList<Transitions>();

                    // Iterate through each of the transitions and check if they point to the empty basic block
                    for (Transitions t : BB2.transitionList){
                        if (t.toBB.BBNumber == BB1.BBNumber){
                            //System.out.println("BB1 " + BB1 + " BB2 " + BB2); 
                            // Update the transition with the BB that the empty block points to
                            if (BB1.transitionList.size() != 0 ){
                                if (t.label.contains("call")){
                                    BB1.transitionList.get(0).toBB.putName(t.toBB.name());
                                }
                                t.toBB = BB1.transitionList.get(0).toBB;
                            }
                            // If the empty block doesn't point to anything, then just remove the transition
                            else if (BB1.transitionList.size() == 0){
                                transitionsToRemove.add(t);
                            }

                            // need to update the branch instructions - isn't updating by reference 
                            if (BB2.getIntInsList().size() != 0 ){
                                if (BB2.getIntInsList().get(BB2.getIntInsList().size() - 1).isBranch()){
                                    if (BB2.getIntInsList().size() >= 2 && BB2.getIntInsList().get(BB2.getIntInsList().size() - 2).getOperator().equals(SSAOperator.RET)){
                                        BB2.getIntInsList().remove(BB2.getIntInsList().size() - 1);
                                    }
                                    else{
                                        if (!t.label.contains("call")){ // don't need to update branch ins in this case
                                            BB2.getIntInsList().get(BB2.getIntInsList().size() - 1).updateBranchIns(t.toBB);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    for (Transitions t: transitionsToRemove){
                        BB2.transitionList.remove(t);
                    }
                }
                toRemove.add(BB1);
            }
        }
        // remove & rename the empty basic blocks
        for (BasicBlock BB : toRemove){
            BasicBlockList.remove(BB);
            for (BasicBlock BB1 : BasicBlockList){
                if (BB1.BBNumber > BB.BBNumber){
                    BB1.BBNumber--;
                }
            }
        }
    }

    public Set<BasicBlock> getBasicBlockList(){
        return BasicBlockList;
    }

    public String asDotGraph(){
        IRVisualizer IRVis = new IRVisualizer(this);
        return IRVis.generateDotGraph();
    }
    @Override
    public void visit(BoolLiteral node) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void visit(IntegerLiteral node) {
        // TODO Auto-generated method stub
    }

    @Override
    public void visit(FloatLiteral node) {
        // TODO Auto-generated method stub
        
    }


    /**
     * generates the instructions for accessing the indices of multi & single dimensional arrays
     * 
     * @param node of the AST for this array
    */
    @Override
    public void visit(ArrayIndex node) {
        InstructionNumber mulInsNum = new InstructionNumber(0, new IntType());
        InstructionNumber addInsNum = new InstructionNumber(0, new IntType());
        // First: the case where there is only one index for the array
        if (node.dimList().size() == 1){
            node.indices().get(0).accept(this);
            mulInsNum = currentBB.add(new IntermediateInstruction(SSAOperator.MUL, node.indices().get(0).getOperand(currentBB.varMap), new IntegerLiteral(0, 0, "4"), BasicBlock.insNumber, new IntType()));
            addInsNum = currentBB.add(new IntermediateInstruction(SSAOperator.ADD, new GDB(), node.arrayIdent(), BasicBlock.insNumber, new IntType()));
            node.setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.ADDA, addInsNum, mulInsNum, BasicBlock.insNumber, new IntType())));
        }
        else{ 
            // see pl434 SSA notes
            int counter = 1;
            node.indices().get(0).accept(this);
            Operand i = node.indices().get(0).getOperand(currentBB.varMap); 
            Operand N = new IntegerLiteral(0, 0, node.dimList().get(1));
            node.indices().get(1).accept(this);
            Operand j = node.indices().get(1).getOperand(currentBB.varMap); 

            for (Expression e : node.indices()){
                if (counter != 1 && counter <= node.indices().size() - 1){
                    i = addInsNum;
                    N = new IntegerLiteral(0, 0, node.dimList().get(counter));
                    node.indices().get(counter).accept(this);
                    j = node.indices().get(counter).getOperand(currentBB.varMap); 
                    mulInsNum = currentBB.add(new IntermediateInstruction(SSAOperator.MUL, i, N, BasicBlock.insNumber, new IntType()));
                    addInsNum = currentBB.add(new IntermediateInstruction(SSAOperator.ADD, mulInsNum, j, BasicBlock.insNumber, new IntType()));
                }
                if (counter == 1){
                    mulInsNum = currentBB.add(new IntermediateInstruction(SSAOperator.MUL, i, N, BasicBlock.insNumber, new IntType()));
                    addInsNum = currentBB.add(new IntermediateInstruction(SSAOperator.ADD, mulInsNum, j, BasicBlock.insNumber, new IntType()));
                }
                counter++;
            }

            mulInsNum = currentBB.add(new IntermediateInstruction(SSAOperator.MUL, addInsNum, new IntegerLiteral(0, 0, "4"), BasicBlock.insNumber, new IntType()));
            addInsNum = currentBB.add(new IntermediateInstruction(SSAOperator.ADD, new GDB(), node.arrayIdent(), BasicBlock.insNumber, new IntType()));
            node.setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.ADDA, addInsNum, mulInsNum, BasicBlock.insNumber, new IntType())));
        }
    }

    @Override
    public void visit(LogicalNot node) {
        node.expr().accept(this);
        if (node.expr() instanceof ArrayIndex){
            ((ArrayIndex) node.expr()).setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.LOAD, node.expr().getOperand(currentBB.varMap), null, BasicBlock.insNumber, node.expr().type())));
         }

        node.setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.NOT, node.expr().getOperand(currentBB.varMap), null, BasicBlock.insNumber, node.expr().type())));
    }

    @Override
    public void visit(Power node) {
        node.leftExpression().accept(this);
        if (node.leftExpression() instanceof ArrayIndex){
            ((ArrayIndex) node.leftExpression()).setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.LOAD, node.leftExpression().getOperand(currentBB.varMap), null, BasicBlock.insNumber, node.leftExpression().type())));
        }
        node.rightExpression().accept(this);
        if (node.rightExpression() instanceof ArrayIndex){
            ((ArrayIndex) node.rightExpression()).setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.LOAD, node.rightExpression().getOperand(currentBB.varMap), null, BasicBlock.insNumber, node.rightExpression().type())));
         }
        node.setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.POW, node.leftExpression().getOperand(currentBB.varMap),  node.rightExpression().getOperand(currentBB.varMap), BasicBlock.insNumber, node.leftExpression().type())));
    }

    @Override
    public void visit(Multiplication node) {
        node.leftExpression().accept(this);
        if (node.leftExpression() instanceof ArrayIndex){
            ((ArrayIndex) node.leftExpression()).setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.LOAD, node.leftExpression().getOperand(currentBB.varMap), null, BasicBlock.insNumber, node.leftExpression().type())));
        }
        node.rightExpression().accept(this);
        if (node.rightExpression() instanceof ArrayIndex){
            ((ArrayIndex) node.rightExpression()).setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.LOAD, node.rightExpression().getOperand(currentBB.varMap), null, BasicBlock.insNumber, node.rightExpression().type())));
         }

        node.setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.MUL, node.leftExpression().getOperand(currentBB.varMap),  node.rightExpression().getOperand(currentBB.varMap), BasicBlock.insNumber, node.rightExpression().type())));
    }

    @Override
    public void visit(Division node) {
        node.leftExpression().accept(this);
        if (node.leftExpression() instanceof ArrayIndex){
            ((ArrayIndex) node.leftExpression()).setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.LOAD, node.leftExpression().getOperand(currentBB.varMap), null, BasicBlock.insNumber, node.leftExpression().type())));
        }
        node.rightExpression().accept(this);
        if (node.rightExpression() instanceof ArrayIndex){
            ((ArrayIndex) node.rightExpression()).setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.LOAD, node.rightExpression().getOperand(currentBB.varMap), null, BasicBlock.insNumber, node.rightExpression().type())));
         }

        node.setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.DIV, node.leftExpression().getOperand(currentBB.varMap),  node.rightExpression().getOperand(currentBB.varMap), BasicBlock.insNumber, node.rightExpression().type())));
    }

    @Override
    public void visit(Modulo node) {
        node.leftExpression().accept(this);
        if (node.leftExpression() instanceof ArrayIndex){
            ((ArrayIndex) node.leftExpression()).setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.LOAD, node.leftExpression().getOperand(currentBB.varMap), null, BasicBlock.insNumber, node.leftExpression().type())));
        }
        node.rightExpression().accept(this);
        if (node.rightExpression() instanceof ArrayIndex){
            ((ArrayIndex) node.rightExpression()).setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.LOAD, node.rightExpression().getOperand(currentBB.varMap), null, BasicBlock.insNumber, node.rightExpression().type())));
         }

        node.setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.MOD, node.leftExpression().getOperand(currentBB.varMap),  node.rightExpression().getOperand(currentBB.varMap), BasicBlock.insNumber, node.rightExpression().type())));
    }

    @Override
    public void visit(LogicalAnd node) {
        node.leftExpression().accept(this);
        if (node.leftExpression() instanceof ArrayIndex){
            ((ArrayIndex) node.leftExpression()).setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.LOAD, node.leftExpression().getOperand(currentBB.varMap), null, BasicBlock.insNumber, node.leftExpression().type())));
        }
        node.rightExpression().accept(this);
        if (node.rightExpression() instanceof ArrayIndex){
            ((ArrayIndex) node.rightExpression()).setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.LOAD, node.rightExpression().getOperand(currentBB.varMap), null, BasicBlock.insNumber, node.leftExpression().type())));
         }

        node.setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.AND, node.leftExpression().getOperand(currentBB.varMap),  node.rightExpression().getOperand(currentBB.varMap), BasicBlock.insNumber, node.rightExpression().type())));
    }

    @Override
    public void visit(Addition node) {
        node.leftExpression().accept(this);
        if (node.leftExpression() instanceof ArrayIndex){
            ((ArrayIndex) node.leftExpression()).setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.LOAD, node.leftExpression().getOperand(currentBB.varMap), null, BasicBlock.insNumber, node.leftExpression().type())));
        }
        node.rightExpression().accept(this);
        if (node.rightExpression() instanceof ArrayIndex){
            ((ArrayIndex) node.rightExpression()).setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.LOAD, node.rightExpression().getOperand(currentBB.varMap), null, BasicBlock.insNumber, node.rightExpression().type())));
         }

        node.setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.ADD, node.leftExpression().getOperand(currentBB.varMap), node.rightExpression().getOperand(currentBB.varMap), BasicBlock.insNumber, node.rightExpression().type())));
    }

    @Override
    public void visit(Subtraction node) {
        node.leftExpression().accept(this);
        if (node.leftExpression() instanceof ArrayIndex){
            ((ArrayIndex) node.leftExpression()).setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.LOAD, node.leftExpression().getOperand(currentBB.varMap), null, BasicBlock.insNumber, node.leftExpression().type())));
        }
        node.rightExpression().accept(this);
        if (node.rightExpression() instanceof ArrayIndex){
            ((ArrayIndex) node.rightExpression()).setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.LOAD, node.rightExpression().getOperand(currentBB.varMap), null, BasicBlock.insNumber, node.rightExpression().type())));
         }

        node.setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.SUB, node.leftExpression().getOperand(currentBB.varMap),  node.rightExpression().getOperand(currentBB.varMap),  BasicBlock.insNumber, node.rightExpression().type())));
    }

    @Override
    public void visit(LogicalOr node) {
        node.leftExpression().accept(this);
        if (node.leftExpression() instanceof ArrayIndex){
            ((ArrayIndex) node.leftExpression()).setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.LOAD, node.leftExpression().getOperand(currentBB.varMap), null, BasicBlock.insNumber, node.leftExpression().type())));
        }
        node.rightExpression().accept(this);
        if (node.rightExpression() instanceof ArrayIndex){
            ((ArrayIndex) node.rightExpression()).setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.LOAD, node.rightExpression().getOperand(currentBB.varMap), null, BasicBlock.insNumber, node.rightExpression().type())));
         }

        node.setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.OR, node.leftExpression().getOperand(currentBB.varMap),  node.rightExpression().getOperand(currentBB.varMap), BasicBlock.insNumber, node.rightExpression().type())));
    }

    @Override
    public void visit(Relation node) {
        node.leftExpression().accept(this);
        if (node.leftExpression() instanceof ArrayIndex){
            ((ArrayIndex) node.leftExpression()).setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.LOAD, node.leftExpression().getOperand(currentBB.varMap), null, BasicBlock.insNumber, node.leftExpression().type())));
        }
        node.rightExpression().accept(this);
        if (node.rightExpression() instanceof ArrayIndex){
            ((ArrayIndex) node.rightExpression()).setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.LOAD, node.rightExpression().getOperand(currentBB.varMap), null, BasicBlock.insNumber, node.rightExpression().type())));
         }
    
        
        IntermediateInstruction ii = new IntermediateInstruction(SSAOperator.CMP, node.leftExpression().getOperand(currentBB.varMap),  node.rightExpression().getOperand(currentBB.varMap), BasicBlock.insNumber, node.rightExpression().type());
        ii.cmp = node.operator();
        node.setInsNumber(currentBB.add(ii));
    }

    @Override
    public void visit(Assignment node) {
        node.lhsDesignator().accept(this);
        node.rhsExpr().accept(this);

        if (node.rhsExpr() instanceof ArrayIndex){
           ((ArrayIndex) node.rhsExpr()).setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.LOAD, node.rhsExpr().getOperand(currentBB.varMap), null, BasicBlock.insNumber, node.rhsExpr().type())));
        }
        if (node.lhsDesignator() instanceof Symbol){
             // put new subscript in
            HashSet<Symbol> newHash = new HashSet<Symbol>();
            Symbol lhs = new Symbol(((Symbol) node.lhsDesignator()).name() + "_" + BasicBlock.insNumber, ((Symbol) node.lhsDesignator()).getType().toString(), "var", ((Symbol) node.lhsDesignator()).scope);
            newHash.add(lhs);
            currentBB.varMap.put((Symbol) node.lhsDesignator(), newHash);   
            currentBB.add(new IntermediateInstruction(SSAOperator.MOVE, node.rhsExpr().getOperand(currentBB.varMap), lhs, BasicBlock.insNumber, new VoidType()));

        }
        else{
            currentBB.add(new IntermediateInstruction(SSAOperator.STORE, node.rhsExpr().getOperand(currentBB.varMap), node.lhsDesignator().getOperand(currentBB.varMap), BasicBlock.insNumber, new VoidType()));
        }

    }

    @Override
    public void visit(ArgumentList node) {
        if (!node.empty()){
            for (Expression e : node.argList) { // TODO: make statement sequence iterable
                e.accept(this);
                if (e instanceof ArrayIndex){
                    ((ArrayIndex) e).setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.LOAD, e.getOperand(currentBB.varMap), null, BasicBlock.insNumber, e.type())));
                }
            }
        }
    }

    @Override
    public void visit(FunctionCall node) {
        node.argList.accept(this);
        // need to check if predefined function call
        Symbol function = node.getFunctionFromType();

        //System.out.println("function " + function.name() + " param type " + function.getParamTypes());
        Boolean paramsNotEqual = false;
        Symbol preFuncMatch = null; 
        for(Symbol prefunc : node.predefinedFunctions){
            if (function.name().equals(prefunc.name())){
                if (function.getParamTypes().size() == (prefunc.getParamTypes().size())){
                    // if they are, iterate through and check that they are the same 
                    for (int i = 0; i < function.getParamTypes().size(); i++){
                       if (!function.getParamTypes().get(i).toString().equals(prefunc.getParamTypes().get(i).toString())){
                            paramsNotEqual = true;
                            break;
                        }
                    }
                }
                else{ 
                    paramsNotEqual = true;
                }
                if (!paramsNotEqual){
                    preFuncMatch = prefunc; 
                    break; 
                }
            }
        }

        if (preFuncMatch != null){
            if (preFuncMatch.name().contains("read")){
                if (preFuncMatch.name().equals("readInt")){
                    node.setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.READ_I, null, null, BasicBlock.insNumber, new IntType())));
                }
                else if (preFuncMatch.name().equals("readFloat")){
                    node.setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.READ_F, null, null, BasicBlock.insNumber, new FloatType())));
                }
                else if (preFuncMatch.name().equals("readBool")){
                    node.setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.READ_B, null, null, BasicBlock.insNumber, new BoolType())));
                }
            }
            else{
                if (preFuncMatch.name().equals("println")){
                    currentBB.add(new IntermediateInstruction(SSAOperator.WRITE_NL, null, null, BasicBlock.insNumber, new VoidType()));
                }
                else if (preFuncMatch.name().equals("printFloat")){
                    currentBB.add(new IntermediateInstruction(SSAOperator.WRITE_F, node.argList.argList.get(0).getOperand(currentBB.varMap), null, BasicBlock.insNumber, new VoidType()));
                }
                else if (preFuncMatch.name().equals("printInt")){
                    currentBB.add(new IntermediateInstruction(SSAOperator.WRITE_I, node.argList.argList.get(0).getOperand(currentBB.varMap), null, BasicBlock.insNumber, new VoidType()));
                }
                else {
                    currentBB.add(new IntermediateInstruction(SSAOperator.WRITE_B, node.argList.argList.get(0).getOperand(currentBB.varMap), null, BasicBlock.insNumber, new VoidType()));
                }
            }
        }

        // handle user-defined functions: 
        else{
            if (node.argList.argList.size() == 0){
                node.setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.CALL, function, null, BasicBlock.insNumber, function.type())));
            }
            if (node.argList.argList.size() == 1){
                node.setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.CALL, node.argList.argList.get(0).getOperand(currentBB.varMap), function, BasicBlock.insNumber, function.type())));
            }
            if (node.argList.argList.size() >= 2){
                List<Operand> extra = new ArrayList<Operand>();
                IntermediateInstruction intIns = new IntermediateInstruction(SSAOperator.CALL, node.argList.argList.get(0).getOperand(currentBB.varMap), node.argList.argList.get(1).getOperand(currentBB.varMap), BasicBlock.insNumber, function.type());
                node.setInsNumber(currentBB.add(intIns));
                for (int i = 2; i < node.argList.argList.size(); i++){
                    extra.add(node.argList.argList.get(i).getOperand(currentBB.varMap));
                }
                extra.add(function);
                intIns.addExtraOperands(extra);
            }
            BasicBlock functionBB;
            for (BasicBlock BB : BasicBlockList){
                if(BB.name() == function.name()){
                    functionBB = BB;
                    functionBB.function = function;
                    currentBB.transitionList.add(currentBB.new Transitions(functionBB, "call" + function.name() + (BasicBlock.insNumber - 1)));
                }
            }
        }
    }

    @Override
    public void visit(IfStatement node) {
        //HashMap<Symbol, Symbol> parentMap = currentBBv;
        BasicBlock thenBlock = new BasicBlock(BBNumber, currentBB.varMap, inFunc);
        thenBlock.addFullMap(currentBB.varMap);
        BasicBlockList.add(thenBlock);
        BBNumber++;

        BasicBlock elseBlock = new BasicBlock(BBNumber, currentBB.varMap, inFunc);
        elseBlock.addFullMap(currentBB.varMap);
        BasicBlockList.add(elseBlock);
        BBNumber++; 
        
        BasicBlock joinBlock = new BasicBlock(BBNumber, new HashMap<Symbol, HashSet<Symbol>>(), inFunc);
        BasicBlockList.add(joinBlock);
        BBNumber++;  

        currentBB.transitionList.add(currentBB.new Transitions(thenBlock, "then"));
        currentBB.transitionList.add(currentBB.new Transitions(elseBlock, "else"));
        node.condition().accept(this);
        if (node.condition() instanceof Relation){
            currentBB.add(new IntermediateInstruction(getBranchOperator((Relation) node.condition()), node.condition().getOperand(currentBB.varMap), elseBlock, BasicBlock.insNumber, new VoidType()));
        }
        else{ 
            currentBB.add(new IntermediateInstruction(SSAOperator.BEQ, node.condition().getOperand(currentBB.varMap), elseBlock, BasicBlock.insNumber, new VoidType()));
        }

        currentBB = thenBlock; 
        node.thenStatementSeq().accept(this);
        thenBlock = currentBB;
        thenBlock.transitionList.add(thenBlock.new Transitions(joinBlock, ""));

        currentBB = elseBlock;
        if (node.elseStatementSeq() != null){
            thenBlock.add(new IntermediateInstruction(SSAOperator.BRA, joinBlock, null, BasicBlock.insNumber, new VoidType()));
            //thenBlock.add(new IntermediateInstruction(SSAOperator.BRA, thenBlock.transitionList.get(thenBlock.transitionList.size() - 1).toBB, null));
            node.elseStatementSeq().accept(this);
        }
        elseBlock = currentBB;
        elseBlock.transitionList.add(elseBlock.new Transitions(joinBlock, ""));

        joinBlock.addFullMap(thenBlock.varMap);
        // resolve any conflicts between the two branches
        //InstructionNumber insNum = new InstructionNumber(0);
        int insNum = 0;
        for (Symbol key : elseBlock.varMap.keySet()){
            if (joinBlock.varMap.containsKey(key)){
                joinBlock.varMap.get(key).addAll(elseBlock.varMap.get(key));

                if (joinBlock.varMap.get(key).size() > 1){
                    Iterator<Symbol> it = joinBlock.varMap.get(key).iterator();
                    IntermediateInstruction newIns = new IntermediateInstruction(SSAOperator.PHI, it.next(), it.next(), BasicBlock.insNumber, new VoidType());
                    insNum = joinBlock.add(newIns).getInstructionNumber();
                    HashSet<Symbol> newHash = new HashSet<Symbol>();
                    Symbol newSymbol = new Symbol(key.name() + "_" + insNum, key.type().toString(), "var", key.scope);
                    newHash.add(newSymbol);
                    joinBlock.varMap.put(key, newHash);
                    
                    newIns.phiSymbol = newSymbol;
                    //System.out.println()
                }
            }
        }

        currentBB = joinBlock;
    }

    public SSAOperator getBranchOperator(Relation node){
        SSAOperator op;        
        if(node.relOp().equals(">")){op = SSAOperator.BLE; }
        else if(node.relOp().equals(">=")){op = SSAOperator.BLT;}
        else if(node.relOp().equals("<")){op = SSAOperator.BGE;}
        else if(node.relOp().equals("<=")){op = SSAOperator.BGT;}
        else if (node.relOp().equals("==")){op = SSAOperator.BNE;}
        else {op = SSAOperator.BEQ;}

        return op;
    }
    @Override
    public void visit(WhileStatement node) {
        BasicBlock whileBlock = new BasicBlock(BBNumber, new HashMap<Symbol, HashSet<Symbol>>(), inFunc);
        whileBlock.addFullMap(currentBB.varMap);
        BBNumber++;
        BasicBlockList.add(whileBlock);
        currentBB.transitionList.add(currentBB.new Transitions(whileBlock, ""));
        currentBB = whileBlock;

        BasicBlock thenBlock = new BasicBlock(BBNumber, currentBB.varMap, inFunc);
        BBNumber++;
        BasicBlockList.add(thenBlock);
        thenBlock.addFullMap(currentBB.varMap);

        BasicBlock elseBlock = new BasicBlock(BBNumber, new HashMap<Symbol, HashSet<Symbol>>(), inFunc);
        BBNumber++;
        BasicBlockList.add(elseBlock);

        node.condition().accept(this);

        if (node.condition() instanceof Relation){
            currentBB.add(new IntermediateInstruction(getBranchOperator((Relation) node.condition()), node.condition().getOperand(currentBB.varMap), elseBlock, BasicBlock.insNumber, new VoidType()));
        }
        else{ 
            currentBB.add(new IntermediateInstruction(SSAOperator.BEQ, node.condition().getOperand(currentBB.varMap), elseBlock, BasicBlock.insNumber, new VoidType()));
        }

        currentBB.transitionList.add(currentBB.new Transitions(thenBlock, "then"));
        currentBB = thenBlock;
        node.statementSeq().accept(this);
        currentBB.add(new IntermediateInstruction(SSAOperator.BRA, whileBlock, null, BasicBlock.insNumber, new VoidType()));
        //currentBB.transitionList.add(currentBB.new Transitions(thenBlock, whileBlock, ""));
        currentBB.transitionList.add(currentBB.new Transitions(whileBlock, "", true));


        //currentBB.transitionList.add(currentBB.new Transitions(elseBlock, "else"));
        whileBlock.transitionList.add(whileBlock.new Transitions(elseBlock, "else"));

        int insNum = 0;
        for (Symbol key : thenBlock.varMap.keySet()){
            if (whileBlock.varMap.containsKey(key)){
                whileBlock.varMap.get(key).addAll(thenBlock.varMap.get(key));

                if (whileBlock.varMap.get(key).size() > 1){
                    Iterator<Symbol> it = whileBlock.varMap.get(key).iterator();
                    IntermediateInstruction newIns = new IntermediateInstruction(SSAOperator.PHI, it.next(), it.next(), BasicBlock.insNumber, new VoidType());
                    insNum = whileBlock.addFront(newIns);
                    HashSet<Symbol> newHash = new HashSet<Symbol>();
                    Symbol newSymbol = new Symbol(key.name() + "_" + insNum, key.type().toString(), "var", key.scope);
                    newHash.add(newSymbol);
                    whileBlock.varMap.put(key, newHash);

                    newIns.phiSymbol = newSymbol;
                    //System.out.println()
                }
            }
        }

        whileBlock.resolveVars(whileBlock.varMap);
        thenBlock.resolveVars(whileBlock.varMap);

        elseBlock.addFullMap(whileBlock.varMap);
        currentBB = elseBlock;
    }

    @Override
    public void visit(RepeatStatement node) { 
        BasicBlock parentBB = currentBB;
        BasicBlock repeatBB = new BasicBlock(BBNumber, currentBB.varMap, inFunc);
        BasicBlock afterRepeatBB;
        repeatBB.addFullMap(currentBB.varMap);
        BBNumber++;
        BasicBlockList.add(repeatBB);

        BasicBlock conditionBB = new BasicBlock(BBNumber, new HashMap<Symbol, HashSet<Symbol>>(), inFunc);
        BBNumber++;
        BasicBlockList.add(conditionBB);

        BasicBlock elseBB = new BasicBlock(BBNumber, currentBB.varMap, inFunc);
        elseBB.addFullMap(currentBB.varMap);
        BBNumber++;
        BasicBlockList.add(elseBB);

        currentBB.transitionList.add(currentBB.new Transitions(repeatBB, ""));
        currentBB = repeatBB; 
        node.statementSeq().accept(this);
        repeatBB.addFullMap(currentBB.varMap);

        // add varMap from 
        conditionBB.addFullMap(repeatBB.varMap);

        currentBB.transitionList.add(currentBB.new Transitions(conditionBB, ""));

        currentBB = conditionBB;
        currentBB.transitionList.add(currentBB.new Transitions(repeatBB, "else", true));

        node.condition().accept(this);

        if (node.condition() instanceof Relation){
            currentBB.add(new IntermediateInstruction(getBranchOperator((Relation) node.condition()), node.condition().getOperand(currentBB.varMap), repeatBB, BasicBlock.insNumber, new VoidType()));
        }
        else{
            currentBB.add(new IntermediateInstruction(SSAOperator.BEQ, node.condition().getOperand(currentBB.varMap), repeatBB, BasicBlock.insNumber, new VoidType()));
        }
    
        currentBB.transitionList.add(currentBB.new Transitions(elseBB, "then"));

        for (Symbol key : parentBB.varMap.keySet()){
            int insNum = 0;
            if (repeatBB.varMap.containsKey(key)){
                repeatBB.varMap.get(key).addAll(parentBB.varMap.get(key));

                if (repeatBB.varMap.get(key).size() > 1){
                    //System.out.println("here");
                    Iterator<Symbol> it = repeatBB.varMap.get(key).iterator();
                    IntermediateInstruction newIns = new IntermediateInstruction(SSAOperator.PHI, it.next(), it.next(), BasicBlock.insNumber, new VoidType());
                    insNum = repeatBB.addFront(newIns);
                    HashSet<Symbol> newHash = new HashSet<Symbol>();
                    Symbol newSymbol = new Symbol(key.name() + "_" + insNum, key.type().toString(), "var", key.scope);
                    newHash.add(newSymbol);
                    repeatBB.varMap.put(key, newHash);

                    newIns.phiSymbol = newSymbol;
                }
            }
        }

        repeatBB.resolveVars(repeatBB.varMap);
        conditionBB.resolveVars(repeatBB.varMap);

        elseBB.addFullMap(repeatBB.varMap);


        currentBB = elseBB;

    }

    @Override
    public void visit(ReturnStatement node) {
        if (node.returnValue() != null){
            node.returnValue().accept(this);
            currentBB.add(new IntermediateInstruction(SSAOperator.RET, node.returnValue().getOperand(currentBB.varMap), null, BasicBlock.insNumber, new VoidType()));
        }
        else{
            currentBB.add(new IntermediateInstruction(SSAOperator.RET, null, null, BasicBlock.insNumber, new VoidType()));

        }

    }

    @Override
    public void visit(StatementSequence node) {
        for (Statement s : node.statSeq) { 
            s.accept(this);
        }
    }

    @Override
    public void visit(VariableDeclaration node) {
        // Global var
        if (currentBB.name().equals("main")){
            HashSet<Symbol> newHash = new HashSet<Symbol>();
            if (node.symbol().getSymbolType().equals("arr")){
                newHash.add(new Symbol(node.symbol().name(), node.symbol().type().toString(), node.symbol().getSymbolType(), 1));
            }
            else{ 
                newHash.add(new Symbol(node.symbol().name() + "_-1", node.symbol().type().toString(), node.symbol().getSymbolType(), 1));
            }
            currentBB.varMap.put(node.symbol(), newHash);
            node.symbol().scope = 1;
            //currentBB.varMap.put(node.symbol(), new Symbol(node.symbol().name() + "_-1", node.symbol().type().toString(), "var"));
        }
        // Local var
        else{ 
            HashSet<Symbol> newHash = new HashSet<Symbol>();
            newHash.add(new Symbol(node.symbol().name() + "_-2", node.symbol().type().toString(), node.symbol().getSymbolType(), 2));
            currentBB.varMap.put(node.symbol(), newHash);
            node.symbol().scope = 2;
        }
        node.symbol().accept(this);
    }

    @Override
    public void visit(FunctionBody node) {
        node.variables().accept(this);
        node.statements().accept(this);
    }

    @Override
    public void visit(FunctionDeclaration node) {
        //System.out.println(node.name()); 
        //System.out.println(BBNumber);
        /*currentBB = new BasicBlock(BBNumber, new HashMap<Symbol, Symbol>(), node.name());
        BBNumber++;
        BasicBlockList.add(currentBB);*/

        //System.out.println(node.function().par)
        //System.out.println(node.name());

        inFunc = true;

        for (BasicBlock BB : BasicBlockList){
            if(BB.name().equals(node.name())){
                BB.addMap(currentBB.varMap);
                currentBB = BB;
                break;
            }
        }
        node.body().accept(this);  
        
        inFunc = false;
    }

    @Override
    public void visit(DeclarationList node) {
        if (node.empty()) return;

        for (Declaration d : node.decList) {
            if (d instanceof FunctionDeclaration){
                currentBB = new BasicBlock(BBNumber, currentBB.varMap, ((FunctionDeclaration) d).name(), inFunc);
                BBNumber++;
                BasicBlockList.add(currentBB);
            }
        }

        for (Declaration d : node.decList) {
            d.accept(this);
        }        
    }

    @Override
    public void visit(Computation node) {
        BasicBlock mainBB = new BasicBlock(BBNumber, new HashMap<Symbol, HashSet<Symbol>>(), "main", inFunc);
        BBNumber++;
        BasicBlockList.add(mainBB);
        currentBB = mainBB;
        node.variables().accept(this);
        node.functions().accept(this);

        currentBB = mainBB;
        rootBB = mainBB;
        node.mainStatementSequence().accept(this);
        currentBB.add(new IntermediateInstruction(SSAOperator.END, null, null, BasicBlock.insNumber, new VoidType()));
        
        endBB = currentBB;
    }

    @Override
    public void visit(Symbol node) {
    }
}
