package SSA;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import SSA.BasicBlock.Transitions;
import SSA.IntermediateInstruction.SSAOperator;
import SSA.SSA.Edge;
import ast.BoolLiteral;
import ast.FloatLiteral;
import ast.IntegerLiteral;
import pl434.Symbol;

public class Optimization { 
    private SSA ssa;

    public Optimization(SSA ssa){
        this.ssa = ssa; 
    }

    public void runUntilConvergence(List<String> optArguments){
        if (optArguments.size() == 0){
            boolean change = true; 
            while(change){
                change = false; 
                // run all of them
                // maybe have each of them return a boolean value that indicates whether or not there were code changes? 
                change |= constantPropagation();
                //change = constantFolding();
                change |= copyPropagation();
                change |= commonSubexpressionElimination();
                //change = deadCodeElimination(); 
                //change = orphanFunctionElimination();
            }

        }
        else{ 
            // only iterate through the requested optimizations
            boolean change = true;
            while(change){
                change = false;
                for (String opt : optArguments){
                    switch(opt){
                        case "cp": 
                            change |= constantPropagation();
                            break;
                        case "cf": 
                            //change |= constantFolding();
                            break;
                        case "cpp": 
                            change |= copyPropagation();
                            break;
                        case "cse": 
                            change |= commonSubexpressionElimination();
                            break;
                        case "dce":
                            //change |= deadCodeElimination(); 
                            break;
                        case "ofe": 
                            change |= orphanFunctionElimination();
                            break; 
                        case "max":
                            break;
                }
            }
        }
    }
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
    public boolean constantPropagation(){
        // available expression analysis
        //generateAvailableExpresssion(ssa);
        boolean change = true;
        int loops = 0;
        while(change){
            loops++;
            generateAvailableExpresssion(ssa);
            change = false;
            for (BasicBlock bb : ssa.getBasicBlockList()){
                for (IntermediateInstruction ii : bb.getIntInsList()){
                    for (IntermediateInstruction iiAvail : ii.availableExpressions){
                        if (iiAvail.getOperator() == SSAOperator.MOVE){
                            if (iiAvail.getOperandOne() instanceof FloatLiteral || iiAvail.getOperandOne() instanceof BoolLiteral || iiAvail.getOperandOne() instanceof IntegerLiteral){
                                // only replace the RHS of a MOVE!
                                if (ii.getOperator() == SSAOperator.MOVE){
                                    if (ii.getOperandOne() instanceof Symbol){
                                        String operandOneName = ((Symbol) ii.getOperandOne()).name();
                                        operandOneName = operandOneName.substring(0, operandOneName.indexOf("_"));
    
                                        String operandName = ((Symbol) iiAvail.getOperandTwo()).name();
                                        operandName = operandName.substring(0, operandName.indexOf("_"));
                                        if (operandName.equals(operandOneName)){
                                            ii.setOperandOne(iiAvail.getOperandOne());
                                            change = true;
                                        }
                                    }
                                }
                                else{ 
                                    if (ii.getOperator() != SSAOperator.PHI){
                                        if(ii.setMatchingOperand(iiAvail.getOperandTwo(), iiAvail.getOperandOne())){
                                            change = true;
                                        }
                                    }
                                }
                            }
                        }
                        else if(iiAvail.getOperator() == SSAOperator.NONE){
                            if (isConst(iiAvail.getOperandOne())){
                                if (ii.getOperator() == SSAOperator.MOVE){
                                    if (ii.getOperandOne() instanceof InstructionNumber){
                                        if (((InstructionNumber) ii.getOperandOne()).getInstructionNumber() == iiAvail.insNum()){
                                            ii.setOperandOne(iiAvail.getOperandOne());
                                            change = true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (loops > 1){ // If there was more than one loop, then there was a code change
            return true;
        }
        return false; // If there was just one loop, then there was no code change
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
                    case NOT:
                        if (isBoolLit(opnd1)){
                            Boolean not = !((BoolLiteral) opnd1).valueAsBool();
                            BoolLiteral boolLit = new BoolLiteral(-1, -1, not.toString());
                            i.putOperandOne(boolLit);
                        }
                        i.putOperator(SSAOperator.NONE);
                        i.putOperandTwo(null);
                        break;
                        // TODO: move to const folding
                        // Operand opnd = i.getOperandOne();
                        // if (isConst(opnd)) {
                        //     ((BoolLiteral) opnd).setBoolValue(!(((BoolLiteral) opnd).valueAsBool()));
                        // }
                        // break;
                    case AND:
                        if (isBoolLit(opnd1)){
                            Boolean and = ((BoolLiteral) opnd1).valueAsBool() && ((BoolLiteral) opnd2).valueAsBool();
                            BoolLiteral boolLit = new BoolLiteral(-1, -1, and.toString());
                            i.putOperandOne(boolLit);
                        }
                        break;
                    case OR:
                        if (isBoolLit(opnd1)){
                            Boolean or = ((BoolLiteral) opnd1).valueAsBool() && ((BoolLiteral) opnd2).valueAsBool();
                            BoolLiteral boolLit = new BoolLiteral(-1, -1, or.toString());
                            i.putOperandOne(boolLit);
                        }
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
                            // Branch untakens
                        }
                        break;
                    default:
                        break;
                    
                }
            }
        }
    }

    // Emory 
    public boolean copyPropagation(){
        // available expression analysis
        boolean change = true;
        int loops = 0;
        while(change){
            loops++;
            generateAvailableExpresssion(ssa);
            change = false;
            for (BasicBlock bb : ssa.getBasicBlockList()){
                for (IntermediateInstruction ii : bb.getIntInsList()){
                    for (IntermediateInstruction iiAvail : ii.availableExpressions){
                        if (iiAvail.getOperator() == SSAOperator.MOVE){
                            if (iiAvail.getOperandOne() instanceof Symbol){
                                // only replace the RHS of a MOVE!
                                if (ii.getOperator() == SSAOperator.MOVE){
                                    if (ii.getOperandOne() instanceof Symbol){
                                        String operandOneName = ((Symbol) ii.getOperandOne()).name();
                                        operandOneName = operandOneName.substring(0, operandOneName.indexOf("_"));
    
                                        String operandName = ((Symbol) iiAvail.getOperandTwo()).name();
                                        operandName = operandName.substring(0, operandName.indexOf("_"));
                                        if (operandName.equals(operandOneName)){
                                            ii.setOperandOne(iiAvail.getOperandOne());
                                            change = true;
                                        }
                                    }
                                }
                                else{ 
                                    if (ii.getOperator() != SSAOperator.PHI){
                                        if(ii.setMatchingOperand(iiAvail.getOperandTwo(), iiAvail.getOperandOne())){
                                            change = true;
                                        }
                                    }
                                }
                            }
                        }
                        else if(iiAvail.getOperator() == SSAOperator.NONE){
                            if (iiAvail.getOperandOne() instanceof Symbol){
                                if (ii.getOperator() == SSAOperator.MOVE){
                                    if (ii.getOperandOne() instanceof InstructionNumber){
                                        if (((InstructionNumber) ii.getOperandOne()).getInstructionNumber() == iiAvail.insNum()){
                                            ii.setOperandOne(iiAvail.getOperandOne());
                                            change = true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (loops > 1){ // If there was more than one loop, then there was a code change
            return true;
        }
        return false; // If there was just one loop, then there was no code change
    }
       
    // Emory 
    public boolean commonSubexpressionElimination(){
        // available expression analysis
        boolean change = true;
        int loops = 0;
        while(change){
            loops++;
            HashMap<Integer, Integer> toReplace = new HashMap<Integer, Integer>();
            generateAvailableExpresssion(ssa);
            change = false;
            for (BasicBlock bb : ssa.getBasicBlockList()){
                for (IntermediateInstruction ii : bb.getIntInsList()){
                    // check the toReplace Hash Set for each instruction: 
                    for (int op : toReplace.keySet()){
                        if (ii.getOperandOne() instanceof InstructionNumber){
                            if (((InstructionNumber) ii.getOperandOne()).getInstructionNumber() == op){
                                change = true;
                                ii.putOperandOne(new InstructionNumber(toReplace.get(op)));
                         }
                        }
                        if (ii.getOperandTwo() instanceof InstructionNumber){
                            if (((InstructionNumber) ii.getOperandTwo()).getInstructionNumber() == op){
                                change = true;
                                ii.putOperandTwo(new InstructionNumber(toReplace.get(op)));
                            }
                        }
                    }
                    for (IntermediateInstruction iiAvail : ii.availableExpressions){
                        if (!iiAvail.conflicts(ii)){
                            if (iiAvail.getOperator() != SSAOperator.MOVE){
                                if (ii.insNum() > iiAvail.insNum()){
                                    if (toReplace.keySet().contains(ii.insNum())){
                                        if (iiAvail.insNum() < toReplace.get(ii.insNum())){
                                            toReplace.put(ii.insNum(), iiAvail.insNum());
                                        }
                                    }
                                    else{ 
                                        toReplace.put(ii.insNum(), iiAvail.insNum());
                                    }
                                }
                            }
                        }
                        if (iiAvail.getOperator() == SSAOperator.MOVE && ii.getOperator() == SSAOperator.MOVE){
                            if (iiAvail.getOperandOne() instanceof InstructionNumber && ii.getOperandOne() instanceof InstructionNumber){
                                if(((InstructionNumber) iiAvail.getOperandOne()).getInstructionNumber() == ((InstructionNumber) ii.getOperandOne()).getInstructionNumber()){
                                    if (!ii.getOperandTwo().toString().equals(iiAvail.getOperandTwo().toString())){
                                        change = true;
                                        ii.putOperandOne(iiAvail.getOperandTwo());
                                    }   
                                }
                            }
                        }
                    }
                }
            }
        }
        if (loops > 1){ // If there was more than one loop, then there was a code change
            return true;
        }
        return false; // If there was just one loop, then there was no code change
    }

    // Max
    public void deadCodeElimination(){
        // liveness analysis
    }

    // Emory
    public boolean orphanFunctionElimination(){
        // need to add functionality to skip over eliminated lines
        List<String> calledFunctions = new ArrayList<String>();
        calledFunctions.add("main");
        List<BasicBlock> toRemove = new ArrayList<BasicBlock>();
        List<BasicBlock> toCheck = new ArrayList<BasicBlock>();

        for(BasicBlock bb : ssa.getBasicBlockList()){
            for (IntermediateInstruction ii : bb.getIntInsList()){
                if (ii.getOperator() == SSAOperator.CALL){
                    calledFunctions.add(ii.getFuncName());
                }
            }
        }
        for (BasicBlock bb : ssa.getBasicBlockList()){
            if (!calledFunctions.contains(bb.name())){
                // now we need to remove the orphan function 
                toRemove.add(bb);

                while(toCheck.size() != 0){
                    BasicBlock checkBB = toCheck.get(0);
                    for (Transitions t : checkBB.transitionList){
                        if (!t.label.contains("call") && !toRemove.contains(t.toBB)){
                            toRemove.add(t.toBB);
                            toCheck.add(t.toBB);
                        }
                    }
                } 
            }
        }
        while(toRemove.size() != 0){
            ssa.removeBB(toRemove.get(0));
            toRemove.remove(0);
        }

        return false;
    }

    public void generateAvailableExpressionForBB(Set<IntermediateInstruction> entrySet, BasicBlock bb){
        List<SSAOperator> opsToAdd = new ArrayList<SSAOperator>();
        opsToAdd.add(SSAOperator.NEG);
        opsToAdd.add(SSAOperator.ADD);
        opsToAdd.add(SSAOperator.SUB);
        opsToAdd.add(SSAOperator.MUL);
        opsToAdd.add(SSAOperator.DIV);
        opsToAdd.add(SSAOperator.MOD);
        opsToAdd.add(SSAOperator.POW);
        opsToAdd.add(SSAOperator.CMP);
        opsToAdd.add(SSAOperator.NOT);
        opsToAdd.add(SSAOperator.OR);
        opsToAdd.add(SSAOperator.AND);
        opsToAdd.add(SSAOperator.ADDA);
        opsToAdd.add(SSAOperator.LOAD);
        opsToAdd.add(SSAOperator.STORE);
        opsToAdd.add(SSAOperator.READ);
        opsToAdd.add(SSAOperator.NONE);

        Set<IntermediateInstruction> availableExpressions = new HashSet<IntermediateInstruction>();
        availableExpressions.addAll(entrySet);
        
        List<IntermediateInstruction> toRemove = new ArrayList<IntermediateInstruction>();

        for (IntermediateInstruction ii : bb.getIntInsList()){
            ii.availableExpressions.clear();
            ii.availableExpressions.addAll(availableExpressions);
            // If there is a move instruction, then we need to remove elements from the set
            if (ii.getOperator() == SSAOperator.MOVE){
                toRemove.clear();
                for (IntermediateInstruction iiAvail : availableExpressions){
                    if (iiAvail.containsOperand(ii.getOperandTwo())){
                        toRemove.add(iiAvail);
                    }
                }
                availableExpressions.removeAll(toRemove);

                // Don't add move instructions with instruction numbers (for now - until CSE is implemented)

                availableExpressions.add(new IntermediateInstruction(ii.getOperator(), ii.getOperandOne(), ii.getOperandTwo(), ii.insNum()));
            }// Add other instructions to the set
            else{ 
                if (opsToAdd.contains(ii.getOperator())){
                    availableExpressions.add(new IntermediateInstruction(ii.getOperator(), ii.getOperandOne(), ii.getOperandTwo(), ii.insNum()));
                }
            }
        }

       bb.exitAvailableExpression = availableExpressions;
    }

    public void generateAvailableExpresssion(SSA ssa){
        // First, compute each BB's Local Available Expression Sets for each instruction
        // Iterate through each Basic Block
    
        ssa.clearAvailExpressions();
        List<BasicBlock> bbList = new ArrayList<BasicBlock>();
        List<BasicBlock> visited = new ArrayList<BasicBlock>();
        visited.addAll(ssa.getBasicBlockList());

        bbList.add(ssa.rootBB);
        BasicBlock bb; 
        boolean change = true; 

        while ((bbList.size() != 0 && change) || visited.size() != 0) { 
            change = false; 
            if (bbList.size() != 0){
                bb = bbList.get(0);
            }
            else{ 
                bb = visited.get(0);
            }
            
            visited.remove(bb);
            //System.out.println("bb " + bb);
            //System.out.println("bbList " + bbList);
            //System.out.println("visited " + visited);

            if (bb.getIntInsList().size() != 0){
                if (bb.getIntInsList().get(bb.getIntInsList().size() - 1).availableExpressions.size() == 0){
                    change = true;
                    generateAvailableExpressionForBB(bb.getIntInsList().get(bb.getIntInsList().size() - 1).availableExpressions, bb);
                }
            }

            for (Transitions t : bb.transitionList){
                if (t.label.contains("call")){
                    //System.out.println("call " + t.label);
                    generateAvailableExpressionForBB(new HashSet<IntermediateInstruction>(), t.toBB);
                    bbList.add(t.toBB);
                    visited.remove(t.toBB);
                    continue;
                }
                //bbList.add(t.toBB);

                if (t.toBB.getIntInsList().get(0).availableExpressions.size() == 0){
                    // If not, then just instantiate it with the avail expression set of the parent's last instruction 
                    change = true;
                    generateAvailableExpressionForBB(bb.exitAvailableExpression, t.toBB);
                    bbList.add(t.toBB);
                }
                else{ //Need to do an intersection - complexity increased by subscripts of variables
                    Set<IntermediateInstruction> intersection = new HashSet<IntermediateInstruction>();
                    for (IntermediateInstruction iiParent : bb.exitAvailableExpression){
                        for (IntermediateInstruction iiChild : t.toBB.getIntInsList().get(0).availableExpressions){
                            if (!iiChild.conflicts(iiParent)){
                                intersection.add(iiChild);
                            }
                        }
                    }

                    if (!t.toBB.getIntInsList().get(0).availableExpressions.equals(intersection)){
                        bbList.remove(t.toBB);
                        //bbList.add(1, t.toBB);
                        bbList.add(0, t.toBB);
                        change = true;
                    }
                    t.toBB.getIntInsList().get(0).availableExpressions.clear();
                    t.toBB.getIntInsList().get(0).availableExpressions.addAll(intersection);
                    generateAvailableExpressionForBB(t.toBB.getIntInsList().get(0).availableExpressions, t.toBB);
                }
            }
            if (bbList.size() != 0){
                bbList.remove(bb);
            }
        }
    }

}

