package pl434;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import SSA.BasicBlock;
import SSA.IntermediateInstruction;
import SSA.Operand;
import SSA.SSA;
import SSA.IntermediateInstruction.SSAOperator;

public class CodeGenerator {
    private SSA ssa; 
    List<Integer> instructions; 
    Integer spillRegOne; 
    Integer spillRegTwo;
    Integer spilledRegToUse; // flop between the spill insertRegisters
    Operand spillRegOneOperand; 
    Operand spillRegTwoOperand;
    int globalVarOffset;
    HashMap<String, Integer> OperandsToOffset;
    
    public CodeGenerator(SSA ssa, int numRegs){
        this.ssa = ssa;
        instructions = new ArrayList<Integer>();
        spillRegOne = numRegs - 1;
        spillRegTwo = numRegs - 2;
        spilledRegToUse = spillRegTwo;
        globalVarOffset = -4;

        generateCode();
    }

    public void generateCode(){
        // write a code generator for the DLX processor
        for (BasicBlock bb : ssa.getBasicBlockList()){
            if (bb.name().contains("elim")){continue;}
            for (IntermediateInstruction ii : bb.getIntInsList()){
                resolveAnySpilledOperands(ii);
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
                        int storeIns = -1;
                        if (ii.getOperator() == SSAOperator.MOVE){
                            if (ii.getRegisterTwo() == null){ 
                                // need to use one of the spilled registers for the result of the ADD and then store the register into memory
                                ii.putRegisterTwo(spilledRegToUse);
                                int offset; 
                                // check if the var already has an offset 
                                //storeIns = DLX.assemble(DLX.STW, spilledRegToUse.value(), 30, )
                            }
                        }
                    default: 
                        break;
                }
                resolveAnySpilledResults(ii);
            }
        }
    }


    public void resolveAnySpilledOperands(IntermediateInstruction ii){
        if (ii.getOperator() != SSAOperator.MOVE){
            if (ii.getOperandOne() != null && ii.getRegisterOne() == null){ // if the ins has an operand but no associated register
                //instructions.add(DLX.LDW, 30)
            }
        }
    }

    public void resolveAnySpilledResults(IntermediateInstruction ii){
        if (ii.getOperator() == SSAOperator.MOVE){
            if (ii.getRegisterTwo() == null){  // if the 
            
            }
        }
    }
}

