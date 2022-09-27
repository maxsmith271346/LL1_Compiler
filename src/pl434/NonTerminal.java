package pl434;

import java.util.HashSet;
import java.util.Set;

public enum NonTerminal {

    // nonterminal FIRST sets for grammar

    // operators
    REL_OP(new HashSet<Token.Kind>() {
        private static final long serialVersionUID = 1L;
        {
            add(Token.Kind.EQUAL_TO);
            add(Token.Kind.NOT_EQUAL);
            add(Token.Kind.LESS_THAN);
            add(Token.Kind.LESS_EQUAL);
            add(Token.Kind.GREATER_EQUAL);
            add(Token.Kind.GREATER_THAN);
        }
    }),
    ASSIGN_OP(new HashSet<Token.Kind>() {
        private static final long serialVersionUID = 1L;
        {
            add(Token.Kind.ASSIGN); 
            add(Token.Kind.ADD_ASSIGN); 
            add(Token.Kind.SUB_ASSIGN);
            add(Token.Kind.MUL_ASSIGN);
            add(Token.Kind.DIV_ASSIGN);
            add(Token.Kind.MOD_ASSIGN);
            add(Token.Kind.POW_ASSIGN);

            //throw new RuntimeException("implement assignOp FIRST set");
        }
    }),
    UNARY_OP(new HashSet<Token.Kind>() {
        private static final long serialVersionUID = 1L;
        {
            add(Token.Kind.UNI_INC);
            add(Token.Kind.UNI_DEC);
            //throw new RuntimeException("implement unaryOp FIRST set");
        }
    }),

    // literals (integer and float handled by Scanner)
    BOOL_LIT(new HashSet<Token.Kind>() {
        private static final long serialVersionUID = 1L;
        {
            add(Token.Kind.TRUE);
            add(Token.Kind.FALSE); 
            //throw new RuntimeException("implement boolLit FIRST set");
        }
    }),
    LITERAL(new HashSet<Token.Kind>() {
        private static final long serialVersionUID = 1L;
        {
            //literal = boolLit | integerLit | floatLit
            add(Token.Kind.TRUE);
            add(Token.Kind.FALSE);

            add(Token.Kind.INT_VAL);

            add(Token.Kind.FLOAT_VAL);
            //add(NonTerminal.) what about the integerLit and floatLit? 
            //throw new RuntimeException("implement literal FIRST set");
        }
    }),

    // designator (ident handled by Scanner)
    DESIGNATOR(new HashSet<Token.Kind>() {
        private static final long serialVersionUID = 1L;
        {
            add(Token.Kind.IDENT);
            //throw new RuntimeException("implement designator FIRST set");
        }
    }),

    // factor, term, expression, relation, condition
    /*FACTOR(new HashSet<Token.Kind>() {
        private static final long serialVersionUID = 1L;
        {
           // System.out.println("Factor not implemented ");
           // throw new RuntimeException("implement factor FIRST set");
        }
    }),
    TERM(new HashSet<Token.Kind>() {
        private static final long serialVersionUID = 1L;
        {
            //System.out.println("TERM not implemented");
            //throw new RuntimeException("implement term FIRST set");
        }
    }),
    EXPRESSION(new HashSet<Token.Kind>() {
        private static final long serialVersionUID = 1L;
        {   
            //System.out.println("Expression not implemented");
            //throw new RuntimeException("implement expression FIRST set");
        }
    }),*/
    RELATION(new HashSet<Token.Kind>() {
        private static final long serialVersionUID = 1L;
        {
            add(Token.Kind.OPEN_PAREN);
            //throw new RuntimeException("implement relation FIRST set");
        }
    }),
    CONDITION(new HashSet<Token.Kind>() {
        private static final long serialVersionUID = 1L;
        {
            add(Token.Kind.IF);
            add(Token.Kind.WHILE);
            add(Token.Kind.REPEAT);
            //throw new RuntimeException("implement condition FIRST set");
        }
    }),

