package pl434;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import SSA.BasicBlock;
import SSA.InstructionNumber;
import SSA.IntermediateInstruction;
import SSA.Operand;
import SSA.SSA;
import SSA.BasicBlock.Transitions;
import SSA.IntermediateInstruction.SSAOperator;
import ast.*;
import types.BoolType;
import types.FloatType;
import types.IntType;


//TODO List
// fix type checker to allow for printFloat as a variable name 
// add user defined functions to code gen 
// add functionality to have bool a = 3 == 2

public class CodeGenerator {
    private SSA ssa; 
    public List<Integer> instructions; 
    private Integer spillRegOne; 
    private Integer spillRegTwo;
    private Operand spillRegOneOperand; 
    private Operand spillRegTwoOperand;
    private int globalVarOffset;
    private HashMap<String, Integer> operandsToOffset;
    private int spillResultLoadIns;
    private HashMap<BasicBlock, List<Integer>> branchesToFix;
    
    public CodeGenerator(SSA ssa, int numRegs){
        this.ssa = ssa;
        instructions = new ArrayList<Integer>();
        spillRegOne = numRegs - 1;
        spillRegTwo = numRegs - 2;
        globalVarOffset = -4;
        operandsToOffset = new HashMap<String, Integer>();
        spillResultLoadIns = -1;
        branchesToFix = new HashMap<BasicBlock, List<Integer>>();

        generateCode();

    }

