package pl434;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import ast.AST;
import pl434.Token.Kind;

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
    }

    private Symbol tryDeclareVariable (Token ident) {
        //TODO: Try declaring variable, handle RedeclarationError
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

    // powOp = "^"
    private Token powOp () {
        return matchNonTerminal(NonTerminal.POW_OP);
    }

    private Token multOp () {
        return matchNonTerminal(NonTerminal.MULT_OP);
    }

    private Token addOp () {
        return matchNonTerminal(NonTerminal.ADD_OP);
    }

    private Token relOp () {
        return matchNonTerminal(NonTerminal.REL_OP);
    }

    private Token assignOp() {
        return matchNonTerminal(NonTerminal.ASSIGN_OP);
    }

    private Token unaryOp () {
        return matchNonTerminal(NonTerminal.UNARY_OP);
    }

    // type = "bool" | "int" | "float"
    private Token type () {
        return matchNonTerminal(NonTerminal.TYPE_DECL);
    }

    // boolLit = "true" | "false"
    private Token boolLit () {
        return matchNonTerminal(NonTerminal.BOOL_LIT);
    }

    // literal = integerLit | floatLit
    private Token literal () {
        return matchNonTerminal(NonTerminal.LITERAL);
    }

    // designator = ident { "[" relExpr "]" }
    private void designator () {
        int lineNum = lineNumber();
        int charPos = charPosition();
        Token ident = expectRetrieve(Kind.IDENT);

        if (accept(Kind.OPEN_BRACKET)) {
            relExpr();
            expect(Kind.CLOSE_BRACKET);
        }
        return new Symbol(ident.lexeme(), "type here", "variable");
    }

    // groupExpr = literal | designator | "not" relExpr | "(" relExpr ")"
    // | funcCall
    private void groupExpr() {
        if (have(NonTerminal.LITERAL)) { literal(); }
        else if (have(NonTerminal.BOOL_LIT)) { boolLit(); }
        else if (have(NonTerminal.DESIGNATOR)) { designator(); }
        else if (accept(Kind.NOT)) {relExpr(); }
        else if (accept(Kind.OPEN_PAREN)) {
            relExpr();
            expect(Kind.CLOSE_PAREN);
        }
        else { funcCall(); }
    }

    // powExpr = groupExpr {powOp groupExpr}
    private void powExpr() {
        groupExpr();
        while (have(NonTerminal.POW_OP)) {
            powOp();
            groupExpr();
        }
    }
    
    // multExpr = powExpr {relOp powExpr}
    private void multExpr() {
        powExpr();
        while (have(NonTerminal.MULT_OP)) {
            multOp();
            powExpr();
        }
    }

    // addExpr = multExpr {addOp multExpr}
    private void addExpr() {
        multExpr();
        while (have(NonTerminal.ADD_OP)) {
            addOp();
            multExpr();
        }
    }

    // relExpr = addExpr {relOp addExpr}
    private void relExpr() {
        addExpr();
        while (have(NonTerminal.REL_OP)) {
            relOp();
            addExpr();
        }
    }

    // assign = "let" designator ((assignOp relExpr) | unaryOp)
    private void assign () {
        expect(NonTerminal.ASSIGN);
        designator();
        
        if (accept(NonTerminal.ASSIGN_OP)) {
            relExpr();
        }
        else { unaryOp(); }
    }

    // relation = "(" relExpr ")"
    private void relation () {
        expect(NonTerminal.RELATION);
        relExpr();
        expect(Kind.CLOSE_PAREN);
    }

    // funcCall = "call" ident "(" [relExpr {"," relExpr}] ")"
    private void funcCall () {
        expect(NonTerminal.FUNC_CALL);
        expect(Kind.OPEN_PAREN);

        if (have(NonTerminal.EXPRESSION)) {
            do {
                relExpr();
            } while (accept(Kind.COMMA));
        }

        expect(Kind.CLOSE_PAREN);
    }

    // ifStat = "if" relation "then" statSeq ["else" statSeq] "fi"
    private void ifStat () {
        expect(NonTerminal.IF_STAT);
        relation();
        expect(Kind.THEN);
        statSeq();

        if (accept(Kind.ELSE)) {
            statSeq();
        }

        expect(Kind.FI);
    }

    // whileStat = "while" relation "do" statSeq "od"
    private void whileStat () {
        expect(NonTerminal.WHILE_STAT);
        relation();
        expect(Kind.DO);
        statSeq();
        expect(Kind.OD);
    }

    // repeatStat = "repeat" statSeq "until" relation
    private void repeatStat () {
        expect(NonTerminal.REPEAT_STAT);
        statSeq();
        expect(Kind.UNTIL);
        relation();
    }

    // returnStat = "return" [relExpr]
    private void returnStat () {
        expect(NonTerminal.RETURN_STAT);
        if (have(NonTerminal.EXPRESSION)) {
            relExpr();
        }
    }

    // statement = assign | funcCall | ifStat | whileStat |
    // repeatStat | returnStat
    private void statement () {
        if (have(NonTerminal.ASSIGN)) { assign(); }
        else if (have(NonTerminal.FUNC_CALL)) { funcCall(); }
        else if (have(NonTerminal.IF_STAT)) { ifStat(); }
        else if (have(NonTerminal.WHILE_STAT)) { whileStat(); }
        else if (have(NonTerminal.REPEAT_STAT)) { repeatStat(); }
        else { returnStat(); }
    }

    // statSeq = statement ";" {statement ";"}
    private void statSeq () {
        do {
            statement();
            expect(Kind.SEMICOLON);
        } while (have(NonTerminal.STATEMENT)); 
    }

    // typeDecl = type { "[" integerLit "]" }
    private void typeDecl () {
        type();
        if (accept(Kind.OPEN_BRACKET)) {
            expect(Kind.INT_VAL);
            expect(Kind.CLOSE_BRACKET);
        }
    }

    // varDecl = typeDecl ident {"," ident} ";"
    private void varDecl () {
        typeDecl();
        do {
            expectRetrieve(Kind.IDENT);
        } while (accept(Kind.COMMA));
        expect(Kind.SEMICOLON);
    }

    // paramType = type { "[" "]" }
    private void paramType () {
        type();
        if (accept(Kind.OPEN_BRACKET)) {
            expect(Kind.CLOSE_BRACKET);
        }
    }
 
    // paramDecl = paramType ident
    private void paramDecl () {
        paramType();
        expectRetrieve(Kind.IDENT);
    }

    // formalParam = "(" [ paramDecl { "," paramDecl } ] ")"
    private void formalParam () {
        expect(Kind.OPEN_PAREN);
        if (have(NonTerminal.PARAM_DECL)) {
            do {
                paramDecl();
            } while (accept(Kind.COMMA));
        }
        expect(Kind.CLOSE_PAREN);
    }

    // funcBody = "{" { varDecl } statSeq "}" ";"
    private void funcBody () {
        expect(Kind.OPEN_BRACE);

        while (have(NonTerminal.VAR_DECL)) { varDecl(); }

        statSeq();
        expect(Kind.CLOSE_BRACE);
        expect(Kind.SEMICOLON);
    }

    // funcDecl = "function" ident formalParam ":" ( "void" | type ) funcBody
<<<<<<< HEAD
    private void funcDecl () {
=======
    private DeclarationList funcDecl () {
        DeclarationList funcs = new DeclarationList(lineNumber(), charPosition());
        FunctionDeclaration funcDec; 
        FunctionBody funcBody;
        Token typeTok = new Token("void", 0, 0); 

>>>>>>> 39d9071e170bd527ed2ce30b2820c6de64031bff
        expect(NonTerminal.FUNC_DECL);
        expectRetrieve(Kind.IDENT);
        formalParam();

        expect(Kind.COLON);
        
        if (!accept(Kind.VOID)) { typeTok = type(); }

<<<<<<< HEAD
        funcBody();
=======
        funcBody = funcBody();

        funcDec = new FunctionDeclaration(lineNumber(), charPosition(), typeTok.lexeme(), identTok.lexeme(), funcBody);
        funcs.decList.add(funcDec);
        return funcs;
>>>>>>> 39d9071e170bd527ed2ce30b2820c6de64031bff
    }

    // computation	= "main" {varDecl} {funcDecl} "{" statSeq "}" "."
    private void computation () {
        
        expect(Kind.MAIN);

        while (have(NonTerminal.TYPE_DECL)) { varDecl(); }
        while (have(NonTerminal.FUNC_DECL)) { funcDecl(); }

        expect(Kind.OPEN_BRACE);
        statSeq();
        expect(Kind.CLOSE_BRACE);
        expect(Kind.PERIOD);
<<<<<<< HEAD
=======


        Symbol compSymbol = new Symbol("main", "void", "function");
        return new Computation(0, 0, compSymbol, vars, funcs, mainSeq); 
>>>>>>> 39d9071e170bd527ed2ce30b2820c6de64031bff
        
    }
}
