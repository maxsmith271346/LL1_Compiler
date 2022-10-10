package ast;
import pl434.Symbol;

public class AST {

    public Computation computation; 

    // TODO: Create AST structure of your choice
    /*public AST(int lineNum, int charPos, Symbol main, DeclarationList vars, DeclarationList funcs,
    StatementSequence mainSeq) {
        computation = new Computation(lineNum, charPos, main, vars, funcs, mainSeq);
        //throw new RuntimeException("implement AST");
    }*/
    public AST(){

    }

    public String printPreOrder(){
        // TODO: Return the pre order traversal of AST. Use "\n" as separator.
        // Use the enum ASTNonTerminal provided for naming convention.
        //PrettyPrinter p = new PrettyPrinter();
        //p.visit(computation);
        //return p.toString().substring(0, p.toString().length() - 1);
        //throw new RuntimeException("implement printPreOrder function");
        return "";
    }
}
