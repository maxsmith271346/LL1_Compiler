package types;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ast.*;
import pl434.Symbol;
import pl434.SymbolTable;

public class TypeChecker implements NodeVisitor {

    
    private StringBuilder errorBuffer;
    private Symbol currentFunction;
    private int currentArrayIndex;

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
    public Type visit (Computation node) {
        node.variables().accept(this);
        node.functions().accept(this);
        node.mainStatementSequence().accept(this);

        return null;
    }

    @Override
    public Type visit(BoolLiteral node) {
        return new BoolType();  
    }

    @Override
    public Type visit(IntegerLiteral node) {
        return new IntType(); 
    }

    @Override
    public Type visit(FloatLiteral node) {
        return new FloatType();  
    }
    @Override
    public Type visit(ArrayIndex node) {
        node.leftExpr().accept(this);
        node.rightExpr().accept(this);
        return node.getType();
    }
    @Override
    public Type visit(LogicalNot node) {
        Type returnType = node.expr().accept(this).not();
        if (returnType instanceof ErrorType){
            reportError(node.lineNumber(), node.charPosition(), ((ErrorType) returnType).getMessage());
        }
        return returnType;
    }
    @Override
    public Type visit(Power node) {
        //check for negative in the base and/or exponent
        if (node.rightExpression() instanceof IntegerLiteral){
            if (Integer.parseInt(((IntegerLiteral) node.rightExpression()).value()) < 0){
                ErrorType error = new ErrorType("Power cannot have a negative exponent of " + ((IntegerLiteral) node.rightExpression()).value() + ".");
                reportError(node.lineNumber(), node.charPosition(), error.getMessage());
                return error;
            }
        }
        else if (node.rightExpression() instanceof FloatLiteral){
            if (Integer.parseInt(((FloatLiteral) node.rightExpression()).value()) < 0){
                ErrorType error = new ErrorType("Power cannot have a negative exponent of " + ((FloatLiteral) node.rightExpression()).value()+ ".");
                reportError(node.lineNumber(), node.charPosition(), error.getMessage());
                return error;
            }
        }

        if (node.leftExpression() instanceof IntegerLiteral){
            if (Integer.parseInt(((IntegerLiteral) node.leftExpression()).value()) < 0){
                ErrorType error = new ErrorType("Power cannot have a negative base of " + ((IntegerLiteral) node.leftExpression()).value()+ ".");
                reportError(node.lineNumber(), node.charPosition(), error.getMessage());
                return error;
            }
        }
        else if (node.leftExpression() instanceof FloatLiteral){
            if (Integer.parseInt(((FloatLiteral) node.leftExpression()).value()) < 0){
                ErrorType error = new ErrorType("Power cannot have a negative base of " + ((FloatLiteral) node.leftExpression()).value()+ ".");
                reportError(node.lineNumber(), node.charPosition(), error.getMessage());
                return error; 
            }
        }
        Type returnType = node.leftExpression().accept(this).pow(node.rightExpression().accept(this));
        if (returnType instanceof ErrorType){
            reportError(node.lineNumber(), node.charPosition(), ((ErrorType) returnType).getMessage());
        }
        return returnType;
    }
    @Override
    public Type visit(Multiplication node) {
        Type returnType = node.leftExpression().accept(this).mul(node.rightExpression().accept(this));
        if (returnType instanceof ErrorType){
            reportError(node.lineNumber(), node.charPosition(), ((ErrorType) returnType).getMessage());
        }
        return returnType;
    }
    @Override
    public Type visit(Division node) {
        //TODO: check for div by 0
        if (node.rightExpression() instanceof IntegerLiteral){
            if (Integer.parseInt(((IntegerLiteral) node.rightExpression()).value()) == 0){
                ErrorType error = new ErrorType("Cannot divide by 0.");
                reportError(node.lineNumber(), node.charPosition(), error.getMessage());
                return error;
            }
        }

        Type returnType = node.leftExpression().accept(this).div(node.rightExpression().accept(this));
        if (returnType instanceof ErrorType){
            reportError(node.lineNumber(), node.charPosition(), ((ErrorType) returnType).getMessage());
        }
        return returnType;
    }
    @Override
    public Type visit(Modulo node) {
        Type returnType = node.leftExpression().accept(this).mod(node.rightExpression().accept(this));
        if (returnType instanceof ErrorType){
            reportError(node.lineNumber(), node.charPosition(), ((ErrorType) returnType).getMessage());
        }
        return returnType;
    }
    @Override
    public Type visit(LogicalAnd node) {
        Type returnType = node.leftExpression().accept(this).and(node.rightExpression().accept(this));
        if (returnType instanceof ErrorType){
            reportError(node.lineNumber(), node.charPosition(), ((ErrorType) returnType).getMessage());
        }
        return returnType;
    }
    @Override
    public Type visit(Addition node) {
        Type returnType = node.leftExpression().accept(this).add(node.rightExpression().accept(this));
        if (returnType instanceof ErrorType){
            reportError(node.lineNumber(), node.charPosition(), ((ErrorType) returnType).getMessage());
        }
        return returnType;
    }
    @Override
    public Type visit(Subtraction node) {
        Type returnType = node.leftExpression().accept(this).sub(node.rightExpression().accept(this));
        if (returnType instanceof ErrorType){
            reportError(node.lineNumber(), node.charPosition(), ((ErrorType) returnType).getMessage());
        }
        return returnType;
    }
    @Override
    public Type visit(LogicalOr node) {
        Type returnType = node.leftExpression().accept(this).or(node.rightExpression().accept(this));
        if (returnType instanceof ErrorType){
            reportError(node.lineNumber(), node.charPosition(), ((ErrorType) returnType).getMessage());
        }
        return returnType;
    }
    @Override
    public Type visit(Relation node) {
        Type returnType = node.leftExpression().accept(this).compare(node.rightExpression().accept(this));
        if (returnType instanceof ErrorType){
            reportError(node.lineNumber(), node.charPosition(), ((ErrorType) returnType).getMessage());
        }
        return returnType;
    }
    @Override
    public Type visit(Assignment node) {
        Type returnType = node.lhsDesignator().accept(this).assign(node.rhsExpr().accept(this));
        if (returnType instanceof ErrorType){
            reportError(node.lineNumber(), node.charPosition(), ((ErrorType) returnType).getMessage());
        }
        return returnType;
    }
    @Override
    public Type visit(ArgumentList node) { //TODO: this only works for one arg currently
        TypeList typeList = new TypeList();
        if (!node.empty()){
            for (Expression e : node.argList) { 
                Type eType = e.accept(this);
                if (eType instanceof AddressOf){
                    eType = ((AddressOf) eType).getType();
                }
                typeList.append(eType);
            }
        }
        return typeList;
    }
    @Override
    public Type visit(FunctionCall node) {
        Type returnType = Type.call(node.argList.accept(this), node.function());
        if (returnType instanceof ErrorType){
            reportError(node.lineNumber(), node.charPosition(), ((ErrorType) returnType).getMessage());
        }
        return returnType;
    }
    @Override
    public Type visit(IfStatement node) {
        Type returnType = Type.ifStat(node.condition().accept(this));
        if (returnType instanceof ErrorType){ 
            reportError(node.lineNumber(), node.charPosition(), ((ErrorType) returnType).getMessage());
        }

        node.thenStatementSeq().accept(this);
        if (node.elseStatementSeq() != null){
            node.elseStatementSeq().accept(this);
        }

        return null;
    }
    @Override
    public Type visit(WhileStatement node) {
        Type returnType = Type.whileStat(node.condition().accept(this));
        if (returnType instanceof ErrorType){ 
            reportError(node.lineNumber(), node.charPosition(), ((ErrorType) returnType).getMessage());
        }
        node.statementSeq().accept(this);
        return returnType;
    }
    @Override
    public Type visit(RepeatStatement node) {
        node.statementSeq().accept(this);
        Type returnType = Type.repeatStat(node.condition().accept(this));
        if (returnType instanceof ErrorType){ 
            reportError(node.lineNumber(), node.charPosition(), ((ErrorType) returnType).getMessage());
        }
        return null;
    }
    @Override
    public Type visit(ReturnStatement node) {
        if (node.returnValue() != null){
            Type returnType = Type.returnStat(node.returnValue().accept(this), currentFunction);
            if(returnType instanceof ErrorType){
                reportError(node.lineNumber(), node.charPosition(), ((ErrorType) returnType).getMessage());
            }
        }
        return null;
    }
    @Override
    public Type visit(StatementSequence node) {
        for (Statement s : node.statSeq) { 
            s.accept(this);
        }
        return null;
    }
    @Override
    public Type visit(VariableDeclaration node) {
        node.symbol().accept(this);
        if (node.symbol().dimList.size() != 0){
            Type returnType = Type.dimList(node.symbol().dimList, node.symbol().name());
            if(returnType instanceof ErrorType){
                reportError(node.lineNumber(), node.charPosition(), ((ErrorType) returnType).getMessage());
            }
        }
        return null;
    }
    @Override
    public Type visit(FunctionBody node) {
        node.variables().accept(this);
        node.statements().accept(this);
        return null;
    }
    @Override
    public Type visit(FunctionDeclaration node) {
        currentFunction = node.function();
        node.body().accept(this);
        return null;
    }
    @Override
    public Type visit(DeclarationList node) {
        if (node.empty()) return null;
        for (Declaration d : node.decList) { // TODO: make statement sequence iterable
            d.accept(this);
        }
        return null;
    }
    @Override
    public Type visit(Symbol node) {
        AddressOf addressOf = new AddressOf(node.getType());
        return addressOf;
    }
}
