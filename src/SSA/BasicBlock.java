package SSA;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import SSA.IntermediateInstruction.SSAOperator;
import ast.IntegerLiteral;
import pl434.Symbol;

public class BasicBlock implements Operand {
    private List<IntermediateInstruction> IntermediateInstructionList; 
    private String BBName;
    public List<Transitions> transitionList;    // Contains all the 'children' of the Basic Block
    public int BBNumber = 0;                    // Keeps a running count of the number of Basic Blocks (used for identifying the BBs)
    public HashMap<Symbol, HashSet<Symbol>> varMap;      // Contains a map for each symbol where the value is a new symbol with subscript
    public static int insNumber = 0;            // Contains a running count of the instruction number
    public Set<IntermediateInstruction> exitAvailableExpression;
    

    // This class is used to represent the children of a BB and their "relationship" to the parent BB (call, else, if, etc)
    public class Transitions{ 
        //public BasicBlock fromBB, toBB;
        public BasicBlock toBB;
        public String label; 
        public Transitions(BasicBlock to, String label){
            this.toBB = to; 
            this.label = label; 
        }
    }

    public BasicBlock(int BBNumber, HashMap<Symbol, HashSet<Symbol>> varMap){
        IntermediateInstructionList = new ArrayList<IntermediateInstruction>();
        transitionList = new ArrayList<Transitions>();
        this.BBNumber = BBNumber;
        this.varMap = new HashMap<Symbol, HashSet<Symbol>>();
        for (Symbol key : varMap.keySet()){
            HashSet<Symbol> varSet = new HashSet<Symbol>(varMap.get(key));
            for (Symbol s : varSet){
                if (s.name().contains("-2")){
                    varSet.remove(s);
                }
            }
            this.varMap.put(key, varSet);
        }
        this.BBName = "";
        this.exitAvailableExpression = new HashSet<IntermediateInstruction>();
    }

    public BasicBlock(int BBNumber, HashMap<Symbol, HashSet<Symbol>> varMap, String name){
        this(BBNumber, varMap);
        this.BBName = name;
    }

    public void addBasicBlockName(String name){
        this.BBName = name;
    }

    public void addMap(HashMap<Symbol, HashSet<Symbol>> varMap){
        for (Symbol key : varMap.keySet()){
            HashSet<Symbol> varSet = new HashSet<Symbol>(varMap.get(key));
            for (Symbol s : varSet){
                if (s.name().contains("-2")){
                    varSet.remove(s);
                }
            }
            this.varMap.put(key, varSet);
        }
    }

    public void addFullMap(HashMap<Symbol, HashSet<Symbol>> varMap){
        for (Symbol key : varMap.keySet()){
            HashSet<Symbol> varSet = new HashSet<Symbol>(varMap.get(key));
            this.varMap.put(key, varSet);
        }
    }
    
