package SSA;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import SSA.BasicBlock.Transitions;
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
                        // TODO: ?
                        break;
                    case POW:
                        // TODO:
                        break;
                    default:                    
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

    // TODO:
    public Boolean hasOpnd() {
        return false;
    }

    // Max
    public void constantFolding(){
        // fold constant expression 
        // for folding relations: 
        // remove unreachable code here

        for (BasicBlock bb : ssa.getBasicBlockList()) {
            for (IntermediateInstruction i : bb.getIntInsList()) {
                Operand opnd1 = i.getOperandOne();
                Operand opnd2 = i.getOperandTwo();
                // continue if either operands is non-constant
                if (!(isConst(opnd1) && opnd2 != null && isConst(opnd2))) { continue; }
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
                        if (isIntLit(opnd1)) {
                            Integer sum = ((IntegerLiteral) opnd1).valueAsInt() + ((IntegerLiteral) opnd2).valueAsInt();
                            IntegerLiteral intLit = new IntegerLiteral(-1, -1, Integer.toString(sum));
                            i.putOperandOne(intLit);
                        }
                        else if (isFloatLit(opnd1)) {
                            Float sum = ((FloatLiteral) opnd1).valueAsFloat() + ((FloatLiteral) opnd2).valueAsFloat();
                            FloatLiteral intLit = new FloatLiteral(-1, -1, Float.toString(sum));
                            i.putOperandOne(intLit);
                        }
                        i.putOperator(SSAOperator.NONE);
                        i.putOperandTwo(null);
                        break;
                    case SUB:
                        if (isIntLit(opnd1)) {
                            Integer sum = ((IntegerLiteral) opnd1).valueAsInt() - ((IntegerLiteral) opnd2).valueAsInt();
                            IntegerLiteral intLit = new IntegerLiteral(-1, -1, Integer.toString(sum));
                            i.putOperandOne(intLit);
                        }
                        else if (isFloatLit(opnd1)) {
                            Float sum = ((FloatLiteral) opnd1).valueAsFloat() - ((FloatLiteral) opnd2).valueAsFloat();
                            FloatLiteral intLit = new FloatLiteral(-1, -1, Float.toString(sum));
                            i.putOperandOne(intLit);
                        }
                        i.putOperator(SSAOperator.NONE);
                        i.putOperandTwo(null);
                        break;
                    case MUL:
                        if (isIntLit(opnd1)) {
                            Integer sum = ((IntegerLiteral) opnd1).valueAsInt() * ((IntegerLiteral) opnd2).valueAsInt();
                            IntegerLiteral intLit = new IntegerLiteral(-1, -1, Integer.toString(sum));
                            i.putOperandOne(intLit);
                        }
                        else if (isFloatLit(opnd1)) {
                            Float sum = ((FloatLiteral) opnd1).valueAsFloat() * ((FloatLiteral) opnd2).valueAsFloat();
                            FloatLiteral intLit = new FloatLiteral(-1, -1, Float.toString(sum));
                            i.putOperandOne(intLit);
                        }
                        i.putOperator(SSAOperator.NONE);
                        i.putOperandTwo(null);
                        break;
                    case DIV:
                        if (isIntLit(opnd1)) {
                            Integer sum = ((IntegerLiteral) opnd1).valueAsInt() / ((IntegerLiteral) opnd2).valueAsInt();
                            IntegerLiteral intLit = new IntegerLiteral(-1, -1, Integer.toString(sum));
                            i.putOperandOne(intLit);
                        }
                        else if (isFloatLit(opnd1)) {
                            Float sum = ((FloatLiteral) opnd1).valueAsFloat() / ((FloatLiteral) opnd2).valueAsFloat();
                            FloatLiteral intLit = new FloatLiteral(-1, -1, Float.toString(sum));
                            i.putOperandOne(intLit);
                        }
                        i.putOperator(SSAOperator.NONE);
                        i.putOperandTwo(null);
                        break;
                    case MOD:
                        if (isIntLit(opnd1)) {
                            Integer sum = ((IntegerLiteral) opnd1).valueAsInt() % ((IntegerLiteral) opnd2).valueAsInt();
                            IntegerLiteral intLit = new IntegerLiteral(-1, -1, Integer.toString(sum));
                            i.putOperandOne(intLit);
                        }
                        else if (isFloatLit(opnd1)) {
                            Float sum = ((FloatLiteral) opnd1).valueAsFloat() % ((FloatLiteral) opnd2).valueAsFloat();
                            FloatLiteral intLit = new FloatLiteral(-1, -1, Float.toString(sum));
                            i.putOperandOne(intLit);
                        }
                        i.putOperator(SSAOperator.NONE);
                        i.putOperandTwo(null);
                        break;
                    case POW:
                        Integer sum = (int) Math.pow(((IntegerLiteral) opnd1).valueAsInt(), ((IntegerLiteral) opnd2).valueAsInt());
                        IntegerLiteral intLit = new IntegerLiteral(-1, -1, Integer.toString(sum));
                        i.putOperandOne(intLit);
                        i.putOperator(SSAOperator.NONE);
                        i.putOperandTwo(null);
                        break;
                    // TODO: 
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
                        if (((BoolLiteral) opnd1).valueAsBool()) {
                            // Branch taken
                        }
                        else {
                            // Branch untaken
                        }
                        break;
                    default:
                        break;
                    
                }
            }
        }
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
        HashMap<BasicBlock, Integer> exitSetCount = new HashMap<BasicBlock, Integer>();


        // HashSet<BasicBlock> reachable = new HashSet<BasicBlock>();
        HashMap<BasicBlock, Boolean> discovered = new HashMap<BasicBlock, Boolean>();
        LinkedList<BasicBlock> q = new LinkedList<BasicBlock>();

        Set<BasicBlock> bbList = ssa.getBasicBlockList();

        // BasicBlock v;

        // q.add(ssa.getEndBB());



        // // initialize all nodes except 
        // for (BasicBlock bb : bbList) {
        //     if (bb == ssa.getEndBB()) {
        //         discovered.put(bb, true);
        //     }
        //     else {
        //         discovered.put(bb, false);
        //     }
        // }

        // while (!q.isEmpty()) {
        //     v = q.pop();
        //     // if not discovered, visit node and add to reachable
        //     if (!discovered.get(v)) {
        //         // label v as discovered
        //         discovered.put(v, true);
        //         reachable.add(v);
        //         for (Transitions t : v.transitionList) {
        //             q.push(t.toBB);
        //         }
        //     }
        // }


