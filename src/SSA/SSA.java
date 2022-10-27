package SSA;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import ast.*;
import pl434.Symbol;
import SSA.BasicBlock.Transitions;
import SSA.IntermediateInstruction.SSAOperator;

public class SSA implements NodeVisitor{
    private Set<BasicBlock> BasicBlockList; 
    private BasicBlock currentBB;
    private int BBNumber = 1;

    private BasicBlock rootBB;
    
    public SSA(AST ast){
        BasicBlockList = new HashSet<BasicBlock>();
        visit(ast.computation);
        pruneEmpty();
        insertPhi();
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
        for (BasicBlock bb : BasicBlockList) {
            HashSet<BasicBlock> df = dfMap.get(bb);
            for (BasicBlock j : df) {
                bb.addFront(new IntermediateInstruction(SSAOperator.PHI, null, null, -1));
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

    public void pruneEmpty(){
        List<BasicBlock> toRemove = new ArrayList<BasicBlock>();
        List<Transitions> transitionsToRemove; 
        for (BasicBlock BB1 : BasicBlockList){
            if (BB1.size() == 0){
                //System.out.println("empty block "  + BB1.BBNumber);
                for (BasicBlock BB2 : BasicBlockList){
                    transitionsToRemove = new ArrayList<Transitions>();
                    for (Transitions t : BB2.transitionList){
                        if (t.toBB.BBNumber == BB1.BBNumber){
                            //System.out.println("from BB " + t.fromBB);
                            if (BB1.transitionList.size() != 0 ){
                                t.toBB = BB1.transitionList.get(0).toBB;

                                // need to update the branch instructions - isn't updating by reference 
                                if (BB2.getIntInsList().get(BB2.getIntInsList().size() - 1).isBranch()){
                                    BB2.getIntInsList().get(BB2.getIntInsList().size() - 1).updateBranchIns(BB1.transitionList.get(0).toBB);
                                }
                            }
                            else if (BB1.transitionList.size() == 0){
                                transitionsToRemove.add(t);
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
        // remove & rename:
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

    @Override
    public void visit(ArrayIndex node) {
        int mulInsNum = 0;
        int addInsNum = 0;
        if (node.dimList().size() == 1){
            node.indices().get(0).accept(this);
            mulInsNum = currentBB.add(new IntermediateInstruction(SSAOperator.MUL, node.indices().get(0).getOperand(currentBB.varMap), new IntegerLiteral(0, 0, "4"), BasicBlock.insNumber));
            addInsNum = currentBB.add(new IntermediateInstruction(SSAOperator.ADD, new GDB(), node.arrayIdent(), BasicBlock.insNumber));
            node.setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.ADDA, new InstructionNumber(addInsNum), new InstructionNumber(mulInsNum), BasicBlock.insNumber)));
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
                    i = new InstructionNumber(addInsNum);
                    N = new IntegerLiteral(0, 0, node.dimList().get(counter));
                    node.indices().get(counter).accept(this);
                    j = node.indices().get(counter).getOperand(currentBB.varMap); 
                    mulInsNum = currentBB.add(new IntermediateInstruction(SSAOperator.MUL, i, N, BasicBlock.insNumber));
                    addInsNum = currentBB.add(new IntermediateInstruction(SSAOperator.ADD, new InstructionNumber(mulInsNum), j, BasicBlock.insNumber));
                }
                if (counter == 1){
                    mulInsNum = currentBB.add(new IntermediateInstruction(SSAOperator.MUL, i, N, BasicBlock.insNumber));
                    addInsNum = currentBB.add(new IntermediateInstruction(SSAOperator.ADD, new InstructionNumber(mulInsNum), j, BasicBlock.insNumber));
                }
                counter++;
            }

            mulInsNum = currentBB.add(new IntermediateInstruction(SSAOperator.MUL, new InstructionNumber(addInsNum), new IntegerLiteral(0, 0, "4"), BasicBlock.insNumber));
            addInsNum = currentBB.add(new IntermediateInstruction(SSAOperator.ADD, new GDB(), node.arrayIdent(), BasicBlock.insNumber));
            node.setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.ADDA, new InstructionNumber(addInsNum), new InstructionNumber(mulInsNum), BasicBlock.insNumber)));
        }
        /*for (Expression e : node.indices()){
            e.accept(this);
            mulInsNum = currentBB.add(new IntermediateInstruction(SSAOperator.MUL, e.getOperand(currentBB.varMap), new IntegerLiteral(0, 0, "4"), BasicBlock.insNumber));
            addInsNum = currentBB.add(new IntermediateInstruction(SSAOperator.ADD, new GDB(), node.arrayIdent(), BasicBlock.insNumber));
            node.setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.ADDA, new InstructionNumber(addInsNum), new InstructionNumber(mulInsNum), BasicBlock.insNumber)));
        }*/
    }

    @Override
    public void visit(LogicalNot node) {
        node.expr().accept(this);
        if (node.expr() instanceof ArrayIndex){
            ((ArrayIndex) node.expr()).setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.LOAD, node.expr().getOperand(currentBB.varMap), null, BasicBlock.insNumber)));
         }

