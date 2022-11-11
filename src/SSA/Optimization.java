package SSA;

import SSA.IntermediateInstruction.SSAOperator;
import ast.BoolLiteral;
import ast.FloatLiteral;
import ast.IntegerLiteral;

public class Optimization { 
    private SSA ssa;

    public Optimization(SSA ssa){
        this.ssa = ssa; 
    }

    // Max

    public Boolean isConst(Operand opnd) {
        return (opnd instanceof BoolLiteral) || (opnd instanceof IntegerLiteral) || (opnd instanceof FloatLiteral);
    }
    
    public Boolean isIntLit(Operand opnd) {
        return opnd instanceof IntegerLiteral;
    }

    public Boolean isFloatLit(Operand opnd) {
        return opnd instanceof FloatLiteral;
    }

    public Boolean isBoolLit(Operand opnd) {
        return opnd instanceof BoolLiteral;
    }

    public Boolean numericOpndEquals(Operand opnd, int num) {
        return isIntLit(opnd) && ((IntegerLiteral) opnd).valueAsInt() == num || 
            isFloatLit(opnd) && ((FloatLiteral) opnd).valueAsFloat() == num;
    }

    public void arithmeticSimplification(){
        // replace mul by add 
        // remove arithmetic identity
        // resolve self-subtraction, self-division
        // Expressions that result in 0: mul 0, div 0 by X

        // switch case on different expression types 
        // ex: if multiplication and either of the operands are 0, then replace with 0

        for (BasicBlock bb : ssa.getBasicBlockList()) {
            for (IntermediateInstruction i : bb.getIntInsList()) {
                Operand opnd1 = i.getOperandOne();
                Operand opnd2 = i.getOperandTwo();
                // continue if both operands are constants (should be taken care of in const. folding)
                if (isConst(opnd1) && opnd2 != null && isConst(opnd2)) { continue; }
                switch (i.getOperator()) {
                    // case NOT:
                        // TODO: move to const folding
                        // Operand opnd = i.getOperandOne();
                        // if (isConst(opnd)) {
                        //     ((BoolLiteral) opnd).setBoolValue(!(((BoolLiteral) opnd).valueAsBool()));
                        // }
                        // break;
                    case AND:
                        break;
                    case OR:
                        break;

                    case ADD:
                        if (numericOpndEquals(opnd1, 0)) {
                            i.putOperator(SSAOperator.NONE);
                            i.putOperandOne(i.getOperandTwo());
                            i.putOperandTwo(null);
                        }
                        else if (numericOpndEquals(opnd2, 0)) {
                            i.putOperator(SSAOperator.NONE);
                            i.putOperandTwo(null);
                        }
                        // else if (isFloatLit(opnd1) && ((FloatLiteral) opnd1).valueAsFloat() == 0) {
                        //     i.putOperator(SSAOperator.NONE);
                        //     i.putOperandOne(i.getOperandTwo());
                        //     i.putOperandTwo(null);
                        // }
                        // else if (isFloatLit(opnd2) && ((FloatLiteral) opnd2).valueAsFloat() == 0) {
                        //     i.putOperator(SSAOperator.NONE);
                        //     i.putOperandTwo(null);
                        // }
                        break;
                    case SUB:
                        if (numericOpndEquals(opnd2, 0)) {
                            i.putOperator(SSAOperator.NONE);
                            i.putOperandTwo(null);
                        }
                        // else if (isFloatLit(opnd2) && ((FloatLiteral) opnd2).valueAsFloat() == 0) {
                        //     i.putOperator(SSAOperator.NONE);
                        //     i.putOperandTwo(null);
                        // }
                        else if (!isConst(opnd1) && !isConst(opnd2) && opnd1 == opnd2) {
                            i.putOperator(SSAOperator.NONE);
                            Operand newOpnd = isIntLit(opnd1) ? (new IntegerLiteral(-1, -1, "0")) : (new FloatLiteral(-1, -1, "0.0"));
                            i.putOperandOne(newOpnd);
                            i.putOperandTwo(null);
                        }
                        break;
                    case MUL:
                        if (numericOpndEquals(opnd1, 0) || numericOpndEquals(opnd2, 0)) {
                            i.putOperator(SSAOperator.NONE);
                            Operand newOpnd = isIntLit(opnd1) ? (new IntegerLiteral(-1, -1, "0")) : (new FloatLiteral(-1, -1, "0.0"));
                            i.putOperandOne(newOpnd);
                            i.putOperandTwo(null);
                        }
                        else if (numericOpndEquals(opnd1, 1)) {
                            i.putOperator(SSAOperator.NONE);
                            i.putOperandOne(i.getOperandTwo());
                            i.putOperandTwo(null);
                        }
                        else if (numericOpndEquals(opnd2, 1)) {
                            i.putOperator(SSAOperator.NONE);
                            i.putOperandTwo(null);
                        }
                        else if (numericOpndEquals(opnd1, 2)) {
                            i.putOperator(SSAOperator.ADD);
                            i.putOperandOne(i.getOperandTwo());
                        }
                        else if (numericOpndEquals(opnd2, 2)) {
                            i.putOperator(SSAOperator.ADD);
                            i.putOperandTwo(i.getOperandOne());
                        }
                    break;
                    case DIV:
                        if (numericOpndEquals(opnd1, 0)) {
                            i.putOperator(SSAOperator.NONE);
                            Operand newOpnd = isIntLit(opnd1) ? (new IntegerLiteral(-1, -1, "0")) : (new FloatLiteral(-1, -1, "0.0"));
                            i.putOperandOne(newOpnd);
                            i.putOperandTwo(null);
                        }
                        else if (numericOpndEquals(opnd2, 1)) {
                            i.putOperator(SSAOperator.NONE);
                            i.putOperandTwo(null);
                        }
                        else if (!isConst(opnd1) && !isConst(opnd2) && opnd1 == opnd2) {
                            i.putOperator(SSAOperator.NONE);
                            Operand newOpnd = isIntLit(opnd1) ? (new IntegerLiteral(-1, -1, "1")) : (new FloatLiteral(-1, -1, "1.0"));
                            i.putOperandOne(newOpnd);
                            i.putOperandTwo(null);
                        }
                        break;
                    case MOD:
                        break;
                    case POW:
                        
                        break;
                    
                    case BEQ:
                        break;
                    case BGE:
                        break;
                    case BGT:
                        break;
                    case BLE:
                        break;
                    case BLT:
                        break;
                    case BNE:
                        break;
                    default:
                        break;
                    
                }
            }
        }

    }

    // Emory
    public void unitializedVariables(){
        // warning on uninitialized vars 
        // explicit IR code to set var to 0
        // go through SSA, anytime find subscript with negative, then 
        // handle this in SSA generation 
    }

    // Emory
    public void constantPropagation(){
        // available expression analysis
    }

    // Max
    public void constantFolding(){
        // fold constant expression 
        // for folding relations: 
        // remove unreachable code here
    }

    // Emory 
    public void copyPropagation(){
        // available expression analysis
    }
       
    // Emory 
    public void commonSubexpressionElimination(){
        // available expression analysis
    }

    // Max
    public void deadCodeElimination(){
        // liveness analysis
    }

    // Max
    public void orphanFunctionElimination(){
        // global map of whether it was used 
        // generate this map during ssa gen 
    }

    // Emory
    public void availableExpressionAnalysis(){
        // each instruction keep track of the set of expressions available prior 
    }
}