    // statements
    ASSIGN(new HashSet<Token.Kind>() {
        private static final long serialVersionUID = 1L;
        {
            add(Token.Kind.LET);
            //throw new RuntimeException("implement assign FIRST set");
        }
    }),
    FUNC_CALL(new HashSet<Token.Kind>() {
        private static final long serialVersionUID = 1L;
        {
            add(Token.Kind.CALL);
            //throw new RuntimeException("implement funcCall FIRST set");
        }
    }),
    IF_STAT(new HashSet<Token.Kind>() {
        private static final long serialVersionUID = 1L;
        {
            add(Token.Kind.IF);
            //throw new RuntimeException("implement ifStat FIRST set");
        }
    }),
    WHILE_STAT(new HashSet<Token.Kind>() {
        private static final long serialVersionUID = 1L;
        {
            add(Token.Kind.WHILE);
            //throw new RuntimeException("implement whileStat FIRST set");
        }
    }),
    REPEAT_STAT(new HashSet<Token.Kind>() {
        private static final long serialVersionUID = 1L;
        {
            add(Token.Kind.REPEAT);
            //throw new RuntimeException("implement repeatStat FIRST set");
        }
    }),
    RETURN_STAT(new HashSet<Token.Kind>() {
        private static final long serialVersionUID = 1L;
        {
            add(Token.Kind.RETURN);
            //throw new RuntimeException("implement returnStat FIRST set");
        }
    }),
    STATEMENT(new HashSet<Token.Kind>() {
        private static final long serialVersionUID = 1L;
        {
            addAll(ASSIGN.firstSet());
            addAll(FUNC_CALL.firstSet());
            addAll(IF_STAT.firstSet());
            addAll(WHILE_STAT.firstSet());
            addAll(REPEAT_STAT.firstSet());
            addAll(RETURN_STAT.firstSet());
            //System.out.println("Statement not implemented");
            //throw new RuntimeException("implement statement FIRST set");
        }
    }),
    STAT_SEQ(new HashSet<Token.Kind>() {
        private static final long serialVersionUID = 1L;
        {
            addAll(STATEMENT.firstSet());
            
            //System.out.println("Stat_Seq not implemented");
            //throw new RuntimeException("implement statSeq FIRST set");
        }
    }),

    // declarations
    TYPE_DECL(new HashSet<Token.Kind>() {
        private static final long serialVersionUID = 1L;
        {
            add(Token.Kind.BOOL);
            add(Token.Kind.INT);
            add(Token.Kind.FLOAT);
            //System.out.println("not implemented");
            //throw new RuntimeException("implement typeDecl FIRST set");
        }
    }),
    VAR_DECL(new HashSet<Token.Kind>() {
        private static final long serialVersionUID = 1L;
        {
            /*for (Token.Kind typeDeclFirstSet: TYPE_DECL.firstSet()){
                add(typeDeclFirstSet);
            }*/

            addAll(TYPE_DECL.firstSet());
            //System.out.println("var_decl not implemented");
            //throw new RuntimeException("implement varDecl FIRST set");
        }
    }),
    PARAM_DECL(new HashSet<Token.Kind>() {
        private static final long serialVersionUID = 1L;
        {
            addAll(TYPE_DECL.firstSet());
            //System.out.println("param decl not implemented");
            //throw new RuntimeException("implement paramDecl FIRST set");
        }
    }),

    // functions
    FORMAL_PARAM(new HashSet<Token.Kind>() {
        private static final long serialVersionUID = 1L;
        {
            add(Token.Kind.OPEN_PAREN);
            //throw new RuntimeException("implement formalParam FIRST set");
        }
    }),
    FUNC_BODY(new HashSet<Token.Kind>() {
        private static final long serialVersionUID = 1L;
        {
            add(Token.Kind.OPEN_BRACKET);
            //System.out.println("func_body not implemented");
            //throw new RuntimeException("implement funcBody FIRST set");
        }
    }),
    FUNC_DECL(new HashSet<Token.Kind>() {
        private static final long serialVersionUID = 1L;
        {
            add(Token.Kind.FUNC);
            //throw new RuntimeException("implement funcDecl FIRST set");
        }
    }),

    // computation
    COMPUTATION(new HashSet<Token.Kind>() {
        private static final long serialVersionUID = 1L;
        {
            add(Token.Kind.MAIN);
            //throw new RuntimeException("implement computation FIRST set");
        }
    })
    ;

    private final Set<Token.Kind> firstSet = new HashSet<>();

    private NonTerminal (Set<Token.Kind> set) {
        firstSet.addAll(set);
    }

    public final Set<Token.Kind> firstSet () {
        return firstSet;
    }
}