    public void generateCode(){
        // have to visit the BBs in order
        List<BasicBlock> BBList = getChildren(ssa.rootBB);

        allocateGlobalVars();
        // write a code generator for the DLX processor
        for (BasicBlock bb : BBList){ // NEED TO TRAVERSE THESE IN ORDER
            if (bb.name().contains("elim")){continue;}
            
            if (branchesToFix.containsKey(bb)){
                fixBranchInstruction(branchesToFix.get(bb));
            }
            else{ 
                List<Integer> bbLoc = new ArrayList<Integer>();
                bbLoc.add(instructions.size());
                branchesToFix.put(bb, bbLoc);
            }

            for (IntermediateInstruction ii : bb.getIntInsList()){
                if (ii.isElim()){continue;}

                List<Integer> instructionPieces;

                resolveAnySpilledOperands(ii);
                spillResultLoadIns = -1;
                resolveAnySpilledResults(ii);
                switch(ii.getOperator()){
                    case READ_I:
                        instructions.add(DLX.assemble(DLX.RDI, ii.returnReg));
                        break;
                    case READ_F:
                        instructions.add(DLX.assemble(DLX.RDF, ii.returnReg));
                        break;
                    case READ_B:
                        instructions.add(DLX.assemble(DLX.RDB, ii.returnReg));
                        break;
                    case MOVE: 
                        if (ii.getOperator() == SSAOperator.MOVE){
                            // if there is no register allocated to the lhs, then we will need to have a store instruction after the ADD
                            if (IntermediateInstruction.isConst(ii.getOperandOne())){ 
                                if (ii.getOperandOne() instanceof FloatLiteral){
                                    instructions.add(DLX.assemble(DLX.fADDI, ii.getRegisterTwo(), 0, Float.parseFloat(((FloatLiteral) ii.getOperandOne()).value())));
                                }
                                else if (ii.getOperandOne() instanceof IntegerLiteral){
                                    instructions.add(DLX.assemble(DLX.ADDI, ii.getRegisterTwo(), 0, Integer.parseInt(((IntegerLiteral) ii.getOperandOne()).value())));
                                }
                                else{
                                    instructions.add(DLX.assemble(DLX.ADDI, ii.getRegisterTwo(), 0, (Boolean.parseBoolean(((BoolLiteral) ii.getOperandOne()).value()) ? 1 : 0)));
                                }
                            }

                            else if (ii.getOperandTwo() instanceof Symbol){
                                if (((Symbol) ii.getOperandTwo()).getType() instanceof FloatType){
                                    instructions.add(DLX.assemble(DLX.fADD, ii.getRegisterTwo(), 0, ii.getRegisterOne()));
                                } 
                                else {
                                    instructions.add(DLX.assemble(DLX.ADD, ii.getRegisterTwo(), 0, ii.getRegisterOne()));
                                }
                            }
                        }
                        break;
                    case WRITE: 
                        if (ii.getOperandOne() == null){
                            instructions.add(DLX.assemble(DLX.WRL));
                        }
                        else if (IntermediateInstruction.isConst(ii.getOperandOne())){
                            if (ii.getOperandOne() instanceof FloatLiteral){
                                instructions.add(DLX.assemble(DLX.fADDI, spillRegOne, 0, Float.parseFloat(((FloatLiteral) ii.getOperandOne()).value())));
                                instructions.add(DLX.assemble(DLX.WRF, spillRegOne));
                            }
                            else if (ii.getOperandOne() instanceof IntegerLiteral){
                                instructions.add(DLX.assemble(DLX.ADDI, spillRegOne, 0, Integer.parseInt(((IntegerLiteral) ii.getOperandOne()).value())));
                                instructions.add(DLX.assemble(DLX.WRI, spillRegOne));
                            }
                            else{
                                instructions.add(DLX.assemble(DLX.ADDI, ii.getRegisterTwo(), 0, (Boolean.parseBoolean(((BoolLiteral) ii.getOperandOne()).value()) ? 1 : 0)));
                                instructions.add(DLX.assemble(DLX.WRB, spillRegOne));
                            }
                        }
                        else if (ii.getOperandOne() instanceof Symbol){ 
                            if (((Symbol)ii.getOperandOne()).type() instanceof FloatType){
                                instructions.add(DLX.assemble(DLX.WRF, ii.getRegisterOne()));
                            }
                            else if (((Symbol)ii.getOperandOne()).type() instanceof IntType){
                                instructions.add(DLX.assemble(DLX.WRI, ii.getRegisterOne()));
                            }
                            else{
                                instructions.add(DLX.assemble(DLX.WRB, ii.getRegisterOne()));
                            }
                        }
                        
                        else if (ii.getOperandOne() instanceof InstructionNumber){
                            if (((InstructionNumber)ii.getOperandOne()).type() instanceof FloatType){
                                instructions.add(DLX.assemble(DLX.WRF, ii.getRegisterOne()));
                            }
                            else if (((InstructionNumber)ii.getOperandOne()).type() instanceof IntType){
                                instructions.add(DLX.assemble(DLX.WRI, ii.getRegisterOne()));
                            }
                            else{
                                instructions.add(DLX.assemble(DLX.WRB, ii.getRegisterOne()));
                            }
                        }
                        break;
                    case DIV: 
                        handleArithmeticOperations(ii, DLX.DIV, DLX.DIVI, DLX.fDIV, DLX.fDIVI, false);
                        break;
                    case MUL: 
                        handleArithmeticOperations(ii, DLX.MUL, DLX.MULI, DLX.fMUL, DLX.fMULI, true);
                        break;
                    case ADD: 
                        handleArithmeticOperations(ii, DLX.ADD, DLX.ADDI, DLX.fADD, DLX.fADDI, true);
                        break;
                    case SUB: 
                        handleArithmeticOperations(ii, DLX.SUB, DLX.SUBI, DLX.fSUB, DLX.fSUBI, false);
                        break;
                    case MOD: 
                        handleArithmeticOperations(ii, DLX.MOD, DLX.MODI, DLX.fMOD, DLX.fMODI, false);
                        break;
                    case CMP: 
                        handleArithmeticOperations(ii, DLX.CMP, DLX.CMPI, DLX.fCMP, DLX.fCMPI, false);
                        if (usedInNonBranch(ii)){
                            convertFromCMPToBool(ii);
                        }
                        break;
                    case AND: 
                        handleArithmeticOperations(ii, DLX.AND, DLX.ANDI, true);
                        break;
                    case OR: 
                        handleArithmeticOperations(ii, DLX.OR, DLX.ORI, true);
                        break;
                    case POW: 
                        handleArithmeticOperations(ii, DLX.POW, DLX.POWI, false);
                        break;
                    case NOT: 
                        //TODO
                        break;
                    case BRA: 
                        if (branchesToFix.containsKey((BasicBlock) ii.getOperandOne())){
                            instructions.add(DLX.assemble(DLX.BEQ, 0, branchesToFix.get((BasicBlock) ii.getOperandOne()).get(0) - (instructions.size())));
                        }   
                        else{
                            instructions.add(0);
                            instructionPieces = new ArrayList<Integer>();
                            instructionPieces.add(DLX.BEQ); 
                            instructionPieces.add(0);
                            instructionPieces.add(instructions.size() - 1);
                            branchesToFix.put((BasicBlock) ii.getOperandOne(), instructionPieces);
                        }
                        
                        break; 
                    case BNE: 
                        generateBranchIns(ii, DLX.BNE);
                        break; 
                    case BEQ: 
                        generateBranchIns(ii, DLX.BEQ);
                        break; 
                    case BLE: 
                        generateBranchIns(ii, DLX.BLE);
                        break; 
                    case BLT: 
                        generateBranchIns(ii, DLX.BLT);
                        break; 
                    case BGE: 
                        generateBranchIns(ii, DLX.BGE);
                        break; 
                    case BGT: 
                        generateBranchIns(ii, DLX.BGT);
                        break; 
                    case CALL: 
                        break; 
                    case RET: 
                        break;
                    default: 
                        break;
                }
                if (spillResultLoadIns != -1){
                    instructions.add(spillResultLoadIns);
                }
            }
        }
        instructions.add(DLX.assemble(DLX.RET, 0));
    }