    /**
     * Adds an IntermediateInstruction to the list and returns the current instruction number
     * 
     * @param intIns new IntermediateInstruction to add
     * @return current instruction number
    */
    public int add(IntermediateInstruction intIns){
        if (intIns.getOperator() != SSAOperator.PHI){
            if (intIns.getOperandOne() != null){
                if (intIns.getOperandOne() instanceof Symbol){
                    Symbol operandOneSymbol = (Symbol) intIns.getOperandOne();
                    String opOneName = (operandOneSymbol).name();
                    if (opOneName.contains("-1") || opOneName.contains("-2")){
                        System.out.println("warning: variable " + opOneName.substring(0, opOneName.indexOf("_")) + " has not been initialized!");
                        Symbol newSymbol = new Symbol(operandOneSymbol.name().substring(0, opOneName.indexOf("_")) + "_" + insNumber, operandOneSymbol.type().toString(), "var", operandOneSymbol.scope);
                        intIns.putOperandOne(newSymbol);
                        this.IntermediateInstructionList.add(new IntermediateInstruction(SSAOperator.MOVE, new IntegerLiteral(0, 0, "0"), newSymbol, insNumber));
                        insNumber++; 
                        // update varMap
                        HashSet<Symbol> newHash = new HashSet<Symbol>();
                        newHash.add(newSymbol);
                        for(Symbol s : varMap.keySet()){
                            //update varMap:
                            if (s.name().equals(opOneName.substring(0, opOneName.indexOf("_")))){
                                varMap.put(s, newHash);
                            }
                        }
                    }  
                }
            }
            if (intIns.getOperator() != SSAOperator.MOVE){
                if (intIns.getOperandTwo() != null){
                    if (intIns.getOperandTwo() instanceof Symbol){
                        Symbol operandTwoSymbol = (Symbol) intIns.getOperandTwo();
                        String opTwoName = (operandTwoSymbol).name();
                        if (opTwoName.contains("-1") || opTwoName.contains("-2")){
                            System.out.println("warning: variable " + opTwoName.substring(0, opTwoName.indexOf("_")) + " has not been initialized!");
                            Symbol newSymbol = new Symbol(operandTwoSymbol.name().substring(0, opTwoName.indexOf("_")) + "_" + insNumber, operandTwoSymbol.type().toString(), "var", operandTwoSymbol.scope);
                            intIns.putOperandTwo(newSymbol);
                            this.IntermediateInstructionList.add(new IntermediateInstruction(SSAOperator.MOVE, new IntegerLiteral(0, 0, "0"), newSymbol, insNumber));
                            insNumber++;
                            // update varMap
                            HashSet<Symbol> newHash = new HashSet<Symbol>();
                            newHash.add(newSymbol);
                            for(Symbol s : varMap.keySet()){
                                //update varMap:
                                if (s.name().equals(opTwoName.substring(0, opTwoName.indexOf("_")))){
                                    varMap.put(s, newHash);
                                }
                            }
                        }  
                    }
                }   
            }    
        }
        
        this.IntermediateInstructionList.add(intIns);
        intIns.setInsNum(insNumber);
        return insNumber++;
    }
    
    public int addFront(IntermediateInstruction intIns){
        this.IntermediateInstructionList.add(0, intIns);
        return insNumber++;
    }

    /*public void addFront(IntermediateInstruction intIns) {
        this.IntermediateInstructionList.add(0, intIns);
    }*/

    public List<IntermediateInstruction> getIntInsList(){
        return IntermediateInstructionList;
    }

    public int size(){
        return IntermediateInstructionList.size();
    }

    public String name(){
        if(BBName != null){
            return BBName;
        }
        return "";
    }

    public void putName(String name){
        this.BBName = name;
    }

    @Override 
    public String toString(){
        return "[" + (BBNumber) + "]";
    }

    public void resolveVars(HashMap<Symbol, HashSet<Symbol>> varMap){
        for (Symbol var : varMap.keySet()){
            for (IntermediateInstruction ii : IntermediateInstructionList){
                if (ii.getOperator() != SSAOperator.MOVE && ii.getOperator() != SSAOperator.PHI){
                    if (ii.getOperandOne() != null){
                        if (ii.getOperandOne().toString().contains(var.name())){
                            ii.putOperandOne(varMap.get(var).iterator().next());
                        }
                    }
                    if (ii.getOperandTwo() != null){
                        if (ii.getOperandTwo().toString().contains(var.name())){
                            ii.putOperandTwo(varMap.get(var).iterator().next());
                        }
                    }
                }
            }
        }
        
        for (Transitions t : transitionList){
           // System.out.println(t.toBB);
            for (Symbol var : varMap.keySet()){
                for (IntermediateInstruction ii : t.toBB.IntermediateInstructionList){
                    if (ii.getOperator() != SSAOperator.MOVE && ii.getOperator() != SSAOperator.PHI){
                        if (ii.getOperandOne() != null){
                            if (ii.getOperandOne().toString().contains(var.name())){
                                ii.putOperandOne(varMap.get(var).iterator().next());
                            }
                        }
                        if (ii.getOperandTwo() != null){
                            if (ii.getOperandTwo().toString().contains(var.name())){
                                ii.putOperandTwo(varMap.get(var).iterator().next());
                            }
                        }
                    }
                }
            }
        }
    }
}
