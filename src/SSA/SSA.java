package SSA;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.sql.rowset.spi.TransactionalWriter;

import ast.*;
import pl434.Symbol;
import SSA.BasicBlock.Transitions;
import SSA.IntermediateInstruction.SSAOperator;

public class SSA implements NodeVisitor{
    private List<BasicBlock> BasicBlockList; 
    private BasicBlock currentBB;
    private int BBNumber = 1;
    
    public SSA(AST ast){
        BasicBlockList = new ArrayList<BasicBlock>();
        visit(ast.computation);
        //pruneEmpty();
    }

    public void pruneEmpty(){
        List<BasicBlock> toRemove = new ArrayList<BasicBlock>(); 
        int decrease = 0;
        for (BasicBlock BB1 : BasicBlockList){
            if (BB1.size() == 0){
                for (BasicBlock BB2 : BasicBlockList){
                    for (Transitions t : BB2.transitionList){
                        if (t.toBB.BBNumber == BB1.BBNumber){
                            t.toBB = BB1.transitionList.get(0).toBB;
                        }
                    }
                }
                toRemove.add(BB1);
                decrease++;
            }
            BB1.BBNumber -= decrease;
        }
        for (BasicBlock BB : toRemove){
            BasicBlockList.remove(BB);
        }
    }

    public List<BasicBlock> getBasicBlockList(){
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
        // TODO Auto-generated method stub
        
    }

    @Override
    public void visit(LogicalNot node) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void visit(Power node) {
        node.leftExpression().accept(this);
        node.rightExpression().accept(this);

        node.setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.POW, node.leftExpression().getOperand(),  node.rightExpression().getOperand())));
    }

    @Override
    public void visit(Multiplication node) {
        node.leftExpression().accept(this);
        node.rightExpression().accept(this);

        node.setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.MUL, node.leftExpression().getOperand(),  node.rightExpression().getOperand())));
    }

    @Override
    public void visit(Division node) {
        node.leftExpression().accept(this);
        node.rightExpression().accept(this);

        node.setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.DIV, node.leftExpression().getOperand(),  node.rightExpression().getOperand())));
    }

    @Override
    public void visit(Modulo node) {
        node.leftExpression().accept(this);
        node.rightExpression().accept(this);

        node.setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.MOD, node.leftExpression().getOperand(),  node.rightExpression().getOperand())));
    }

    @Override
    public void visit(LogicalAnd node) {
        node.leftExpression().accept(this);
        node.rightExpression().accept(this);

        node.setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.AND, node.leftExpression().getOperand(),  node.rightExpression().getOperand())));
    }

    @Override
    public void visit(Addition node) {
        node.leftExpression().accept(this);
        node.rightExpression().accept(this);

        node.setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.ADD, node.leftExpression().getOperand(),  node.rightExpression().getOperand())));
    }

    @Override
    public void visit(Subtraction node) {
        node.leftExpression().accept(this);
        node.rightExpression().accept(this);

        node.setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.SUB, node.leftExpression().getOperand(),  node.rightExpression().getOperand())));
    }

    @Override
    public void visit(LogicalOr node) {
        node.leftExpression().accept(this);
        node.rightExpression().accept(this);

        node.setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.OR, node.leftExpression().getOperand(),  node.rightExpression().getOperand())));
    }

    @Override
    public void visit(Relation node) {
        node.leftExpression().accept(this);
        node.rightExpression().accept(this);

        node.setInsNumber(currentBB.add(new IntermediateInstruction(SSAOperator.CMP, node.leftExpression().getOperand(),  node.rightExpression().getOperand())));
    }

    @Override
    public void visit(Assignment node) {
        node.lhsDesignator().accept(this);
        node.rhsExpr().accept(this);

       currentBB.add(new IntermediateInstruction(SSAOperator.MOVE, node.rhsExpr().getOperand(), node.lhsDesignator().getOperand()));

    }

    @Override
    public void visit(ArgumentList node) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void visit(FunctionCall node) {
        // need to check if predefined function call
        Symbol function = node.getFunctionFromType();
    }

    @Override
    public void visit(IfStatement node) {
        BBNumber++; 
        BasicBlock thenBlock = new BasicBlock(BBNumber);
        BasicBlockList.add(thenBlock);
        BBNumber++;
        BasicBlock elseBlock = new BasicBlock(BBNumber);
        BasicBlockList.add(elseBlock);
        BBNumber++; 
        BasicBlock joinBlock = new BasicBlock(BBNumber);
        BasicBlockList.add(joinBlock);

        currentBB.transitionList.add(currentBB.new Transitions(currentBB, thenBlock, "then"));
        currentBB.transitionList.add(currentBB.new Transitions(currentBB, elseBlock, "else"));
        node.condition().accept(this);
        if (node.condition() instanceof Relation){
            currentBB.add(new IntermediateInstruction(getBranchOperator((Relation) node.condition()), node.condition().getOperand(), currentBB));
        }

        currentBB = thenBlock; 
        node.thenStatementSeq().accept(this);
        thenBlock = currentBB;
        thenBlock.transitionList.add(thenBlock.new Transitions(thenBlock, joinBlock, ""));
        
        currentBB = elseBlock;
        if (node.elseStatementSeq() != null){
            node.elseStatementSeq().accept(this);
        }
        elseBlock = currentBB;
        elseBlock.transitionList.add(elseBlock.new Transitions(elseBlock, joinBlock, ""));

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
        BBNumber++;
        BasicBlock whileBlock = new BasicBlock(BBNumber);
        BasicBlockList.add(whileBlock);
        currentBB.transitionList.add(currentBB.new Transitions(currentBB, whileBlock, ""));
        currentBB = whileBlock;

        node.condition().accept(this);

        if (node.condition() instanceof Relation){
            currentBB.add(new IntermediateInstruction(getBranchOperator((Relation) node.condition()), node.condition().getOperand(), currentBB));
        }

        BBNumber++;
        BasicBlock thenBlock = new BasicBlock(BBNumber);
        BasicBlockList.add(thenBlock);
        currentBB.transitionList.add(currentBB.new Transitions(whileBlock, thenBlock, "then"));
        currentBB = thenBlock;
        node.statementSeq().accept(this);
        currentBB.add(new IntermediateInstruction(SSAOperator.BRA, whileBlock, null));
        currentBB.transitionList.add(currentBB.new Transitions(thenBlock, whileBlock, ""));

        BBNumber++;
        BasicBlock elseBlock = new BasicBlock(BBNumber);
        BasicBlockList.add(elseBlock);
        currentBB.transitionList.add(currentBB.new Transitions(whileBlock, elseBlock, "else"));
        currentBB = thenBlock;
    }

    @Override
    public void visit(RepeatStatement node) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void visit(ReturnStatement node) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void visit(StatementSequence node) {
        for (Statement s : node.statSeq) { // TODO: make statement sequence iterable
            s.accept(this);
        }
    }

    @Override
    public void visit(VariableDeclaration node) {
        node.symbol().accept(this);
    }

    @Override
    public void visit(FunctionBody node) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void visit(FunctionDeclaration node) {
        currentBB = new BasicBlock(BBNumber, node.name());
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
        currentBB = new BasicBlock(BBNumber, "main");
        BasicBlockList.add(currentBB);
        node.variables().accept(this);
        node.functions().accept(this);
        node.mainStatementSequence().accept(this);
    }

    @Override
    public void visit(Symbol node) {
        // TODO Auto-generated method stub
        
    }
}
