package pl434;

import java.util.HashSet;
import java.util.Set;

public enum NonTerminal {

    // nonterminal FIRST sets for grammar

    // operators
    POW_OP(new HashSet<Token.Kind>() {
        private static final long serialVersionUID = 1L;
        {
            add(Token.Kind.POW);
        }
    }),
    MULT_OP(new HashSet<Token.Kind>() {
        private static final long serialVersionUID = 1L;
        {
            add(Token.Kind.MUL);
            add(Token.Kind.DIV);
            add(Token.Kind.MOD);
            add(Token.Kind.AND);
        }
    }),
    ADD_OP(new HashSet<Token.Kind>() {
        private static final long serialVersionUID = 1L;
        {
            add(Token.Kind.ADD);
            add(Token.Kind.SUB);
            add(Token.Kind.OR);
        }
    }),
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
        }
    }),
    UNARY_OP(new HashSet<Token.Kind>() {
        private static final long serialVersionUID = 1L;
        {
            add(Token.Kind.UNI_INC);
            add(Token.Kind.UNI_DEC);
        }
    }),

    // literals (integer and float handled by Scanner)
    BOOL_LIT(new HashSet<Token.Kind>() {
        private static final long serialVersionUID = 1L;
        {
            add(Token.Kind.TRUE);
            add(Token.Kind.FALSE);
        }
    }),
    LITERAL(new HashSet<Token.Kind>() {
        private static final long serialVersionUID = 1L;
        {
            add(Token.Kind.INT_VAL);
            add(Token.Kind.FLOAT_VAL);
        }
    }),

    // designator (ident handled by Scanner)
    DESIGNATOR(new HashSet<Token.Kind>() {
        private static final long serialVersionUID = 1L;
        {
            add(Token.Kind.IDENT);
        }
    }),

    // factor, term, expression, relation, condition
    // FACTOR(new HashSet<Token.Kind>() {
    //     private static final long serialVersionUID = 1L;
    //     {
    //         throw new RuntimeException("implement condition FIRST set");
    //         addAll(LITERAL.firstSet);
    //         add(Token.Kind.OPEN_PAREN);
    //     }
    // }),
    // TERM(new HashSet<Token.Kind>() {
    //     private static final long serialVersionUID = 1L;
    //     {
    //         throw new RuntimeException("implement condition FIRST set");
    //         addAll(LITERAL.firstSet);
    //         add(Token.Kind.OPEN_PAREN);
    //     }
    // }),
    EXPRESSION(new HashSet<Token.Kind>() {
        private static final long serialVersionUID = 1L;
        {
            // throw new RuntimeException("implement condition FIRST set");
            addAll(LITERAL.firstSet);
            addAll(BOOL_LIT.firstSet);
            addAll(DESIGNATOR.firstSet);
            add(Token.Kind.NOT);
            add(Token.Kind.OPEN_PAREN);
            add(Token.Kind.CALL);
        }
    }),
    RELATION(new HashSet<Token.Kind>() {
        private static final long serialVersionUID = 1L;
        {
            add(Token.Kind.OPEN_PAREN);
        }
    }),
    // CONDITION(new HashSet<Token.Kind>() {
    //     private static final long serialVersionUID = 1L;
    //     {
    //         throw new RuntimeException("implement condition FIRST set");
    //     }
    // }),

    // statements
    ASSIGN(new HashSet<Token.Kind>() {
        private static final long serialVersionUID = 1L;
        {
            add(Token.Kind.LET);
        }
    }),
    FUNC_CALL(new HashSet<Token.Kind>() {
        private static final long serialVersionUID = 1L;
        {
            add(Token.Kind.CALL);
        }
    }),
    IF_STAT(new HashSet<Token.Kind>() {
        private static final long serialVersionUID = 1L;
        {
            add(Token.Kind.IF);
        }
    }),
    WHILE_STAT(new HashSet<Token.Kind>() {
        private static final long serialVersionUID = 1L;
        {
            add(Token.Kind.WHILE);
        }
    }),
    REPEAT_STAT(new HashSet<Token.Kind>() {
        private static final long serialVersionUID = 1L;
        {
            add(Token.Kind.REPEAT);
        }
    }),
    RETURN_STAT(new HashSet<Token.Kind>() {
        private static final long serialVersionUID = 1L;
        {
            add(Token.Kind.RETURN);
        }
    }),
    STATEMENT(new HashSet<Token.Kind>() {
        private static final long serialVersionUID = 1L;
        {
            addAll(ASSIGN.firstSet);
            addAll(FUNC_CALL.firstSet);
            addAll(IF_STAT.firstSet);
            addAll(WHILE_STAT.firstSet);
            addAll(REPEAT_STAT.firstSet);
            addAll(RETURN_STAT.firstSet);
        }
    }),
    STAT_SEQ(new HashSet<Token.Kind>() {
        private static final long serialVersionUID = 1L;
        {
            addAll(STATEMENT.firstSet);
        }
    }),

    // declarations
    TYPE_DECL(new HashSet<Token.Kind>() {
        private static final long serialVersionUID = 1L;
        {
            add(Token.Kind.INT);
            add(Token.Kind.BOOL);
            add(Token.Kind.FLOAT);
        }
    }),
    VAR_DECL(new HashSet<Token.Kind>() {
        private static final long serialVersionUID = 1L;
        {
            addAll(TYPE_DECL.firstSet);
        }
    }),
    PARAM_DECL(new HashSet<Token.Kind>() {
        private static final long serialVersionUID = 1L;
        {
            add(Token.Kind.INT);
            add(Token.Kind.BOOL);
            add(Token.Kind.FLOAT);
        }
    }),

    // functions
    FORMAL_PARAM(new HashSet<Token.Kind>() {
        private static final long serialVersionUID = 1L;
        {
            add(Token.Kind.OPEN_PAREN);
        }
    }),
    FUNC_BODY(new HashSet<Token.Kind>() {
        private static final long serialVersionUID = 1L;
        {
            add(Token.Kind.OPEN_PAREN);
        }
    }),
    FUNC_DECL(new HashSet<Token.Kind>() {
        private static final long serialVersionUID = 1L;
        {
            add(Token.Kind.FUNC);
        }
    }),

    // computation
    COMPUTATION(new HashSet<Token.Kind>() {
        private static final long serialVersionUID = 1L;
        {
            add(Token.Kind.MAIN);
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