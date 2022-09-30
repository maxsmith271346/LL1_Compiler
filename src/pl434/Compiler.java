package pl434;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import ast.AST;
import ast.Computation;

public class Compiler {

    // Error Reporting ============================================================
    private StringBuilder errorBuffer = new StringBuilder();

    private String reportSyntaxError (NonTerminal nt) {
        String message = "SyntaxError(" + lineNumber() + "," + charPosition() + ")[Expected a token from " + nt.name() + " but got " + currentToken.kind + ".]";
        errorBuffer.append(message + "\n");
        return message;
    }

    private String reportSyntaxError (Token.Kind kind) {
        String message = "SyntaxError(" + lineNumber() + "," + charPosition() + ")[Expected " + kind + " but got " + currentToken.kind + ".]";
        errorBuffer.append(message + "\n");
        return message;
    }

    public String errorReport () {
        return errorBuffer.toString();
    }

    public boolean hasError () {
        return errorBuffer.length() != 0;
    }

    private class QuitParseException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        public QuitParseException (String errorMessage) {
            super(errorMessage);
        }
    }

    private int lineNumber () {
        return currentToken.lineNumber();
    }

    private int charPosition () {
        return currentToken.charPosition();
    }

    // Compiler ===================================================================
    private Scanner scanner;
    private Token currentToken;

    private int numDataRegisters; // available registers are [1..numDataRegisters]
    private List<Integer> instructions;

    // Need to map from IDENT to memory offset

    public Compiler (Scanner scanner, int numRegs) {
        this.scanner = scanner;
        currentToken = this.scanner.next();
        numDataRegisters = numRegs;
        instructions = new ArrayList<>();
    }

    //TODO
    public ast.AST genAST() {
        return new AST();
    }
    
    public int[] compile () {
        initSymbolTable();
        try {
            computation();
            return instructions.stream().mapToInt(Integer::intValue).toArray();
        }
        catch (QuitParseException q) {
            errorBuffer.append("SyntaxError(" + lineNumber() + "," + charPosition() + ")");
            errorBuffer.append("[Could not complete parsing.]");
            return new ArrayList<Integer>().stream().mapToInt(Integer::intValue).toArray();
        }
    }

    // SymbolTable Management =====================================================
    private SymbolTable symbolTable;

    private void initSymbolTable () {
        throw new RuntimeException("implement initSymbolTable");
    }

    private void enterScope () {
        throw new RuntimeException("implement enterScope");
    }

    private void exitScope () {
        throw new RuntimeException("implement exitScope");
    }

    private Symbol tryResolveVariable (Token ident) {
        //TODO: Try resolving variable, handle SymbolNotFoundError
        return null; 
    }

    private Symbol tryDeclareVariable (Token ident) {
        //TODO: Try declaring variable, handle RedeclarationError
        return null;
    }

    private String reportResolveSymbolError (String name, int lineNum, int charPos) {
        String message = "ResolveSymbolError(" + lineNum + "," + charPos + ")[Could not find " + name + ".]";
        errorBuffer.append(message + "\n");
        return message;
    }

    private String reportDeclareSymbolError (String name, int lineNum, int charPos) {
        String message = "DeclareSymbolError(" + lineNum + "," + charPos + ")[" + name + " already exists.]";
        errorBuffer.append(message + "\n");
        return message;
    }


    // Helper Methods =============================================================
    private boolean have (Token.Kind kind) {
        return currentToken.is(kind);
    }

    private boolean have (NonTerminal nt) {
        return nt.firstSet().contains(currentToken.kind);
    }

    private boolean accept (Token.Kind kind) {
        if (have(kind)) {
            try {
                currentToken = scanner.next();
            }
            catch (NoSuchElementException e) {
                if (!kind.equals(Token.Kind.EOF)) {
                    String errorMessage = reportSyntaxError(kind);
                    throw new QuitParseException(errorMessage);
                }
            }
            return true;
        }
        return false;
    }

    private boolean accept (NonTerminal nt) {
        if (have(nt)) {
            currentToken = scanner.next();
            return true;
        }
        return false;
    }

    private boolean expect (Token.Kind kind) {
        if (accept(kind)) {
            return true;
        }
        String errorMessage = reportSyntaxError(kind);
        throw new QuitParseException(errorMessage);
    }

    private boolean expect (NonTerminal nt) {
        if (accept(nt)) {
            return true;
        }
        String errorMessage = reportSyntaxError(nt);
        throw new QuitParseException(errorMessage);
    }

    private Token expectRetrieve (Token.Kind kind) {
        Token tok = currentToken;
        if (accept(kind)) {
            return tok;
        }
        String errorMessage = reportSyntaxError(kind);
        throw new QuitParseException(errorMessage);
    }

    private Token expectRetrieve (NonTerminal nt) {
        Token tok = currentToken;
        if (accept(nt)) {
            return tok;
        }
        String errorMessage = reportSyntaxError(nt);
        throw new QuitParseException(errorMessage);
    }


