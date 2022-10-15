package ast;

import java.util.ArrayList;
import java.util.List;


// TODO: make Declaration List iterable 
public class DeclarationList extends Node {
    public List<Declaration> decList; 

    public DeclarationList(int lineNum, int charPos) {
        super(lineNum, charPos);
        decList = new ArrayList<Declaration>(); 
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    public boolean empty(){
        if (decList.size() == 0){
            return true; 
        }
        return false;
    }

    
    
}
