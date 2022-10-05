package pl434;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import ast.*;
import pl434.Token.Kind;

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

    // Need to map from IDENT to memory offset

    public Compiler(Scanner scanner, int numRegs) {
        this.scanner = scanner;
        currentToken = this.scanner.next();
        numDataRegisters = numRegs;
        instructions = new ArrayList<>();
    }

    // TODO
    public AST genAST() {
        AST retAST = new AST();
        retAST.computation = computation();

        return retAST;
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
        throw new RuntimeException("implement initSymbolTable");
    }

    private void enterScope() {
        throw new RuntimeException("implement enterScope");
    }

    private void exitScope() {
        throw new RuntimeException("implement exitScope");
    }

    private Symbol tryResolveVariable(Token ident) {
        return null;
        // TODO: Try resolving variable, handle SymbolNotFoundError
    }

    private Symbol tryDeclareVariable(Token ident) {
        return null;
        // TODO: Try declaring variable, handle RedeclarationError
    }

    private String reportResolveSymbolError(String name, int lineNum, int charPos) {
        String message = "ResolveSymbolError(" + lineNum + "," + charPos + ")[Could not find " + name + ".]";
        errorBuffer.append(message + "\n");
        return message;
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
        Token tok = matchNonTerminal(NonTerminal.LITERAL);
        if (tok.kind() == Token.Kind.INT_VAL) {
            return new IntegerLiteral(lineNumber(), charPosition(), tok.lexeme());
        }
        else if (tok.kind() == Token.Kind.TRUE || tok.kind() == Token.Kind.FALSE) {
            return new BoolLiteral(lineNumber(), charPosition(), tok.lexeme());
        }
        else if (tok.kind() == Token.Kind.FLOAT_VAL) {
            return new FloatLiteral(lineNumber(), charPosition(), tok.lexeme());
        }
        return null;
    }

    // designator = ident { "[" relExpr "]" }
    private Symbol designator() {
        int lineNum = lineNumber();
        int charPos = charPosition();

        Token ident = expectRetrieve(Kind.IDENT);

        if (accept(Kind.OPEN_BRACKET)) {
            relExpr();
            expect(Kind.CLOSE_BRACKET);
        }

        // TODO: get the actual type from the Symbol table?
        return new Symbol(ident.lexeme(), "int", "variable");
    }

    // groupExpr = literal | designator | "not" relExpr | "(" relExpr ")"
    // | funcCall
    private Expression groupExpr() {
        Expression groupExpr;
        if (have(NonTerminal.LITERAL)) {
            groupExpr = literal();
            return groupExpr;
        } else if (have(NonTerminal.BOOL_LIT)) {
            boolLit();
        } else if (have(NonTerminal.DESIGNATOR)) {
            groupExpr = designator(); // In this case, the expression node will have an expression and a null
            return groupExpr;
        } else if (accept(Kind.NOT)) {
            Expression relExpr = relExpr();
            return new LogicalNot(lineNumber(), charPosition(), relExpr);
        } else if (accept(Kind.OPEN_PAREN)) {
            groupExpr =  relExpr();
            expect(Kind.CLOSE_PAREN);
            return groupExpr;
        } else {
            return funcCall();
        }

        return null;
    }

    // powExpr = groupExpr {powOp groupExpr}
    private Expression powExpr() {
        Expression lhsExpr = groupExpr();
        Expression rhsExpr = null; 

        while (have(NonTerminal.POW_OP)) {
            powOp();
            rhsExpr = groupExpr();
            lhsExpr = new Power(lineNumber(), charPosition(), lhsExpr, rhsExpr);
        }
        return lhsExpr;
    }

    // multExpr = powExpr {relOp powExpr}
    private Expression multExpr() {
        Expression lhsExpr = powExpr();
        Expression rhsExpr = null;

        while (have(NonTerminal.MULT_OP)) {
            //multOp();
            if (accept(Token.Kind.MUL)){
                rhsExpr = powExpr();
                lhsExpr = new Multiplication(lineNumber(), charPosition(), lhsExpr, rhsExpr);
            }
            else if(accept(Token.Kind.DIV)){
                rhsExpr = powExpr();
                lhsExpr = new Division(lineNumber(), charPosition(), lhsExpr, rhsExpr);
            }
            else if(accept(Token.Kind.MOD)){
                rhsExpr = powExpr();
                lhsExpr = new Modulo(lineNumber(), charPosition(), lhsExpr, rhsExpr);
            }
            else if(accept(Token.Kind.AND)){
                rhsExpr = powExpr();
                lhsExpr = new LogicalAnd(lineNumber(), charPosition(), lhsExpr, rhsExpr);   
            }
        }
        return lhsExpr;
    }

    // addExpr = multExpr {addOp multExpr}
    private Expression addExpr() {
        Expression lhsExpr = multExpr();
        Expression rhsExpr = null;
        Token opTok = null;

        while (have(NonTerminal.ADD_OP)) {
            //opTok = addOp();
            if (accept(Token.Kind.ADD)){
                rhsExpr = multExpr();
                lhsExpr = new Addition(lineNumber(), charPosition(), lhsExpr, rhsExpr);
            }
            else if(accept(Token.Kind.SUB)){
                rhsExpr = multExpr();
                lhsExpr = new Subtraction(lineNumber(), charPosition(), lhsExpr, rhsExpr);
            }
            else if(accept(Token.Kind.OR)){
                rhsExpr = multExpr();
                lhsExpr = new LogicalOr(lineNumber(), charPosition(), lhsExpr, rhsExpr);
            }
        }
        return lhsExpr;
    }

    // relExpr = addExpr {relOp addExpr}
    private Expression relExpr() {
        Expression lhsExpr = addExpr();
        Expression rhsExpr = null;
        Token opTok = null;

        while (have(NonTerminal.REL_OP)) {
            opTok = relOp();
            rhsExpr = addExpr();
            lhsExpr = new Relation(lineNumber(), charPosition(), opTok.lexeme(), lhsExpr, rhsExpr);
        }

        return lhsExpr;
    }

    // assign = "let" designator ((assignOp relExpr) | unaryOp)
    private Assignment assign() {
        Expression rhs = null;
        expect(NonTerminal.ASSIGN);
        Symbol designator = designator();
        Token op;

        if (have(NonTerminal.ASSIGN_OP)) {
            op = assignOp();
            rhs = relExpr();
            switch (op.kind()) {
                case ADD_ASSIGN:
                    rhs = new Addition(lineNumber(), charPosition(), designator, rhs);
                case SUB_ASSIGN:
                    rhs = new Subtraction(lineNumber(), charPosition(), designator, rhs);
                case MUL_ASSIGN:
                    rhs = new Multiplication(lineNumber(), charPosition(), designator, rhs);
                case DIV_ASSIGN:
                    rhs = new Division(lineNumber(), charPosition(), designator, rhs);
                case MOD_ASSIGN:
                    rhs = new Modulo(lineNumber(), charPosition(), designator, rhs);
                case POW_ASSIGN:
                    rhs = new Power(lineNumber(), charPosition(), designator, rhs);
            }
        } else {
            op = unaryOp();
            if (op.kind() == Kind.UNI_INC) {
                rhs = new Addition(lineNumber(), charPosition(), rhs, new IntegerLiteral(lineNumber(), charPosition(), "1"));
            }
            else {
                rhs = new Subtraction(lineNumber(), charPosition(), rhs, new IntegerLiteral(lineNumber(), charPosition(), "1"));
            }
        }

        Assignment assign = new Assignment(lineNumber(), charPosition(), designator, rhs);
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
        Symbol symbol = new Symbol("TEMP FUNC", "int", "function");
        ArgumentList arguments = new ArgumentList(lineNumber(), charPosition());
        expect(NonTerminal.FUNC_CALL);
        expect(Kind.IDENT); // I added this - Emory
        expect(Kind.OPEN_PAREN);

        if (have(NonTerminal.EXPRESSION)) {
            do {
                arguments.argList.add(relExpr());
            } while (accept(Kind.COMMA));
        }

        expect(Kind.CLOSE_PAREN);

        FunctionCall funcCall = new FunctionCall(lineNumber(), charPosition(), symbol, arguments);
        return funcCall;
    }

    // ifStat = "if" relation "then" statSeq ["else" statSeq] "fi"
    private IfStatement ifStat() {
        expect(NonTerminal.IF_STAT);
        Expression rel = relation();
        expect(Kind.THEN);
        StatementSequence thenStatSeq = statSeq();
        StatementSequence elseStatSeq = null;

        if (accept(Kind.ELSE)) {
            elseStatSeq = statSeq();
        }

        expect(Kind.FI);

        return new IfStatement(lineNumber(), charPosition(), rel, thenStatSeq, elseStatSeq);
    }

    // whileStat = "while" relation "do" statSeq "od"
    private WhileStatement whileStat() {
        expect(NonTerminal.WHILE_STAT);
        Expression exp = relation();
        expect(Kind.DO);
        StatementSequence statSeq = statSeq();
        expect(Kind.OD);

        return new WhileStatement(lineNumber(), charPosition(), exp, statSeq);
    }

    // repeatStat = "repeat" statSeq "until" relation
    private RepeatStatement repeatStat() {
        expect(NonTerminal.REPEAT_STAT);
        StatementSequence statSeq = statSeq();
        expect(Kind.UNTIL);
        Expression exp = relation();

        return new RepeatStatement(lineNumber(), charPosition(), exp, statSeq);
    }

    // returnStat = "return" [relExpr]
    private ReturnStatement returnStat() {
        Expression returnVal = null;
        expect(NonTerminal.RETURN_STAT);
        if (have(NonTerminal.EXPRESSION)) {
            returnVal = relExpr();
        }
        return new ReturnStatement(lineNumber(), charPosition(), returnVal);
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
        StatementSequence statSeq = new StatementSequence(lineNumber(), charPosition());
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
        if (accept(Kind.OPEN_BRACKET)) {
            expect(Kind.INT_VAL);
            expect(Kind.CLOSE_BRACKET);
        }
        return tok;
    }

    // varDecl = typeDecl ident {"," ident} ";"
    private DeclarationList varDecl() {
        // create a new Declaration List node and fill it with Variable Declarations
        DeclarationList vars = new DeclarationList(lineNumber(), charPosition());
        VariableDeclaration varDec;

        Token typeTok = typeDecl();
        do {
            Token identTok = expectRetrieve(Kind.IDENT);
            varDec = new VariableDeclaration(lineNumber(), charPosition(), typeTok.lexeme(), identTok.lexeme());
            vars.decList.add(varDec);
        } while (accept(Kind.COMMA));
        expect(Kind.SEMICOLON);

        return vars;
    }

    // paramType = type { "[" "]" }
    private void paramType() {
        type();
        if (accept(Kind.OPEN_BRACKET)) {
            expect(Kind.CLOSE_BRACKET);
        }
    }

    // paramDecl = paramType ident
    private void paramDecl() {
        paramType();
        expectRetrieve(Kind.IDENT);
    }

    // formalParam = "(" [ paramDecl { "," paramDecl } ] ")"
    private void formalParam() {
        expect(Kind.OPEN_PAREN);
        if (have(NonTerminal.PARAM_DECL)) {
            do {
                paramDecl();
            } while (accept(Kind.COMMA));
        }
        expect(Kind.CLOSE_PAREN);
    }

    // funcBody = "{" { varDecl } statSeq "}" ";"
    private FunctionBody funcBody() {
        FunctionBody funcBody;
        DeclarationList varDecl = new DeclarationList(0, 0);
        StatementSequence statSeq;

        expect(Kind.OPEN_BRACE);

        while (have(NonTerminal.VAR_DECL)) {
            varDecl = varDecl();
        } // TODO: do this like in main?

        statSeq = statSeq();
        expect(Kind.CLOSE_BRACE);
        expect(Kind.SEMICOLON);

        funcBody = new FunctionBody(lineNumber(), charPosition(), varDecl, statSeq);
        return funcBody;
    }

    // funcDecl = "function" ident formalParam ":" ( "void" | type ) funcBody
    private DeclarationList funcDecl() {
        DeclarationList funcs = new DeclarationList(lineNumber(), charPosition());
        FunctionDeclaration funcDec;
        FunctionBody funcBody;
        Token typeTok = new Token("void", 0, 0);

        expect(NonTerminal.FUNC_DECL);
        Token identTok = expectRetrieve(Kind.IDENT);

        formalParam();

        expect(Kind.COLON);

        if (!accept(Kind.VOID)) {
            typeTok = type();
        }

        funcBody = funcBody();

        funcDec = new FunctionDeclaration(lineNumber(), charPosition(), typeTok.lexeme(), identTok.lexeme(), funcBody);
        funcs.decList.add(funcDec);
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
        while (have(NonTerminal.FUNC_DECL)) {
            funcs = funcDecl();
        }

        expect(Kind.OPEN_BRACE);
        mainSeq = statSeq();
        expect(Kind.CLOSE_BRACE);
        expect(Kind.PERIOD);

        Symbol compSymbol = new Symbol("main", "void", "function");
        return new Computation(0, 0, compSymbol, vars, funcs, mainSeq);

    }
}
