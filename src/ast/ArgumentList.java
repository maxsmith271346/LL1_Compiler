package ast;

import java.util.ArrayList;
import java.util.List;

import types.TypeList;

public class ArgumentList extends Node {
    public List<Expression> argList;
    public TypeList type;

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

    public TypeList type(){
        return type; 
    }

    public void addType(TypeList type){
        this.type = type;
    }
    
}