    public List<BasicBlock> getChildren(BasicBlock bb){
        
        List<BasicBlock> children = new ArrayList<BasicBlock>();
        children.add(bb);
        BasicBlock thenBlock = null; 
        BasicBlock elseBlock = null; 
        BasicBlock otherBlock = null;

        for (Transitions t : bb.transitionList){
            if (t.label.contains("then")){
                thenBlock = t.toBB;
            }
            else if (t.label.contains("else")){ 
                elseBlock = t.toBB;
            }
            else{ 
                if(!t.backEdge){
                    otherBlock = t.toBB;
                }
            }
        }

        if (thenBlock != null && elseBlock != null){
            children.addAll(getChildren(thenBlock));
            children.addAll(getChildren(elseBlock));
        }

        if (otherBlock != null){
            children.addAll(getChildren(otherBlock));
        }

        if (bb.equals(ssa.rootBB)){
            while(children.size() > ssa.getBasicBlockList().size()){
                for (BasicBlock block : ssa.getBasicBlockList()){
                    if (Collections.frequency(children, block) > 1){
                        children.remove(block);
                    }
                }
            }
        }
        return children;
    }

    public void resolveAnySpilledOperands(IntermediateInstruction ii){
        if (ii.getOperandOne() != null && ii.getRegisterOne() == null){ // if the ins has an operand but no associated register
            if (!IntermediateInstruction.isConst(ii.getOperandOne()) && !(ii.getOperandOne() instanceof BasicBlock)){
                ii.putRegisterOne(spillRegOne);
                instructions.add(DLX.assemble(DLX.LDW, spillRegOne, 30, getOffset(ii.getOperandOne())));
            }
        }
        if (ii.getOperandTwo() != null && ii.getRegisterTwo() == null){
            if (ii.getOperator() != SSAOperator.MOVE){
                if (!IntermediateInstruction.isConst(ii.getOperandTwo()) && !(ii.getOperandTwo() instanceof BasicBlock)){
                    ii.putRegisterTwo(spillRegTwo);
                    instructions.add(DLX.assemble(DLX.LDW, spillRegTwo, 30, getOffset(ii.getOperandTwo())));
                }
            }
        }
    }

    public void resolveAnySpilledResults(IntermediateInstruction ii){
        if (ii.getOperator() == SSAOperator.MOVE){
            if (ii.getRegisterTwo() != null){ 
                if (ii.getRegisterTwo() < 0){
                    ii.putRegisterTwo(spillRegTwo);    
                    spillResultLoadIns = DLX.assemble(DLX.STW, spillRegTwo, 30, getOffset(ii.getOperandTwo()));
                }
            }
        }

        if (ii.returnReg == null){
            return;
        }
        if (ii.returnReg < 0){ 
            ii.returnReg = spillRegOne;
            instructions.add(DLX.assemble(DLX.STW, spillRegOne, 30, getOffset(ii.instNum())));
        }
    }

    public String getBaseString(Operand op){
        if (op instanceof Symbol){
            if (op.toString().contains("_")){
                return op.toString().substring(0, op.toString().indexOf("_"));
            }
        }
        return op.toString();
    }

