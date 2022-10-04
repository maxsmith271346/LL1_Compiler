package ast;

import java.util.ArrayList;
import java.util.List;

public class ArgumentList extends Node {
    public List<Expression> argList;


    public ArgumentList(int lineNum, int charPos) {
        super(lineNum, charPos);
        argList = new ArrayList<Expression>();
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    public boolean empty(){
        return (argList.size() == 0);
    }
    
}
