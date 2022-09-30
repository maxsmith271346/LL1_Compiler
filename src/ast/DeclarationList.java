package ast;

import java.util.List;


// make Declaration List iterable 
public class DeclarationList extends Node {
    public List<Node> decList; 

    protected DeclarationList(int lineNum, int charPos, String ident) {
        super(lineNum, charPos);
        decList = new List<Node>(); // TODO
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
    
}
