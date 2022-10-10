package ast;

import pl434.Symbol;
import types.Type;

public interface NodeVisitor {

    // literal
    public Type visit (BoolLiteral node);
    public Type visit (IntegerLiteral node);
    public Type visit (FloatLiteral node);

    // designator
    //public void visit (AddressOf node);
    public Type visit (ArrayIndex node);
    //public void visit (Dereference node); // useful for the DLX code & going into the right hand side and loading in the register
    
    // groupExpr
    public Type visit (LogicalNot node);
    // powExpr
    public Type visit (Power node);
    // multExpr
    public Type visit (Multiplication node);
    public Type visit (Division node);
    public Type visit (Modulo node);
    public Type visit (LogicalAnd node);
    // addExpr
    public Type visit (Addition node);
    public Type visit (Subtraction node);
    public Type visit (LogicalOr node);
    // relExpr
    public Type visit (Relation node);
    
    // assign
    public Type visit (Assignment node);
    
    // funcCall
    public Type visit (ArgumentList node);
    public Type visit (FunctionCall node);
    // ifStat
    public Type visit (IfStatement node);
    // whileStat
    public Type visit (WhileStatement node);
    // repeatStat
    public Type visit (RepeatStatement node);
    // returnStat
    public Type visit (ReturnStatement node); 
    // statSeq
    public Type visit (StatementSequence node);
    // varDecl
    public Type visit (VariableDeclaration node);
    // funcBody
    public Type visit (FunctionBody node);
    // funcDecl
    public Type visit (FunctionDeclaration node);

    // computation
    public Type visit (DeclarationList node);
    public Type visit (Computation node);

    public Type visit (Symbol node); //added this
    
}

