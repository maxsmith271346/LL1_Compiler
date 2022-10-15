package types; 

import java.util.ArrayList;
import java.util.List;

import ast.*;
import pl434.Symbol;

public class TypeChecker implements NodeVisitor{
    
    private StringBuilder errorBuffer;
    private Symbol currentFunction;

    /* 
     * Useful error strings:
     *
     * "Call with args " + argTypes + " matches no function signature."
     * "Call with args " + argTypes + " matches multiple function signatures."
     * 
     * "IfStat requires relation condition not " + cond.getClass() + "."
     * "WhileStat requires relation condition not " + cond.getClass() + "."
     * "RepeatStat requires relation condition not " + cond.getClass() + "."
     * 
     * "Function " + currentFunction.name() + " returns " + statRetType + " instead of " + funcRetType + "."
     * 
     * "Variable " + var.name() + " has invalid type " + var.type() + "."
     * "Array " + var.name() + " has invalid base type " + baseType + "."
     * 
     * 
     * "Function " + currentFunction.name() + " has a void arg at pos " + i + "."
     * "Function " + currentFunction.name() + " has an error in arg at pos " + i + ": " + ((ErrorType) t).message())
     * "Not all paths in function " + currentFunction.name() + " return."
     */


    public Boolean check(AST ast){
        errorBuffer = new StringBuilder();
        visit(ast.computation);
        return !hasError();
    }
    private void reportError (int lineNum, int charPos, String message) {
        errorBuffer.append("TypeError(" + lineNum + "," + charPos + ")");
        errorBuffer.append("[" + message + "]" + "\n");
    }

    public boolean hasError () {
        return errorBuffer.length() != 0;
    }

