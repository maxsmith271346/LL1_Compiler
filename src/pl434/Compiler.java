package pl434;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;

import SSA.SSA;
import ast.*;
import pl434.Token.Kind;

import org.apache.commons.cli.*;


public class Compiler {

    // Error Reporting ============================================================
    private StringBuilder errorBuffer = new StringBuilder();

    private String reportSyntaxError(NonTerminal nt) {
        String message = "SyntaxError(" + lineNumber() + "," + charPosition() + ")[Expected a token from " + nt.name()
                + " but got " + currentToken.kind + ".]";
        errorBuffer.append(message + "\n");
        return message;
    }

    private String reportSyntaxError(Token.Kind kind) {
        String message = "SyntaxError(" + lineNumber() + "," + charPosition() + ")[Expected " + kind + " but got "
                + currentToken.kind + ".]";
        errorBuffer.append(message + "\n");
        return message;
    }

    public String errorReport() {
        return errorBuffer.toString();
    }

    public boolean hasError() {
        return errorBuffer.length() != 0;
    }

    private class QuitParseException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public QuitParseException(String errorMessage) {
            super(errorMessage);
        }
    }

    private int lineNumber() {
        return currentToken.lineNumber();
    }

    private int charPosition() {
        return currentToken.charPosition();
    }

    // Compiler ===================================================================
    private Scanner scanner;
    private Token currentToken;

    private int numDataRegisters; // available registers are [1..numDataRegisters]
    private List<Integer> instructions;
    
    private SymbolTable rootScope = new SymbolTable("_root");
    private SymbolTable currScope = rootScope;

    private HashMap<String, FunctionCall> functionResolution;

    private SSA ssa; 

    // Need to map from IDENT to memory offset

    public Compiler(Scanner scanner, int numRegs) {
        this.scanner = scanner;
        currentToken = this.scanner.next();
        numDataRegisters = numRegs;
        instructions = new ArrayList<>();

        functionResolution = new HashMap<String, FunctionCall>();

        initSymbolTable();
    }

    public AST genAST() {
        AST retAST = new AST();
        retAST.computation = computation();

        return retAST;
    }

    public SSA genSSA(AST ast){
        ssa = new SSA(ast);
        return ssa;
    }

    public String optimization(List<String> optArguments, Options options){
        System.out.println("optArguments " + optArguments); 
        Optimization optimization = new Optimization(ssa);
        for (String opt : optArguments){
            System.out.println(opt);
            switch(opt){
                case "cp": 
                    optimization.constantPropagation();
                    break;
                case "cf": 
                    optimization.constantFolding();
                    break;
                case "cpp": 
                    optimization.copyPropagation();
                    break;
                case "cse": 
                    optimization.commonSubexpressionElimination();
                    break;
                case "dce":
                    optimization.deadCodeElimination(); 
                    break;
                case "ofe": 
                    optimization.orphanFunctionElimination();
                    break; 
            }
        }
        //Optimizer.copyPropagation(ssa);
        return ssa.asDotGraph();
    }
    public int[] compile() {
        initSymbolTable();
        try {
            computation();
            return instructions.stream().mapToInt(Integer::intValue).toArray();
        } catch (QuitParseException q) {
            errorBuffer.append("SyntaxError(" + lineNumber() + "," + charPosition() + ")");
            errorBuffer.append("[Could not complete parsing.]");
            return new ArrayList<Integer>().stream().mapToInt(Integer::intValue).toArray();
        }
    }

    // SymbolTable Management =====================================================
    private SymbolTable symbolTable;

    private void initSymbolTable() {
        // Initialization of root scope

        // TODO: At some point, should likely change Symbol constructor to take in
        // list of types
        rootScope.insert("readInt", new Symbol("readInt", "int", "func"));
        rootScope.insert("readFloat", new Symbol("readFloat", "float", "func"));
        rootScope.insert("readBool", new Symbol("readBool", "bool", "func"));
        rootScope.insert("printInt", new Symbol("printInt", "void", "func", new ArrayList<String>(Arrays.asList("int"))));
        rootScope.insert("printFloat", new Symbol("printFloat", "void", "func", new ArrayList<String>(Arrays.asList("float"))));
        rootScope.insert("printBool", new Symbol("printBool", "void", "func", new ArrayList<String>(Arrays.asList("bool"))));
        rootScope.insert("println", new Symbol("println", "void", "func"));
    }

    private void enterScope(String scopeName) {
        currScope = new SymbolTable(scopeName, currScope);
    }

    private void exitScope() {
        currScope = currScope.getParentTable();
    }
    private List<Symbol> tryResolveVariable(Token ident) {
        try {
            return currScope.lookup(ident.lexeme());
        } catch (SymbolNotFoundError e) {
            reportResolveSymbolError(ident.lexeme(), lineNumber(), charPosition());
        }
        List<Symbol> symbolList = new ArrayList<Symbol>();
        return symbolList;
    }

    private List<Symbol> tryResolveFunction(Token ident, FunctionCall funcCall) {
        try {
            return currScope.lookup(ident.lexeme());
        } catch (SymbolNotFoundError e) {
            functionResolution.put(ident.lexeme(), funcCall);
        }
        List<Symbol> symbolList = new ArrayList<Symbol>();
        return symbolList;
    }

    private void resolveFunctions(){
        List<Symbol> funcSymbol;
        for(String fR : functionResolution.keySet()){
            try{ 
                funcSymbol = currScope.lookup(fR);
                functionResolution.get(fR).putFunc(funcSymbol);
            } catch (SymbolNotFoundError e) {
                insertResolveSymbolError(fR, functionResolution.get(fR).lineNumber(), functionResolution.get(fR).lineNumber());
            }
        }
    } 
    
    private void tryDeclareVariable(Token ident, Symbol symbol) {
        try {
            currScope.insert(ident.lexeme(), symbol);
        } catch (RedeclarationError e) {
            reportDeclareSymbolError(ident.lexeme(), lineNumber(), charPosition());
        }
    }

    private String reportResolveSymbolError(String name, int lineNum, int charPos) {
        String message = "ResolveSymbolError(" + lineNum + "," + charPos + ")[Could not find " + name + ".]";
        errorBuffer.append(message + "\n");
        return message;
    }

    private void insertResolveSymbolError(String name, int lineNum, int charPos){
        List<String> errorBufferList = Arrays.asList(errorBuffer.toString().split("\n"));

        if (errorBuffer.length() != 0){
            int offset = 0;
            int openParenIndex = 0; 
            int commaIndex = 0;
            int closeParenIndex = 0;
            for (String error : errorBufferList){
                openParenIndex = error.indexOf("(");
                commaIndex = error.indexOf(",");
                closeParenIndex = error.indexOf(")");
                if (Integer.parseInt(error.substring(openParenIndex + 1, commaIndex)) < lineNum){
                    offset += error.length() + 1;
                }
                else if (Integer.parseInt(error.substring(openParenIndex + 1, commaIndex)) == lineNum){
                    if (Integer.parseInt(error.substring(commaIndex + 1, closeParenIndex)) < charPos){
                        offset += error.length() + 1;
                    }
                }
            }
            String message = "ResolveSymbolError(" + lineNum + "," + charPos + ")[Could not find " + name + ".]";
            errorBuffer.insert(offset, message + "\n");
        }   
        else{ 
            reportResolveSymbolError(name, lineNum, charPos);
        }
    }

    private String reportDeclareSymbolError(String name, int lineNum, int charPos) {
        String message = "DeclareSymbolError(" + lineNum + "," + charPos + ")[" + name + " already exists.]";
        errorBuffer.append(message + "\n");
        return message;
    }

    // Helper Methods =============================================================
    private boolean have(Token.Kind kind) {
        return currentToken.is(kind);
    }

    private boolean have(NonTerminal nt) {
        return nt.firstSet().contains(currentToken.kind);
    }

    private boolean accept(Token.Kind kind) {
        if (have(kind)) {
            try {
                currentToken = scanner.next();
            } catch (NoSuchElementException e) {
                if (!kind.equals(Token.Kind.EOF)) {
                    String errorMessage = reportSyntaxError(kind);
                    throw new QuitParseException(errorMessage);
                }
            }
            return true;
        }
        return false;
    }

    private boolean accept(NonTerminal nt) {
        if (have(nt)) {
            currentToken = scanner.next();
            return true;
        }
        return false;
    }

    private boolean expect(Token.Kind kind) {
        if (accept(kind)) {
            return true;
        }
        String errorMessage = reportSyntaxError(kind);
        throw new QuitParseException(errorMessage);
    }

    private boolean expect(NonTerminal nt) {
        if (accept(nt)) {
            return true;
        }
        String errorMessage = reportSyntaxError(nt);
        throw new QuitParseException(errorMessage);
    }

    private Token expectRetrieve(Token.Kind kind) {
        Token tok = currentToken;
        if (accept(kind)) {
            return tok;
        }
        String errorMessage = reportSyntaxError(kind);
        throw new QuitParseException(errorMessage);
    }

    private Token expectRetrieve(NonTerminal nt) {
        Token tok = currentToken;
        if (accept(nt)) {
            return tok;
        }
        String errorMessage = reportSyntaxError(nt);
        throw new QuitParseException(errorMessage);
    }

    // Grammar Rules ==============================================================
    // function for matching rule that only expects nonterminal's FIRST set
    private Token matchNonTerminal(NonTerminal nt) {
        return expectRetrieve(nt);
    }

    // powOp = "^"
    private Token powOp() {
        return matchNonTerminal(NonTerminal.POW_OP);
    }

    private Token multOp() {
        return matchNonTerminal(NonTerminal.MULT_OP);
    }

    private Token addOp() {
        return matchNonTerminal(NonTerminal.ADD_OP);
    }

    private Token relOp() {
        return matchNonTerminal(NonTerminal.REL_OP);
    }

    private Token assignOp() {
        return matchNonTerminal(NonTerminal.ASSIGN_OP);
    }

    private Token unaryOp() {
        return matchNonTerminal(NonTerminal.UNARY_OP);
    }

    // type = "bool" | "int" | "float"
    private Token type() {
        return matchNonTerminal(NonTerminal.TYPE_DECL);
    }

    // boolLit = "true" | "false"
    private Token boolLit() {
        return matchNonTerminal(NonTerminal.BOOL_LIT);
    }

    // literal = integerLit | floatLit
    private Expression literal() {
        int lineNum = lineNumber();
        int charPos = charPosition();
        Token tok = matchNonTerminal(NonTerminal.LITERAL);

        if (tok.kind() == Token.Kind.INT_VAL) {
            return new IntegerLiteral(lineNum, charPos, tok.lexeme());
        }
        else if (tok.kind() == Token.Kind.TRUE || tok.kind() == Token.Kind.FALSE) {
            return new BoolLiteral(lineNum, charPos, tok.lexeme());
        }
        else if (tok.kind() == Token.Kind.FLOAT_VAL) {
            return new FloatLiteral(lineNum, charPos, tok.lexeme());
        }
        return null;
    }

    // designator = ident { "[" relExpr "]" }
    private Expression designator() {
        int lineNum = lineNumber();
        int charPos = charPosition();

        Token ident = expectRetrieve(Kind.IDENT);

        Expression designator = tryResolveVariable(ident).get(0); 

        // while
        /*if (accept(Kind.OPEN_BRACKET)){
            Expression lhsExpr = designator;
            Expression rhsExpr =  relExpr();
            ArrayIndex arrayIndex = new ArrayIndex(lineNum, charPos, lhsExpr, rhsExpr);
            expect(Kind.CLOSE_BRACKET);

            while(accept(Kind.OPEN_BRACKET)){
                rhsExpr = relExpr();
                arrayIndex.leftExpr = new ArrayIndex(lineNum, charPos, lhsExpr, rhsExpr);
                expect(Kind.CLOSE_BRACKET);

                lineNum = lineNumber();
                charPos = charPosition();
            }
            return arrayIndex;
        }*/
        if (accept(Kind.OPEN_BRACKET)){
            List<Expression> indices = new ArrayList<Expression>();
            indices.add(relExpr());
            expect(Kind.CLOSE_BRACKET);

            lineNum = lineNumber();
            charPos = charPosition();

            while (accept(Kind.OPEN_BRACKET)){
                indices.add(relExpr());
                expect(Kind.CLOSE_BRACKET);
            }

            ArrayIndex arrayIndex = new ArrayIndex(lineNum, charPos, (Symbol) designator, indices);

            return arrayIndex;
        }
        // TODO: get the actual type from the Symbol table?
        return designator;
    }

    // groupExpr = literal | designator | "not" relExpr | "(" relExpr ")"
    // | funcCall
    private Expression groupExpr() {
        int lineNum = lineNumber();
        int charPos = charPosition();
        Expression groupExpr;

        if (have(NonTerminal.LITERAL)) {
            groupExpr = literal();
            return groupExpr;
        } else if (have(NonTerminal.BOOL_LIT)) {
            Token tok = boolLit();
            return new BoolLiteral(lineNum, charPos, tok.lexeme());
        } else if (have(NonTerminal.DESIGNATOR)) {
            groupExpr = designator(); // In this case, the expression node will have an expression and a null
            return groupExpr;
        } else if (accept(Kind.NOT)) {
            Expression relExpr = relExpr();
            return new LogicalNot(lineNum, charPos, relExpr);
        } else if (accept(Kind.OPEN_PAREN)) {
            groupExpr =  relExpr();
            expect(Kind.CLOSE_PAREN);
            return groupExpr;
        } else {
            return funcCall();
        }
    }

    // powExpr = groupExpr {powOp groupExpr}
    private Expression powExpr() {
        int lineNum = lineNumber();
        int charPos = charPosition();

        Expression lhsExpr = groupExpr();
        Expression rhsExpr = null; 

        while (have(NonTerminal.POW_OP)) {
            powOp();
            rhsExpr = groupExpr();
            lhsExpr = new Power(lineNum, charPos, lhsExpr, rhsExpr);

            lineNum = lineNumber();
            charPos = charPosition();
        }
        return lhsExpr;
    }

    // multExpr = powExpr {relOp powExpr}
    private Expression multExpr() {
        int lineNum = lineNumber();
        int charPos = charPosition();

        Expression lhsExpr = powExpr();
        Expression rhsExpr = null;

        while (have(NonTerminal.MULT_OP)) {
            //multOp();
            if (accept(Token.Kind.MUL)){
                rhsExpr = powExpr();
                lhsExpr = new Multiplication(lineNum, charPos, lhsExpr, rhsExpr);
            }
            else if(accept(Token.Kind.DIV)){
                rhsExpr = powExpr();
                lhsExpr = new Division(lineNum, charPos, lhsExpr, rhsExpr);
            }
            else if(accept(Token.Kind.MOD)){
                rhsExpr = powExpr();
                lhsExpr = new Modulo(lineNum, charPos, lhsExpr, rhsExpr);
            }
            else if(accept(Token.Kind.AND)){
                rhsExpr = powExpr();
                lhsExpr = new LogicalAnd(lineNum, charPos, lhsExpr, rhsExpr);   
            }

            lineNum = lineNumber();
            charPos = charPosition();
        }
        return lhsExpr;
    }

    // addExpr = multExpr {addOp multExpr}
    private Expression addExpr() {
        int lineNum = lineNumber();
        int charPos = charPosition();

        Expression lhsExpr = multExpr();
        Expression rhsExpr = null;
        Token opTok = null;

        while (have(NonTerminal.ADD_OP)) {
            //opTok = addOp();
            if (accept(Token.Kind.ADD)){
                rhsExpr = multExpr();
                lhsExpr = new Addition(lineNum, charPos, lhsExpr, rhsExpr);
            }
            else if(accept(Token.Kind.SUB)){
                rhsExpr = multExpr();
                lhsExpr = new Subtraction(lineNum, charPos, lhsExpr, rhsExpr);
            }
            else if(accept(Token.Kind.OR)){
                rhsExpr = multExpr();
                lhsExpr = new LogicalOr(lineNum, charPos, lhsExpr, rhsExpr);
            }

            lineNum = lineNumber();
            charPos = charPosition();
        }
        return lhsExpr;
    }

    // relExpr = addExpr {relOp addExpr}
    private Expression relExpr() {
        Expression lhsExpr = addExpr();
        Expression rhsExpr = null;
        Token opTok = null;

        while (have(NonTerminal.REL_OP)) {
            int lineNum = lineNumber();
            int charPos = charPosition();

            opTok = relOp();
            rhsExpr = addExpr();
            lhsExpr = new Relation(lineNum, charPos, opTok.lexeme(), lhsExpr, rhsExpr);

            lineNum = lineNumber();
            charPos = charPosition();
        }

        return lhsExpr;
    }

    // assign = "let" designator ((assignOp relExpr) | unaryOp)
    private Assignment assign() {
        int lineNum = lineNumber();
        int charPos = charPosition();

        Expression rhs = null;
        expect(NonTerminal.ASSIGN);
        Expression designator = designator();

        Token op;

        if (have(NonTerminal.ASSIGN_OP)) {
            op = assignOp();
            rhs = relExpr();
            switch (op.kind()) {
                case ADD_ASSIGN:
                    rhs = new Addition(lineNum, charPos, designator, rhs);
                    break; 
                case SUB_ASSIGN:
                    rhs = new Subtraction(lineNum, charPos, designator, rhs);
                    break; 
                case MUL_ASSIGN:
                    rhs = new Multiplication(lineNum, charPos, designator, rhs);
                    break; 
                case DIV_ASSIGN:
                    rhs = new Division(lineNum, charPos, designator, rhs);
                    break; 
                case MOD_ASSIGN:
                    rhs = new Modulo(lineNum, charPos, designator, rhs);
                    break; 
                case POW_ASSIGN:
                    rhs = new Power(lineNum, charPos, designator, rhs);
                    break;
            }
        } else {
            op = unaryOp();
            if (op.kind() == Kind.UNI_INC) {
                rhs = new Addition(lineNum, charPos, designator, new IntegerLiteral(lineNum, charPos, "1"));
            }
            else {
                rhs = new Subtraction(lineNum, charPos, designator, new IntegerLiteral(lineNum, charPos, "1"));
            }
        }

        Assignment assign = new Assignment(lineNum, charPos, designator, rhs);
        return assign;
    }

    // relation = "(" relExpr ")"
    private Expression relation() {
        Expression expr; 
        expect(NonTerminal.RELATION);
        expr = relExpr();
        expect(Kind.CLOSE_PAREN);
        
        return expr; 
    }

    // funcCall = "call" ident "(" [relExpr {"," relExpr}] ")"
    private FunctionCall funcCall() {
        int lineNum = lineNumber();
        int charPos = charPosition();

        FunctionCall funcCall = new FunctionCall(lineNum, charPos);
        //Symbol symbol = new Symbol("TEMP FUNC", "int", "func");
        ArgumentList arguments = new ArgumentList(lineNum, charPos);
        expect(NonTerminal.FUNC_CALL);

        Token identTok = expectRetrieve(Kind.IDENT); 
        //Symbol symbol = tryResolveVariable(identTok); 
        List<Symbol> symbol = tryResolveFunction(identTok, funcCall);
        expect(Kind.OPEN_PAREN);

        if (have(NonTerminal.EXPRESSION)) {
            do {
                arguments.argList.add(relExpr());
            } while (accept(Kind.COMMA));
        }

        expect(Kind.CLOSE_PAREN);

        funcCall.putArgs(arguments);
        funcCall.putFunc(symbol);
        return funcCall;
    }

    // ifStat = "if" relation "then" statSeq ["else" statSeq] "fi"
    private IfStatement ifStat() {
        int lineNum = lineNumber();
        int charPos = charPosition();

        expect(NonTerminal.IF_STAT);
        Expression rel = relation();
        expect(Kind.THEN);
        StatementSequence thenStatSeq = statSeq();
        StatementSequence elseStatSeq = null; //CHECK 

        if (accept(Kind.ELSE)) {
            elseStatSeq = statSeq();
        }

        expect(Kind.FI);

        return new IfStatement(lineNum, charPos, rel, thenStatSeq, elseStatSeq);
    }

    // whileStat = "while" relation "do" statSeq "od"
    private WhileStatement whileStat() {
        int lineNum = lineNumber();
        int charPos = charPosition();

        expect(NonTerminal.WHILE_STAT);
        Expression exp = relation();
        expect(Kind.DO);
        StatementSequence statSeq = statSeq();
        expect(Kind.OD);

        return new WhileStatement(lineNum, charPos, exp, statSeq);
    }

    // repeatStat = "repeat" statSeq "until" relation
    private RepeatStatement repeatStat() {
        int lineNum = lineNumber();
        int charPos = charPosition();

        expect(NonTerminal.REPEAT_STAT);
        StatementSequence statSeq = statSeq();
        expect(Kind.UNTIL);
        Expression exp = relation();

        return new RepeatStatement(lineNum, charPos, exp, statSeq);
    }

    // returnStat = "return" [relExpr]
    private ReturnStatement returnStat() {
        int lineNum = lineNumber();
        int charPos = charPosition();

        Expression returnVal = null;
        expect(NonTerminal.RETURN_STAT);
        if (have(NonTerminal.EXPRESSION)) {
            returnVal = relExpr();
        }
        return new ReturnStatement(lineNum, charPos, returnVal);
    }

    // statement = assign | funcCall | ifStat | whileStat |
    // repeatStat | returnStat
    private Statement statement() {
        Statement statement = null;
        if (have(NonTerminal.ASSIGN)) {
            statement = assign();
        } else if (have(NonTerminal.FUNC_CALL)) {
            statement = funcCall();
        } else if (have(NonTerminal.IF_STAT)) {
           statement = ifStat();
        } else if (have(NonTerminal.WHILE_STAT)) {
            statement = whileStat();
        } else if (have(NonTerminal.REPEAT_STAT)) {
            statement = repeatStat();
        } else {
            statement = returnStat();
        }

        return statement;
    }

    // statSeq = statement ";" {statement ";"}
    private StatementSequence statSeq() {
        int lineNum = lineNumber();
        int charPos = charPosition();

        StatementSequence statSeq = new StatementSequence(lineNum, charPos);
        Statement statement;
        do {
            statement = statement();
            statSeq.statSeq.add(statement);
            expect(Kind.SEMICOLON);
        } while (have(NonTerminal.STATEMENT));

        return statSeq;
    }

    // typeDecl = type { "[" integerLit "]" }
    private Token typeDecl() {
        Token tok = type();
        /*while (accept(Kind.OPEN_BRACKET)) {
            expect(Kind.INT_VAL);
            expect(Kind.CLOSE_BRACKET);
        }*/
        return tok;
    }

    // varDecl = typeDecl ident {"," ident} ";"
    private DeclarationList varDecl() {
        int lineNum = lineNumber();
        int charPos = charPosition();

        // create a new Declaration List node and fill it with Variable Declarations
        DeclarationList vars = new DeclarationList(lineNum, charPos);
        VariableDeclaration varDec;
        List<String> dimList = new ArrayList<String>();

        Token typeTok = typeDecl();

        // moved this from typeDecl since it only returns a Token & this is the only method that calls typeDecl
        while (accept(Kind.OPEN_BRACKET)) {
            dimList.add(expectRetrieve(Kind.INT_VAL).lexeme());
            expect(Kind.CLOSE_BRACKET);
        }

        do {
            lineNum = lineNumber();
            charPos = charPosition();

            Token identTok = expectRetrieve(Kind.IDENT);
            varDec = new VariableDeclaration(lineNum, charPos, typeTok.lexeme(), identTok.lexeme());

            lineNum = lineNumber();
            charPos = charPosition();
            
            if (dimList.size() != 0){
                varDec.symbol().addDims(dimList);
            }
    
            tryDeclareVariable(identTok, varDec.symbol());

            vars.decList.add(varDec);
        } while (accept(Kind.COMMA));
        expect(Kind.SEMICOLON);

        return vars;
    }

    // paramType = type { "[" "]" }
    private String paramType() {
        Token t = type();
        String pType = t.lexeme();
        // TODO: optional: extend enum to include array types 
        if (accept(Kind.OPEN_BRACKET)) {
            expect(Kind.CLOSE_BRACKET);
        }

        return pType;
    }

    // paramDecl = paramType ident
    private String paramDecl() {
        String pType = paramType();
        Token ident = expectRetrieve(Kind.IDENT);

        Symbol symbol = new Symbol(ident.lexeme(), pType, "param");

        tryDeclareVariable(ident, symbol);

        return pType;
    }

    // formalParam = "(" [ paramDecl { "," paramDecl } ] ")"
    private ArrayList<String> formalParam() {
        expect(Kind.OPEN_PAREN);
        ArrayList<String> paramTypes = new ArrayList<String>();
        if (have(NonTerminal.PARAM_DECL)) {
            do {
                //paramDecl();
                // Add parameter types to the paramTypes list
                paramTypes.add(paramDecl());
            } while (accept(Kind.COMMA));
        }
        expect(Kind.CLOSE_PAREN);

        return paramTypes;
    }

    // funcBody = "{" { varDecl } statSeq "}" ";"
    private FunctionBody funcBody() {
        int lineNum = lineNumber();
        int charPos = charPosition();

        FunctionBody funcBody;
        DeclarationList varDecl = new DeclarationList(lineNum, charPos);
        StatementSequence statSeq;

        expect(Kind.OPEN_BRACE);

        while (have(NonTerminal.VAR_DECL)) {
            varDecl = varDecl();
        } // TODO: do this like in main?

        statSeq = statSeq();
        expect(Kind.CLOSE_BRACE);
        expect(Kind.SEMICOLON);

        funcBody = new FunctionBody(lineNum, charPos, varDecl, statSeq);
        return funcBody;
    }

    // funcDecl = "function" ident formalParam ":" ( "void" | type ) funcBody
    private DeclarationList funcDecl() {
        int lineNum = lineNumber();
        int charPos = charPosition();

        DeclarationList funcs = new DeclarationList(lineNum, charPos);
        FunctionDeclaration funcDec;
        FunctionBody funcBody;
        Token typeTok = new Token("void", 0, 0);

        expect(NonTerminal.FUNC_DECL);
        Token identTok = expectRetrieve(Kind.IDENT);

        // Declare function in parent scope (still needs paramTypes list)
        Symbol func = new Symbol(identTok.lexeme(), typeTok.lexeme(), "func");

        // save off the char pos and line number in case they are needed later for an error
        int funcCharPos = charPosition();
        int funcLineNum = lineNumber();

        tryDeclareVariable(identTok, func);
        // Enter function scope
        enterScope(identTok.lexeme());

        ArrayList<String> paramTypes = formalParam();

        func.addParams(paramTypes);

        expect(Kind.COLON);

         // Set func return type
         // special case for void because void is not a valid variable type so didn't want to modify the type() method 
        if (currentToken.kind != Token.Kind.VOID){
            func.setReturnType(type().lexeme());
        }
        else{ 
            func.setReturnType(Token.Kind.VOID.getDefaultLexeme());
            accept(Token.Kind.VOID);
        }

        funcBody = funcBody();

        funcDec = new FunctionDeclaration(funcLineNum, funcCharPos, func, funcBody);
        funcs.decList.add(funcDec);

        // Exit function scope
        exitScope();

        // Check for param conflicts here
        try {
            currScope.checkParamConflicts(identTok.lexeme());
        } catch (RedeclarationError e) {
            reportDeclareSymbolError(identTok.lexeme(), funcLineNum, funcCharPos);
        }

        return funcs;
    }

    // computation = "main" {varDecl} {funcDecl} "{" statSeq "}" "."
    private Computation computation() {
        expect(Kind.MAIN);

        DeclarationList vars = new DeclarationList(lineNumber(), charPosition());
        DeclarationList funcs = new DeclarationList(lineNumber(), charPosition());
        StatementSequence mainSeq = new StatementSequence(lineNumber(), charPosition());

        // Make the vars list for the first "set"/"line" of variable declarations
        if (have(NonTerminal.TYPE_DECL)) {
            vars = varDecl();
        }

        // Then add to it for subsequent "sets"/"lines" of variable declarations
        while (have(NonTerminal.TYPE_DECL)) {
            vars.decList.addAll(varDecl().decList);
        }

        if (have(NonTerminal.TYPE_DECL)) {
            funcs = funcDecl();
        }

        while (have(NonTerminal.FUNC_DECL)) {
            funcs.decList.addAll(funcDecl().decList);
        }
        

        expect(Kind.OPEN_BRACE);
        mainSeq = statSeq();
        expect(Kind.CLOSE_BRACE);
        expect(Kind.PERIOD);

        resolveFunctions();

        Symbol compSymbol = new Symbol("main", "void", "func");
        return new Computation(0, 0, compSymbol, vars, funcs, mainSeq);

    }
}