// Grammar Rules ==============================================================

    // function for matching rule that only expects nonterminal's FIRST set
    private Token matchNonTerminal (NonTerminal nt) {
        return expectRetrieve(nt);
    }

    // TODO: copy operators and type grammar rules from Compiler

    // literal = integerLit | floatLit
    private Token literal () {
        return matchNonTerminal(NonTerminal.LITERAL);
    }

    // TODO: copy remaining grammar rules from Compiler and make edits to build ast

    private void varDecl(){
        // typeDecl ident {"," ident} ";"
        expect(NonTerminal.VAR_DECL);
        expect(Token.Kind.IDENT);

        while (accept(Token.Kind.COMMA)){
            expect(Token.Kind.IDENT);
        }
        
        expect(Token.Kind.SEMICOLON);

    }

    private void statSeq(){
        statement();       
        expect(Token.Kind.SEMICOLON);
        
        while(have(NonTerminal.STATEMENT)){
            statement();
            expect(Token.Kind.SEMICOLON);
        }
    }

    private void statement(){
        // statement = assign | funcCall | ifStat | whileStat | repeatStat | returnStat . 
        if(have(NonTerminal.ASSIGN)){
            expect(NonTerminal.STATEMENT);
            assign();
        }
        else if(have(NonTerminal.FUNC_CALL)){
            expect(NonTerminal.STATEMENT);
            funcCall();
        }
        else if(have(NonTerminal.IF_STAT)){
            expect(NonTerminal.STATEMENT);
            ifStat();
        }
        // TODO: add whileStat here
        // TODO: add repeatStat here
        else if(have(NonTerminal.RETURN_STAT)){
            expect(NonTerminal.STATEMENT);
        }
        else{ // If none of these, then return an error message saying that we expected a STATEMENT
            expect(NonTerminal.STATEMENT);
        }
    }


    private void assign(){
        // assign = "let" designator ((assignOP relExpr) | unaryOp)
        // "let" should have already been consumed in statement()
        designator();

        // assignOP relEXpr
        if(accept(NonTerminal.ASSIGN_OP)){
            relExpr();
        }
        // unaryOp
        else if(accept(NonTerminal.UNARY_OP)){
        }
        else{ 
            expect(NonTerminal.ASSIGN_OP);
        }
    }

    private void designator () {
        // designator = ident { "[" relExpr "]" }
        expect(Token.Kind.IDENT);
        
        // TODO: add arrays here
    }

    private void relExpr(){
        // relExpr = addExpr {relOp addExpr}
        addExpr(); 

        while(accept(NonTerminal.REL_OP)){
            addExpr();
        }
    }

    private void addExpr(){
        // addExpr = multExpr {addOp multExpr}
        multExpr(); 

        while(accept(Token.Kind.ADD) || accept(Token.Kind.SUB) || accept(Token.Kind.SUB)){
            multExpr();
        }
    }

    private void multExpr(){
        // multExpr = powExpr {multOp powExpr}
        powExpr();

        while(accept(Token.Kind.MUL) || accept(Token.Kind.DIV) || accept(Token.Kind.MOD) || accept(Token.Kind.AND)){
            powExpr();
        }
    }

    private void powExpr(){
        // powExpr = groupExpr {powOP groupExpr}
        groupExpr();

        while(accept(Token.Kind.POW)){
            groupExpr();
        }
    }

    private void groupExpr(){
        // groupExpr = literal | designator | "not" relExpr | "(" relExpr ")" | funcCall
        if(have(NonTerminal.LITERAL)){
            literal(); // gets the literal
        }
        else if(have(NonTerminal.DESIGNATOR)){
            designator();
        }
        else if(accept(Token.Kind.NOT)){
            relExpr();
        }
        else if(accept(Token.Kind.OPEN_PAREN)){
            relExpr();
            expect(Token.Kind.CLOSE_PAREN);
        }
        else if(accept(NonTerminal.FUNC_CALL)){
            funcCall();
        }
        else{ 
            expect(NonTerminal.DESIGNATOR);
        }
    }

    private void funcCall(){
        // funcCall = "call" ident "(" [ relExpr { ", relExpr"} ] ")".
        accept(Token.Kind.IDENT);
        expect(Token.Kind.OPEN_PAREN);
        // TODO: add arrays here
        expect(Token.Kind.CLOSE_PAREN);
    }

    private void ifStat(){
        // ifStat = "if" relation "then" statSeq ["else" statSeq] "fi"
        expect(NonTerminal.RELATION);
        expect(Token.Kind.CLOSE_PAREN);
        expect(Token.Kind.THEN);

        statSeq();

        if (accept(Token.Kind.ELSE)){
            statSeq();
        }

        expect(Token.Kind.FI);
    }
    // computation	= "main" {varDecl} {funcDecl} "{" statSeq "}" "."
    private Computation computation () {
        expect(Token.Kind.MAIN);

        while(have(NonTerminal.VAR_DECL)){
            varDecl();
        }

        // TODO: add funcDecl

        expect(Token.Kind.OPEN_BRACE);

        statSeq();

        while(have(NonTerminal.STAT_SEQ)){
            statSeq();
        }

        expect(Token.Kind.CLOSE_BRACE);
        expect(Token.Kind.PERIOD);

        // TODO: change this return
        return null;
    }
}