    public int getOffset(Operand op){
        int offset; 
        if (operandsToOffset.containsKey(getBaseString(op))){
            offset = operandsToOffset.get(getBaseString(op));
        }
        else{ 
            offset = globalVarOffset; 
            globalVarOffset -= 4; 
            operandsToOffset.put(getBaseString(op), offset);
        } 
        return offset;       
    }

    public void allocateGlobalVars(){
        int globalVarCount = 0;
        for (Symbol s : ssa.getBasicBlockList().iterator().next().varMap.keySet()){
            if (s.scope == 1){
                globalVarCount++;
            }
        }

        instructions.add(DLX.assemble(DLX.SUBI, 29, 30, globalVarCount * 4));
        instructions.add(DLX.assemble(DLX.ADDI, 28, 29, 0));
    }

    // generic function to handle arithmetic operations given the four different potential operands 
    public void handleArithmeticOperations(IntermediateInstruction intIns, int intRegOp, int intConstOp, int floatRegOp, int floatConstOp, boolean commutative){
        // both are constant, each needs to be stored in a register 
        if (IntermediateInstruction.isConst(intIns.getOperandOne()) && IntermediateInstruction.isConst(intIns.getOperandTwo())){
            if (intIns.getOperandOne() instanceof FloatLiteral){
                instructions.add(DLX.assemble(DLX.fADDI, spillRegOne, 0, Float.parseFloat(((FloatLiteral) intIns.getOperandOne()).value())));
                instructions.add(DLX.assemble(DLX.fADDI, spillRegTwo, 0, Float.parseFloat(((FloatLiteral) intIns.getOperandTwo()).value())));
                instructions.add(DLX.assemble(floatRegOp, intIns.returnReg, spillRegOne, spillRegTwo));
            }
            else if (intIns.getOperandOne() instanceof IntegerLiteral){
                instructions.add(DLX.assemble(DLX.ADDI, spillRegOne, 0, Integer.parseInt(((IntegerLiteral) intIns.getOperandOne()).value())));
                instructions.add(DLX.assemble(DLX.ADDI, spillRegTwo, 0, Integer.parseInt(((IntegerLiteral) intIns.getOperandTwo()).value())));
                instructions.add(DLX.assemble(intRegOp, intIns.returnReg, spillRegOne, spillRegTwo));
            }
        }

        // only the first is constant, can use the operation with the constant c ONLY if the operation is commutative 
        else if (IntermediateInstruction.isConst(intIns.getOperandOne()) && commutative){
            if (intIns.getOperandOne() instanceof FloatLiteral){
                instructions.add(DLX.assemble(floatConstOp, intIns.returnReg, intIns.getRegisterTwo(), Float.parseFloat(((FloatLiteral) intIns.getOperandOne()).value())));
            }
            else if (intIns.getOperandOne() instanceof IntegerLiteral){
                instructions.add(DLX.assemble(intConstOp, intIns.returnReg, intIns.getRegisterTwo(), Integer.parseInt(((IntegerLiteral) intIns.getOperandOne()).value())));
            }
        }


        // if the operation is not commutative, then need to load the first operand into a register
        else if (IntermediateInstruction.isConst(intIns.getOperandOne()) && !commutative){
            if (intIns.getOperandOne() instanceof FloatLiteral){
                instructions.add(DLX.assemble(DLX.fADDI, spillRegOne, 0, Float.parseFloat(((FloatLiteral) intIns.getOperandOne()).value())));
                instructions.add(DLX.assemble(floatRegOp, intIns.returnReg, spillRegOne, intIns.getRegisterTwo()));
            }
            else if (intIns.getOperandOne() instanceof IntegerLiteral){
                instructions.add(DLX.assemble(DLX.ADDI, spillRegOne, 0, Integer.parseInt(((IntegerLiteral) intIns.getOperandOne()).value())));
                instructions.add(DLX.assemble(intRegOp, intIns.returnReg, spillRegOne, intIns.getRegisterTwo()));
            }
        }

        // If the second operand is constant, can use the constant operation, do not have to worry about commutative property here
        else if (IntermediateInstruction.isConst(intIns.getOperandTwo())){
            if (intIns.getOperandTwo() instanceof FloatLiteral){
                instructions.add(DLX.assemble(floatConstOp, intIns.returnReg, intIns.getRegisterOne(), Float.parseFloat(((FloatLiteral) intIns.getOperandTwo()).value())));
            }
            else if (intIns.getOperandTwo() instanceof IntegerLiteral){
                instructions.add(DLX.assemble(intConstOp, intIns.returnReg, intIns.getRegisterOne(), Integer.parseInt(((IntegerLiteral) intIns.getOperandTwo()).value())));
            }
        }

        //neither are constant - can be a combination of symbols & instructionNumbers, both have types associated with them
        else if (intIns.getOperandOne() instanceof Symbol){
            if (((Symbol) intIns.getOperandOne()).type() instanceof FloatType){
                instructions.add(DLX.assemble(floatRegOp, intIns.returnReg, intIns.getRegisterOne(), intIns.getRegisterTwo()));
            }
            else if (((Symbol) intIns.getOperandOne()).type() instanceof IntType){
                instructions.add(DLX.assemble(intRegOp, intIns.returnReg, intIns.getRegisterOne(), intIns.getRegisterTwo()));
            }
        }

        else if (intIns.getOperandTwo() instanceof Symbol){
            if (((Symbol) intIns.getOperandTwo()).type() instanceof FloatType){
                instructions.add(DLX.assemble(floatRegOp, intIns.returnReg, intIns.getRegisterOne(), intIns.getRegisterTwo()));
            }
            else if (((Symbol) intIns.getOperandTwo()).type() instanceof IntType){
                instructions.add(DLX.assemble(intRegOp, intIns.returnReg, intIns.getRegisterOne(), intIns.getRegisterTwo()));
            }
        }
        // both are instruction numbers -- issue: do not have type 
            // need to give instruction numbers types -- done
        else if (intIns.getOperandOne() instanceof InstructionNumber && intIns.getOperandTwo() instanceof InstructionNumber){
            if (((InstructionNumber) intIns.getOperandOne()).type() instanceof FloatType){
                instructions.add(DLX.assemble(floatRegOp, intIns.returnReg, intIns.getRegisterOne(), intIns.getRegisterTwo()));
            }
            else if (((InstructionNumber) intIns.getOperandOne()).type() instanceof IntType){
                instructions.add(DLX.assemble(intRegOp, intIns.returnReg, intIns.getRegisterOne(), intIns.getRegisterTwo()));
            }
        }
    }


