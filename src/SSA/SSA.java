package SSA;

import java.util.ArrayList;
import java.util.List;

import ast.*;
import pl434.Symbol;
import SSA.IntermediateInstruction.SSAOperator;

public class SSA implements NodeVisitor{
//public class SSA{

    // will contain a List<IntermediateInstruction> as a member
    // will walk through the AST and generate this list

    private List<BasicBlock> BasicBlockList; 
    private BasicBlock currentBB;
    
    public SSA(AST ast){
        BasicBlockList = new ArrayList<BasicBlock>();
        visit(ast.computation);
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
        // TODO Auto-generated method stub
        
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
        // TODO Auto-generated method stub
        
    }

    @Override
    public void visit(IfStatement node) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void visit(WhileStatement node) {
        // TODO Auto-generated method stub
        
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
        currentBB = new BasicBlock(node.name());
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
        currentBB = new BasicBlock("main");
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
