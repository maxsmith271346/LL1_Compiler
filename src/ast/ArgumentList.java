package ast;

import java.util.ArrayList;
import java.util.List;

import types.Type;

public class ArgumentList extends Node {
    public List<Expression> argList;


    public ArgumentList(int lineNum, int charPos) {
        super(lineNum, charPos);
        argList = new ArrayList<Expression>();
    }

    @Override
    public Type accept(NodeVisitor visitor) {
       return visitor.visit(this);
    }

    public boolean empty(){
        return (argList.size() == 0);
    }
    
}