    public String errorReport () {
        return errorBuffer.toString();
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
        if (node.arrayIdent().type() instanceof ArrayType){
            ((ArrayType) node.arrayIdent().type()).addIndices(new ArrayList<Expression>());
        }
        int currentPos = 0;
        for (Expression e : node.indices()){
            e.accept(this);
            Type returnType = node.arrayIdent().type().index(e.type());
            if (returnType instanceof ErrorType){
                reportError(node.lineNumber(), node.charPosition(), ((ErrorType) returnType).getMessage());
                node.addType(returnType);
                return;
            }

            if (currentPos == node.dimList().size()){
                Type t = node.arrayIdent().type();
                if (t instanceof ArrayType){
                    t = ((ArrayType) node.arrayIdent().type()).type();
                }
                ErrorType error = new ErrorType("Cannot index AddressOf(" + t + ") with " + e.type() + ".");
                reportError(node.lineNumber(), node.charPosition(), error.getMessage());
                node.addType(error);
                return;
            }
            if (e instanceof IntegerLiteral){
                if (Integer.parseInt(((IntegerLiteral) e).value()) >= Integer.parseInt(node.dimList().get(currentPos)) || Integer.parseInt(((IntegerLiteral) e).value()) < 0){
                    ErrorType error = new ErrorType("Array Index Out of Bounds : " + ((IntegerLiteral) e).value() + " for array " + node.name());
                    reportError(node.lineNumber(), node.charPosition(), error.getMessage());

                    node.addType(error);
                    return;
                }
            }
            currentPos++;
        }
        if (node.arrayIdent().type() instanceof ArrayType){
            ((ArrayType) node.arrayIdent().type()).addIndices(node.indices());
        }
        node.addType(node.arrayIdent().type());
        
    }
    @Override
    public void visit(LogicalNot node) {
        node.expr().accept(this);

        Type returnType = node.expr().type().not();
        if (returnType instanceof ErrorType){
            reportError(node.lineNumber(), node.charPosition(), ((ErrorType) returnType).getMessage());
        }

        node.addType(returnType);
    }
    @Override
    public void visit(Power node) {
        //check for negative in the base and/or exponent
        if (node.rightExpression() instanceof IntegerLiteral){
            if (Integer.parseInt(((IntegerLiteral) node.rightExpression()).value()) < 0){
                ErrorType error = new ErrorType("Power cannot have a negative exponent of " + ((IntegerLiteral) node.rightExpression()).value() + ".");
                reportError(node.lineNumber(), node.charPosition(), error.getMessage());
                node.addType(error);
                return;
            }
        }
        else if (node.rightExpression() instanceof FloatLiteral){
            if (Integer.parseInt(((FloatLiteral) node.rightExpression()).value()) < 0){
                ErrorType error = new ErrorType("Power cannot have a negative exponent of " + ((FloatLiteral) node.rightExpression()).value()+ ".");
                reportError(node.lineNumber(), node.charPosition(), error.getMessage());
                node.addType(error);
                return;
            }
        }

        if (node.leftExpression() instanceof IntegerLiteral){
            if (Integer.parseInt(((IntegerLiteral) node.leftExpression()).value()) < 0){
                ErrorType error = new ErrorType("Power cannot have a negative base of " + ((IntegerLiteral) node.leftExpression()).value()+ ".");
                reportError(node.lineNumber(), node.charPosition(), error.getMessage());
                node.addType(error);
                return;
            }
        }
        else if (node.leftExpression() instanceof FloatLiteral){
            if (Integer.parseInt(((FloatLiteral) node.leftExpression()).value()) < 0){
                ErrorType error = new ErrorType("Power cannot have a negative base of " + ((FloatLiteral) node.leftExpression()).value()+ ".");
                reportError(node.lineNumber(), node.charPosition(), error.getMessage());
                node.addType(error);
                return;
            }
        }
        node.leftExpression().accept(this);
        node.rightExpression().accept(this);

        Type returnType = node.leftExpression().type().pow(node.rightExpression().type());

        if (returnType instanceof ErrorType){
            reportError(node.lineNumber(), node.charPosition(), ((ErrorType) returnType).getMessage());
        }

        node.addType(returnType);
    }
    @Override
    public void visit(Multiplication node) {
        node.leftExpression().accept(this);
        node.rightExpression().accept(this);

        Type returnType = node.leftExpression().type().mul(node.rightExpression().type());

        if (returnType instanceof ErrorType){
            reportError(node.lineNumber(), node.charPosition(), ((ErrorType) returnType).getMessage());
        }

        node.addType(returnType);
    }
    @Override
    public void visit(Division node) {

        if (node.rightExpression() instanceof IntegerLiteral){
            if (Integer.parseInt(((IntegerLiteral) node.rightExpression()).value()) == 0){
                ErrorType error = new ErrorType("Cannot divide by 0.");
                reportError(node.lineNumber(), node.charPosition(), error.getMessage());
                node.addType(error);
                return;
            }
        }
        
        node.leftExpression().accept(this);
        node.rightExpression().accept(this);

        Type returnType = node.leftExpression().type().div(node.rightExpression().type());

        if (returnType instanceof ErrorType){
            reportError(node.lineNumber(), node.charPosition(), ((ErrorType) returnType).getMessage());
        }

        node.addType(returnType);
    }
    @Override
    public void visit(Modulo node) {
        node.leftExpression().accept(this);
        node.rightExpression().accept(this);

        Type returnType = node.leftExpression().type().mod(node.rightExpression().type());

        if (returnType instanceof ErrorType){
            reportError(node.lineNumber(), node.charPosition(), ((ErrorType) returnType).getMessage());
        }

        node.addType(returnType);
    }
    @Override
    public void visit(LogicalAnd node) {
        node.leftExpression().accept(this);
        node.rightExpression().accept(this);

        Type returnType = node.leftExpression().type().and(node.rightExpression().type());

        if (returnType instanceof ErrorType){
            reportError(node.lineNumber(), node.charPosition(), ((ErrorType) returnType).getMessage());
        }

        node.addType(returnType);
    }
    @Override
    public void visit(Addition node) {
        node.leftExpression().accept(this);
        node.rightExpression().accept(this);

        Type returnType = node.leftExpression().type().add(node.rightExpression().type());

        if (returnType instanceof ErrorType){
            reportError(node.lineNumber(), node.charPosition(), ((ErrorType) returnType).getMessage());
        }

        node.addType(returnType);
    }
    @Override
    public void visit(Subtraction node) {
        node.leftExpression().accept(this);
        node.rightExpression().accept(this);

        Type returnType = node.leftExpression().type().sub(node.rightExpression().type());

        if (returnType instanceof ErrorType){
            reportError(node.lineNumber(), node.charPosition(), ((ErrorType) returnType).getMessage());
        }

        node.addType(returnType);
        
    }
    @Override
    public void visit(LogicalOr node) {
        node.leftExpression().accept(this);
        node.rightExpression().accept(this);

        Type returnType = node.leftExpression().type().or(node.rightExpression().type());

        if (returnType instanceof ErrorType){
            reportError(node.lineNumber(), node.charPosition(), ((ErrorType) returnType).getMessage());
        }

        node.addType(returnType);
        
    }
    @Override
    public void visit(Relation node) {
        node.leftExpression().accept(this);
        node.rightExpression().accept(this);

        Type returnType = node.leftExpression().type().compare(node.rightExpression().type());

        if (returnType instanceof ErrorType){
            reportError(node.lineNumber(), node.charPosition(), ((ErrorType) returnType).getMessage());
        }

        node.addType(returnType);
    }
    @Override
    public void visit(Assignment node) {
        node.lhsDesignator().accept(this);
        node.rhsExpr().accept(this);

        Type returnType = node.lhsDesignator().type().assign(node.rhsExpr().type());

        if (returnType instanceof ErrorType){
            reportError(node.lineNumber(), node.charPosition(), ((ErrorType) returnType).getMessage());
        }
    }
    @Override
    public void visit(ArgumentList node) {
        TypeList typeList = new TypeList();
        if (!node.empty()){
            for (Expression e : node.argList) { 
                e.accept(this);
                typeList.append(e.type());
            }
        }
        node.addType(typeList);
    }
    @Override
    public void visit(FunctionCall node) {
        node.argList.accept(this);

        Type returnType = node.argList.type().call(node.function());

        if (returnType instanceof ErrorType){
            reportError(node.lineNumber(), node.charPosition(), ((ErrorType) returnType).getMessage());
        }

        node.addType(returnType);   
    }
    @Override
    public void visit(IfStatement node) {
        node.condition().accept(this);

        Type returnType = node.condition().type().ifStat();
        if (returnType instanceof ErrorType){
            reportError(node.lineNumber(), node.charPosition(), ((ErrorType) returnType).getMessage());
        }

        node.thenStatementSeq().accept(this);
        if (node.elseStatementSeq() != null){
            node.elseStatementSeq().accept(this);
        }
    }
    @Override
    public void visit(WhileStatement node){
        node.condition().accept(this);

        Type returnType = node.condition().type().whileStat();
        if (returnType instanceof ErrorType){
            reportError(node.lineNumber(), node.charPosition(), ((ErrorType) returnType).getMessage());
        }

        node.statementSeq().accept(this);
        
    }
    @Override
    public void visit(RepeatStatement node) {
        node.statementSeq().accept(this);
        node.condition().accept(this);

        Type returnType = node.condition().type().repeatStat();
        if (returnType instanceof ErrorType){
            reportError(node.lineNumber(), node.charPosition(), ((ErrorType) returnType).getMessage());
        }
        
    }
    @Override
    public void visit(ReturnStatement node) {
        if (node.returnValue() != null){
            node.returnValue().accept(this);
            Type returnType = node.returnValue().type().returnStat(currentFunction);
            if(returnType instanceof ErrorType){
                reportError(node.lineNumber(), node.charPosition(), ((ErrorType) returnType).getMessage());
            }
        }
        
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
        if (node.symbol().dimList.size() != 0){
            Type returnType = Type.dimList(node.symbol().dimList, node.symbol().name());
            if(returnType instanceof ErrorType){
                reportError(node.lineNumber(), node.charPosition(), ((ErrorType) returnType).getMessage());
            }
            node.symbol().addType(new ArrayType(node.symbol().type(), node.symbol().dimList));
        }
        
    }
    @Override
    public void visit(FunctionBody node) {
        node.variables().accept(this);
        node.statements().accept(this);
        
    }
    @Override
    public void visit(FunctionDeclaration node) {
        currentFunction = node.function();
        node.body().accept(this);
        
    }
    @Override
    public void visit(DeclarationList node) {
        if (node.empty()) return;
        for (Declaration d : node.decList) { // TODO: make statement sequence iterable
            d.accept(this);
        }        
    }
    @Override
    public void visit(Computation node) {
        node.variables().accept(this);
        node.functions().accept(this);
        node.mainStatementSequence().accept(this);
        
    }
    @Override
    public void visit(Symbol node) {
        // TODO Auto-generated method stub
        if (node.type() instanceof ArrayType){
            ((ArrayType) node.type()).addIndices(new ArrayList<Expression>());
        }
        
    }
}
