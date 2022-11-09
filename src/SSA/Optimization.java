package SSA;

public class Optimization { 
    private SSA ssa;

    public Optimization(SSA ssa){
        this.ssa = ssa; 
    }

    // Max
    public void arithmeticSimplification(){
        // replace mul by add 
        // remove arithmetic identity
        // resolve self-subtraction, self-division
        // Expressions that result in 0: mul 0, div 0 by X

        // switch case on different expression types 
        // ex: if multiplication and either of the operands are 0, then replace with 0
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
