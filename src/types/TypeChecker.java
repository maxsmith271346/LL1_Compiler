package types;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ast.*;
import pl434.Symbol;

public class TypeChecker implements NodeVisitor {

    
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
        return new ArrayType();
    }
    @Override
    public Type visit(LogicalNot node) {
        Type returnType = node.expr().accept(this);
        if (returnType instanceof ErrorType){
            reportError(node.lineNumber(), node.charPosition(), ((ErrorType) returnType).getMessage());
        }
        return returnType;
    }
    @Override
    public Type visit(Power node) {
        // TODO Auto-generated method stub
        return null;
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
        Type returnType = node.leftExpression().accept(this).div(node.rightExpression().accept(this));
        if (returnType instanceof ErrorType){
            reportError(node.lineNumber(), node.charPosition(), ((ErrorType) returnType).getMessage());
        }
        return returnType;
    }
    @Override
    public Type visit(Modulo node) {
        // TODO Auto-generated method stub
        return null;
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
        // TODO Auto-generated method stub
        return null;
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
        if (!node.empty()){
            for (Expression e : node.argList) { 
                return e.accept(this);
            }
        }
        return null;
    }
    @Override
    public Type visit(FunctionCall node) {
        Type returnType = Type.call(node.argList.accept(this));
        if (returnType instanceof ErrorType){
            reportError(node.lineNumber(), node.charPosition(), ((ErrorType) returnType).getMessage());
        }
        return returnType;
    }
    @Override
    public Type visit(IfStatement node) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public Type visit(WhileStatement node) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public Type visit(RepeatStatement node) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public Type visit(ReturnStatement node) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public Type visit(StatementSequence node) {
        for (Statement s : node.statSeq) { // TODO: make statement sequence iterable
            s.accept(this);
        }
        return null;
    }
    @Override
    public Type visit(VariableDeclaration node) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public Type visit(FunctionBody node) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public Type visit(FunctionDeclaration node) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public Type visit(DeclarationList node) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public Type visit(Symbol node) {
        return node.getType();
    }
}