// ====================================================================

        for (BasicBlock bb : ssa.getBasicBlockList()) {
            exitSetCount.put(bb, 0);
            for (Transitions t : bb.transitionList) {
                t.toBB.addInEdge(bb);
            }
        }

        // Ensure proper visiting order for global liveness analysis
        
        Boolean change = false;
        BasicBlock bb = ssa.getEndBB();

         do {
            // System.out.println("print");
            change = false;
            for (BasicBlock block: bbList) {
                change |= block.liveAnalysis();
            }
        } while (change);

        // DCE
        for (BasicBlock block: bbList) {
            for (int i = block.getIntInsList().size()-1; i >= 0; i--) {
                IntermediateInstruction ii = block.getIntInsList().get(i);
                HashSet<Operand> live = ii.getLiveVars();
                switch (ii.getOperator()) {
                    case ADDA:
                        break;
                    
                    
                    case BEQ:
                    case BGE:
                    case BGT:
                    case BLE:
                    case BLT:
                    case BNE:
                    case BRA:
                        break;

                    case CALL:  //TODO:

                        break;
                    case END:
                        break;
                    case LOAD:
                        break;
                    case NEG:
                        break;
                    case NONE:
                        break;
                    case NOT:
                        break;
                    case PHI:
                        break;
                    case RET:
                        break;
                    case STORE:
                        break;

                    case ADD:
                    case AND:
                    case CMP:
                    case DIV:
                    case MOD:
                    case MUL:
                    case OR:
                    case POW:
                    case SUB:
                        if (!live.contains(ii.instNum())) {  // result not used later
                            ii.eliminate();
                        }
                        break;

                    case READ:
                    case READ_B:
                    case READ_F:
                        if (!live.contains(ii.instNum())) {  // result not used later
                            ii.eliminate();
                        }
                        break;
                    
                    case WRITE:
                    case WRITE_B:
                    case WRITE_F:
                        break;

                    case MOVE:
                        if (!live.contains(ii.getOperandTwo())) {  // result not used later
                            ii.eliminate();
                        }
                        break;
                    
                    default:
                        break;
                    
                }
            }
        }
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