    // generic function to handle arithmetic operations given the four different potential operands
    // This one is for the operations with only two "options": POW & POWI; AND & ANDI; only need to consider BOOL & INT 
    public void handleArithmeticOperations(IntermediateInstruction intIns, int RegOp, int ConstOp, boolean commutative){
        // both are constant, each needs to be stored in a register 
        if (IntermediateInstruction.isConst(intIns.getOperandOne()) && IntermediateInstruction.isConst(intIns.getOperandTwo())){
            if (intIns.getOperandOne() instanceof BoolLiteral){
                instructions.add(DLX.assemble(DLX.fADDI, spillRegOne, 0, (Boolean.parseBoolean(((BoolLiteral) intIns.getOperandOne()).value()) ? 1 : 0)));
                instructions.add(DLX.assemble(DLX.fADDI, spillRegTwo, 0, (Boolean.parseBoolean(((BoolLiteral) intIns.getOperandOne()).value()) ? 1 : 0)));
            }
            else if (intIns.getOperandOne() instanceof IntegerLiteral){
                instructions.add(DLX.assemble(DLX.ADDI, spillRegOne, 0, Integer.parseInt(((IntegerLiteral) intIns.getOperandOne()).value())));
                instructions.add(DLX.assemble(DLX.ADDI, spillRegTwo, 0, Integer.parseInt(((IntegerLiteral) intIns.getOperandTwo()).value())));
            }
            instructions.add(DLX.assemble(RegOp, intIns.returnReg, spillRegOne, spillRegTwo));
        }

        // only the first is constant, can use the operation with the constant c ONLY if the operation is commutative 
        else if (IntermediateInstruction.isConst(intIns.getOperandOne()) && commutative){
            if (intIns.getOperandOne() instanceof BoolLiteral){
                instructions.add(DLX.assemble(ConstOp, intIns.returnReg, intIns.getRegisterTwo(), (Boolean.parseBoolean(((BoolLiteral) intIns.getOperandOne()).value()) ? 1 : 0)));
            }
            else if (intIns.getOperandOne() instanceof IntegerLiteral){
                instructions.add(DLX.assemble(ConstOp, intIns.returnReg, intIns.getRegisterTwo(), Integer.parseInt(((IntegerLiteral) intIns.getOperandOne()).value())));
            }
        }


        // if the operation is not commutative, then need to load the first operand into a register
        else if (IntermediateInstruction.isConst(intIns.getOperandOne()) && !commutative){
            if (intIns.getOperandOne() instanceof BoolLiteral){
                instructions.add(DLX.assemble(DLX.fADDI, spillRegOne, 0, (Boolean.parseBoolean(((BoolLiteral) intIns.getOperandOne()).value()) ? 1 : 0)));
                instructions.add(DLX.assemble(RegOp, intIns.returnReg, spillRegOne, intIns.getRegisterTwo()));
            }
            else if (intIns.getOperandOne() instanceof IntegerLiteral){
                instructions.add(DLX.assemble(DLX.ADDI, spillRegOne, 0, Integer.parseInt(((IntegerLiteral) intIns.getOperandOne()).value())));
                instructions.add(DLX.assemble(RegOp, intIns.returnReg, spillRegOne, intIns.getRegisterTwo()));
            }
        }

        // If the second operand is constant, can use the constant operation, do not have to worry about commutative property here
        else if (IntermediateInstruction.isConst(intIns.getOperandTwo())){
            if (intIns.getOperandTwo() instanceof BoolLiteral){
                instructions.add(DLX.assemble(ConstOp, intIns.returnReg, intIns.getRegisterOne(), (Boolean.parseBoolean(((BoolLiteral) intIns.getOperandOne()).value()) ? 1 : 0)));
            }
            else if (intIns.getOperandTwo() instanceof IntegerLiteral){
                instructions.add(DLX.assemble(ConstOp, intIns.returnReg, intIns.getRegisterOne(), Integer.parseInt(((IntegerLiteral) intIns.getOperandTwo()).value())));
            }
        }

        //neither are constant - can be a combination of symbols & instructionNumbers, both have types associated with them
        else {
            instructions.add(DLX.assemble(RegOp, intIns.returnReg, intIns.getRegisterOne(), intIns.getRegisterTwo()));
        }
    }

