package SSA;

import java.util.ArrayList;
import java.util.List;

import ast.AST;
import ast.NodeVisitor;
import pl434.Symbol;

//public class SSAGenerator implements NodeVisitor{
public class SSA{

    // will contain a List<IntermediateInstruction> as a member
    // will walk through the AST and generate this list

    List<IntermediateInstruction> IntermediateInstructionList; 
    
    public SSA(AST ast){
        IntermediateInstructionList = new ArrayList<IntermediateInstruction>();

        // generate a base SSA with the following instructions: 
        // 0: ADD b c 
        // 1: ADD (0) d 
        // 2: MOVE (1) a
        IntermediateInstructionList.add(new IntermediateInstruction("ADD", new Symbol("b", "int", "var"), new Symbol("c", "int", "var")));
        IntermediateInstructionList.add(new IntermediateInstruction("ADD", new InstructionNumber(0), new Symbol("d", "int", "var")));
        IntermediateInstructionList.add(new IntermediateInstruction("MOVE", new InstructionNumber(1), new Symbol("a", "int", "var")));

    }

    public String asDotGraph(){
        IRVisualizer IRVis = new IRVisualizer(IntermediateInstructionList);
        return IRVis.generateDotGraph();
    }
}