        node.setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.NOT, node.expr().getOperand(currentBB.varMap), null, BasicBlock.insNumber)));
    }

    @Override
    public void visit(Power node) {
        node.leftExpression().accept(this);
        if (node.leftExpression() instanceof ArrayIndex){
            ((ArrayIndex) node.leftExpression()).setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.LOAD, node.leftExpression().getOperand(currentBB.varMap), null, BasicBlock.insNumber)));
        }
        node.rightExpression().accept(this);
        if (node.rightExpression() instanceof ArrayIndex){
            ((ArrayIndex) node.rightExpression()).setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.LOAD, node.rightExpression().getOperand(currentBB.varMap), null, BasicBlock.insNumber)));
         }
        node.setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.POW, node.leftExpression().getOperand(currentBB.varMap),  node.rightExpression().getOperand(currentBB.varMap), BasicBlock.insNumber)));
    }

    @Override
    public void visit(Multiplication node) {
        node.leftExpression().accept(this);
        if (node.leftExpression() instanceof ArrayIndex){
            ((ArrayIndex) node.leftExpression()).setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.LOAD, node.leftExpression().getOperand(currentBB.varMap), null, BasicBlock.insNumber)));
        }
        node.rightExpression().accept(this);
        if (node.rightExpression() instanceof ArrayIndex){
            ((ArrayIndex) node.rightExpression()).setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.LOAD, node.rightExpression().getOperand(currentBB.varMap), null, BasicBlock.insNumber)));
         }

        node.setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.MUL, node.leftExpression().getOperand(currentBB.varMap),  node.rightExpression().getOperand(currentBB.varMap), BasicBlock.insNumber)));
    }

    @Override
    public void visit(Division node) {
        node.leftExpression().accept(this);
        if (node.leftExpression() instanceof ArrayIndex){
            ((ArrayIndex) node.leftExpression()).setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.LOAD, node.leftExpression().getOperand(currentBB.varMap), null, BasicBlock.insNumber)));
        }
        node.rightExpression().accept(this);
        if (node.rightExpression() instanceof ArrayIndex){
            ((ArrayIndex) node.rightExpression()).setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.LOAD, node.rightExpression().getOperand(currentBB.varMap), null, BasicBlock.insNumber)));
         }

        node.setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.DIV, node.leftExpression().getOperand(currentBB.varMap),  node.rightExpression().getOperand(currentBB.varMap), BasicBlock.insNumber)));
    }

    @Override
    public void visit(Modulo node) {
        node.leftExpression().accept(this);
        if (node.leftExpression() instanceof ArrayIndex){
            ((ArrayIndex) node.leftExpression()).setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.LOAD, node.leftExpression().getOperand(currentBB.varMap), null, BasicBlock.insNumber)));
        }
        node.rightExpression().accept(this);
        if (node.rightExpression() instanceof ArrayIndex){
            ((ArrayIndex) node.rightExpression()).setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.LOAD, node.rightExpression().getOperand(currentBB.varMap), null, BasicBlock.insNumber)));
         }

        node.setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.MOD, node.leftExpression().getOperand(currentBB.varMap),  node.rightExpression().getOperand(currentBB.varMap), BasicBlock.insNumber)));
    }

    @Override
    public void visit(LogicalAnd node) {
        node.leftExpression().accept(this);
        if (node.leftExpression() instanceof ArrayIndex){
            ((ArrayIndex) node.leftExpression()).setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.LOAD, node.leftExpression().getOperand(currentBB.varMap), null, BasicBlock.insNumber)));
        }
        node.rightExpression().accept(this);
        if (node.rightExpression() instanceof ArrayIndex){
            ((ArrayIndex) node.rightExpression()).setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.LOAD, node.rightExpression().getOperand(currentBB.varMap), null, BasicBlock.insNumber)));
         }

        node.setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.AND, node.leftExpression().getOperand(currentBB.varMap),  node.rightExpression().getOperand(currentBB.varMap), BasicBlock.insNumber)));
    }

    @Override
    public void visit(Addition node) {
        node.leftExpression().accept(this);
        if (node.leftExpression() instanceof ArrayIndex){
            ((ArrayIndex) node.leftExpression()).setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.LOAD, node.leftExpression().getOperand(currentBB.varMap), null, BasicBlock.insNumber)));
        }
        node.rightExpression().accept(this);
        if (node.rightExpression() instanceof ArrayIndex){
            ((ArrayIndex) node.rightExpression()).setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.LOAD, node.rightExpression().getOperand(currentBB.varMap), null, BasicBlock.insNumber)));
         }

        node.setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.ADD, node.leftExpression().getOperand(currentBB.varMap),  node.rightExpression().getOperand(currentBB.varMap), BasicBlock.insNumber)));
    }

    @Override
    public void visit(Subtraction node) {
        node.leftExpression().accept(this);
        if (node.leftExpression() instanceof ArrayIndex){
            ((ArrayIndex) node.leftExpression()).setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.LOAD, node.leftExpression().getOperand(currentBB.varMap), null, BasicBlock.insNumber)));
        }
        node.rightExpression().accept(this);
        if (node.rightExpression() instanceof ArrayIndex){
            ((ArrayIndex) node.rightExpression()).setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.LOAD, node.rightExpression().getOperand(currentBB.varMap), null, BasicBlock.insNumber)));
         }

        node.setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.SUB, node.leftExpression().getOperand(currentBB.varMap),  node.rightExpression().getOperand(currentBB.varMap),  BasicBlock.insNumber)));
    }

    @Override
    public void visit(LogicalOr node) {
        node.leftExpression().accept(this);
        if (node.leftExpression() instanceof ArrayIndex){
            ((ArrayIndex) node.leftExpression()).setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.LOAD, node.leftExpression().getOperand(currentBB.varMap), null, BasicBlock.insNumber)));
        }
        node.rightExpression().accept(this);
        if (node.rightExpression() instanceof ArrayIndex){
            ((ArrayIndex) node.rightExpression()).setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.LOAD, node.rightExpression().getOperand(currentBB.varMap), null, BasicBlock.insNumber)));
         }

        node.setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.OR, node.leftExpression().getOperand(currentBB.varMap),  node.rightExpression().getOperand(currentBB.varMap), BasicBlock.insNumber)));
    }

    @Override
    public void visit(Relation node) {
        node.leftExpression().accept(this);
        if (node.leftExpression() instanceof ArrayIndex){
            ((ArrayIndex) node.leftExpression()).setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.LOAD, node.leftExpression().getOperand(currentBB.varMap), null, BasicBlock.insNumber)));
        }
        node.rightExpression().accept(this);
        if (node.rightExpression() instanceof ArrayIndex){
            ((ArrayIndex) node.rightExpression()).setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.LOAD, node.rightExpression().getOperand(currentBB.varMap), null, BasicBlock.insNumber)));
         }
    
        node.setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.CMP, node.leftExpression().getOperand(currentBB.varMap),  node.rightExpression().getOperand(currentBB.varMap), BasicBlock.insNumber)));
    }

    @Override
    public void visit(Assignment node) {
        node.lhsDesignator().accept(this);
        node.rhsExpr().accept(this);

        if (node.rhsExpr() instanceof ArrayIndex){
           ((ArrayIndex) node.rhsExpr()).setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.LOAD, node.rhsExpr().getOperand(currentBB.varMap), null, BasicBlock.insNumber)));
        }
        if (node.lhsDesignator() instanceof Symbol){
             // put new subscript in
            Symbol lhs =  new Symbol(((Symbol) node.lhsDesignator()).name() + "_" + BasicBlock.insNumber, ((Symbol) node.lhsDesignator()).getType().toString(), "var");
            currentBB.varMap.put((Symbol) node.lhsDesignator(), lhs);   
            currentBB.add(new IntermediateInstruction(SSAOperator.MOVE, node.rhsExpr().getOperand(currentBB.varMap), lhs.getOperand(currentBB.varMap), BasicBlock.insNumber));

        }
        else{
            currentBB.add(new IntermediateInstruction(SSAOperator.STORE, node.rhsExpr().getOperand(currentBB.varMap), node.lhsDesignator().getOperand(currentBB.varMap), BasicBlock.insNumber));
        }

    }

    @Override
    public void visit(ArgumentList node) {
        if (!node.empty()){
            for (Expression e : node.argList) { // TODO: make statement sequence iterable
                e.accept(this);
                if (e instanceof ArrayIndex){
                    ((ArrayIndex) e).setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.LOAD, e.getOperand(currentBB.varMap), null, BasicBlock.insNumber)));
                }
            }
        }
    }

    @Override
    public void visit(FunctionCall node) {
        node.argList.accept(this);
        // need to check if predefined function call
        Symbol function = node.getFunctionFromType();
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
                if (!paramsNotEqual){
                    preFuncMatch = prefunc; 
                    break; 
                }
            }
        }

        if (preFuncMatch != null){
            if (preFuncMatch.name().contains("read")){
                node.setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.READ, null, null, BasicBlock.insNumber)));
            }
            else{
                if (preFuncMatch.name().equals("println")){
                    currentBB.add(new IntermediateInstruction(SSAOperator.WRITE, null, null, BasicBlock.insNumber));
                }
                else{
                    currentBB.add(new IntermediateInstruction(SSAOperator.WRITE, node.argList.argList.get(0).getOperand(currentBB.varMap), null, BasicBlock.insNumber));
                }
            }
        }

        // handle user-defined functions: 
        else{
            if (node.argList.argList.size() == 0){
                node.setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.CALL, function, null, BasicBlock.insNumber)));
            }
            if (node.argList.argList.size() == 1){
                node.setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.CALL, node.argList.argList.get(0).getOperand(currentBB.varMap), function, BasicBlock.insNumber)));
            }
            if (node.argList.argList.size() >= 2){
                List<Operand> extra = new ArrayList<Operand>();
                IntermediateInstruction intIns = new IntermediateInstruction(SSAOperator.CALL, node.argList.argList.get(0).getOperand(currentBB.varMap), node.argList.argList.get(1).getOperand(currentBB.varMap), BasicBlock.insNumber);
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
                    currentBB.transitionList.add(currentBB.new Transitions(functionBB, "call" + function.name() + (BasicBlock.insNumber - 1)));
                }
            }
        }
    }

    @Override
    public void visit(IfStatement node) {
        //HashMap<Symbol, Symbol> parentMap = currentBBv;
        BasicBlock thenBlock = new BasicBlock(BBNumber, currentBB.varMap);
        BasicBlockList.add(thenBlock);
        BBNumber++;

        BasicBlock elseBlock = new BasicBlock(BBNumber, currentBB.varMap);
        BasicBlockList.add(elseBlock);
        BBNumber++; 
        BasicBlock joinBlock = new BasicBlock(BBNumber, currentBB.varMap);
        BasicBlockList.add(joinBlock);
        BBNumber++;  

        currentBB.transitionList.add(currentBB.new Transitions(thenBlock, "then"));
        currentBB.transitionList.add(currentBB.new Transitions(elseBlock, "else"));
        node.condition().accept(this);
        if (node.condition() instanceof Relation){
            currentBB.add(new IntermediateInstruction(getBranchOperator((Relation) node.condition()), node.condition().getOperand(currentBB.varMap), elseBlock, BasicBlock.insNumber));
        }

        currentBB = thenBlock; 
        node.thenStatementSeq().accept(this);
        thenBlock = currentBB;
        thenBlock.transitionList.add(thenBlock.new Transitions(joinBlock, ""));

        currentBB = elseBlock;
        if (node.elseStatementSeq() != null){
            thenBlock.add(new IntermediateInstruction(SSAOperator.BRA, joinBlock, null, BasicBlock.insNumber));
            //thenBlock.add(new IntermediateInstruction(SSAOperator.BRA, thenBlock.transitionList.get(thenBlock.transitionList.size() - 1).toBB, null));
            node.elseStatementSeq().accept(this);
        }
        elseBlock = currentBB;
        elseBlock.transitionList.add(elseBlock.new Transitions(joinBlock, ""));

        currentBB = joinBlock;
    }

    public SSAOperator getBranchOperator(Relation node){
        SSAOperator op;        
        if(node.relOp().equals(">")){op = SSAOperator.BLE; }
        else if(node.relOp().equals(">=")){op = SSAOperator.BLT;}
        else if(node.relOp().equals("<")){op = SSAOperator.BGE;}
        else if(node.relOp().equals("<=")){op = SSAOperator.BGT;}
        else if (node.relOp().equals("==")){op = SSAOperator.BEQ;}
        else {op = SSAOperator.BNE;}

        return op;
    }
    @Override
    public void visit(WhileStatement node) {
        BasicBlock whileBlock = new BasicBlock(BBNumber, currentBB.varMap);
        BBNumber++;
        BasicBlockList.add(whileBlock);
        currentBB.transitionList.add(currentBB.new Transitions(whileBlock, ""));
        currentBB = whileBlock;

        BasicBlock thenBlock = new BasicBlock(BBNumber, currentBB.varMap);
        BBNumber++;
        BasicBlockList.add(thenBlock);

        BasicBlock elseBlock = new BasicBlock(BBNumber, currentBB.varMap);
        BBNumber++;
        BasicBlockList.add(elseBlock);

        node.condition().accept(this);

        if (node.condition() instanceof Relation){
            currentBB.add(new IntermediateInstruction(getBranchOperator((Relation) node.condition()), node.condition().getOperand(currentBB.varMap), elseBlock, BasicBlock.insNumber));
        }

        currentBB.transitionList.add(currentBB.new Transitions(thenBlock, "then"));
        currentBB = thenBlock;
        node.statementSeq().accept(this);
        currentBB.add(new IntermediateInstruction(SSAOperator.BRA, whileBlock, null, BasicBlock.insNumber));
        //currentBB.transitionList.add(currentBB.new Transitions(thenBlock, whileBlock, ""));
        currentBB.transitionList.add(currentBB.new Transitions(whileBlock, ""));


        //currentBB.transitionList.add(currentBB.new Transitions(elseBlock, "else"));
        whileBlock.transitionList.add(whileBlock.new Transitions(elseBlock, "else"));
        currentBB = elseBlock;
    }

    @Override
    public void visit(RepeatStatement node) { 
        BasicBlock repeatBB = new BasicBlock(BBNumber, currentBB.varMap);
        BBNumber++;
        BasicBlockList.add(repeatBB);

        BasicBlock conditionBB = new BasicBlock(BBNumber, currentBB.varMap);
        BBNumber++;
        BasicBlockList.add(conditionBB);

        BasicBlock elseBB = new BasicBlock(BBNumber, currentBB.varMap);
        BBNumber++;
        BasicBlockList.add(elseBB);

        currentBB.transitionList.add(currentBB.new Transitions(repeatBB, ""));
        currentBB = repeatBB; 
        node.statementSeq().accept(this);

        currentBB.transitionList.add(currentBB.new Transitions(conditionBB, ""));

        currentBB = conditionBB;
        currentBB.transitionList.add(currentBB.new Transitions(repeatBB, "then"));

        node.condition().accept(this);

        if (node.condition() instanceof Relation){
            currentBB.add(new IntermediateInstruction(getBranchOperator((Relation) node.condition()), node.condition().getOperand(currentBB.varMap), repeatBB, BasicBlock.insNumber));
        }
    
        currentBB.transitionList.add(currentBB.new Transitions(elseBB, "else"));

        currentBB = elseBB;

    }

    @Override
    public void visit(ReturnStatement node) {
        if (node.returnValue() != null){
            node.returnValue().accept(this);
        }

        currentBB.add(new IntermediateInstruction(SSAOperator.RET, node.returnValue().getOperand(currentBB.varMap), null, BasicBlock.insNumber));
    }

    @Override
    public void visit(StatementSequence node) {
        for (Statement s : node.statSeq) { 
            s.accept(this);
        }
    }

    @Override
    public void visit(VariableDeclaration node) {
        node.symbol().accept(this);
    }

    @Override
    public void visit(FunctionBody node) {
        node.variables().accept(this);
        node.statements().accept(this);
    }

    @Override
    public void visit(FunctionDeclaration node) {
        currentBB = new BasicBlock(BBNumber, new HashMap<Symbol, Symbol>(), node.name());
        BBNumber++;
        BasicBlockList.add(currentBB);
        node.body().accept(this);    
    }

    @Override
    public void visit(DeclarationList node) {
        if (node.empty()) return;
        for (Declaration d : node.decList) {
            d.accept(this);
        }        
    }

    @Override
    public void visit(Computation node) {
        BasicBlock mainBB = new BasicBlock(BBNumber, new HashMap<Symbol, Symbol>(), "main");
        BBNumber++;
        BasicBlockList.add(mainBB);
        node.variables().accept(this);
        node.functions().accept(this);

        currentBB = mainBB;
        rootBB = mainBB;
        node.mainStatementSequence().accept(this);
    }

    @Override
    public void visit(Symbol node) {
    }
}
