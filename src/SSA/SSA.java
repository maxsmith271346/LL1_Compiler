package SSA;

import java.util.ArrayList;
import java.util.List;

import ast.*;
import pl434.Symbol;

public class SSA implements NodeVisitor{
//public class SSA{

    // will contain a List<IntermediateInstruction> as a member
    // will walk through the AST and generate this list

    private List<BasicBlock> BasicBlockList; 
    private BasicBlock currentBB;
    
    public SSA(AST ast){
        BasicBlockList = new ArrayList<BasicBlock>();
        visit(ast.computation);
        // generate a base SSA with the following instructions: 
        // 0: ADD b c 
        // 1: ADD (0) d 
        // 2: MOVE (1) a
        /*IntermediateInstructionList.add(new IntermediateInstruction("ADD", new Symbol("b", "int", "var"), new Symbol("c", "int", "var")));
        IntermediateInstructionList.add(new IntermediateInstruction("ADD", new InstructionNumber(0), new Symbol("d", "int", "var")));
        IntermediateInstructionList.add(new IntermediateInstruction("MOVE", new InstructionNumber(1), new Symbol("a", "int", "var")));
        */
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
        // TODO Auto-generated method stub
        
    }

    @Override
    public void visit(Multiplication node) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void visit(Division node) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void visit(Modulo node) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void visit(LogicalAnd node) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void visit(Addition node) {
        node.leftExpression().accept(this);
        node.rightExpression().accept(this);

        node.setInsNumber(currentBB.add(new IntermediateInstruction("ADD", node.leftExpression().getOperand(),  node.rightExpression().getOperand())));
    }

    @Override
    public void visit(Subtraction node) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void visit(LogicalOr node) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void visit(Relation node) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void visit(Assignment node) {
        node.lhsDesignator().accept(this);
        node.rhsExpr().accept(this);
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
        System.out.println("in computation");
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
