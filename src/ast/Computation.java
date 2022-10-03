package ast;

import pl434.Symbol;

public class Computation extends Node {

    private Symbol main;
    private DeclarationList vars; // make DeclarationList a class that extends Node?
    private DeclarationList funcs;
    private StatementSequence mainSeq; // make StatementSequence a class that extends Node? 


    public Computation(int lineNum, int charPos, Symbol main, DeclarationList vars, DeclarationList funcs,
            StatementSequence mainSeq) {
        super(lineNum, charPos);
        this.main = main;
        this.vars = vars;
        this.funcs = funcs;
        this.mainSeq = mainSeq;
    }

    /*public Symbol main() { //TODO
        return main;
    }*/

    public String main(){
        return main.name();
    }

    public DeclarationList variables() {
        return vars;
    }

    public DeclarationList functions() {
        return funcs;
    }

    public StatementSequence mainStatementSequence() {
        return mainSeq;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