    public void generateBranchIns(IntermediateInstruction ii, int op){
        List<Integer> instructionPieces = new ArrayList<Integer>();
        instructions.add(0);
        instructionPieces.add(op); 
        instructionPieces.add(ii.getRegisterOne());
        instructionPieces.add(instructions.size() - 1);
        branchesToFix.put((BasicBlock) ii.getOperandTwo(), instructionPieces);        
    }

    public void fixBranchInstruction(List<Integer> instructionPieces){
        instructions.set(instructionPieces.get(2), DLX.assemble(instructionPieces.get(0), instructionPieces.get(1), instructions.size() - instructionPieces.get(2)));
    }

    public boolean usedInNonBranch(IntermediateInstruction iiTest){
        for (BasicBlock bb : ssa.getBasicBlockList()){
            if (bb.name().contains("elim")){continue;}
            for (IntermediateInstruction ii : bb.getIntInsList()){
                System.out.println("ii " + ii);

                if (ii.isElim()){continue;}
                System.out.println("ii " + ii);
                System.out.println("iitest " + iiTest);
                if (!ii.isBranch()){
                    if (ii.getOperandOne() != null){
                        if (ii.getOperandOne() instanceof InstructionNumber){
                            if (((InstructionNumber) ii.getOperandOne()).getInstructionNumber() == iiTest.insNum()){
                                return true;
                            }
                        }
                    }
                    if (ii.getOperandTwo() != null){
                        if (ii.getOperandTwo() instanceof InstructionNumber){
                            if (((InstructionNumber) ii.getOperandTwo()).getInstructionNumber() == iiTest.insNum()){
                                return true;
                            }
                        }
                    }
                }
                
            }
        }

        return false;
    }

    public void convertFromCMPToBool(IntermediateInstruction intIns){
        if (intIns.cmp.equals("==")){

        }
        else if (intIns.cmp.equals("==")){
            
        }
    }
}

