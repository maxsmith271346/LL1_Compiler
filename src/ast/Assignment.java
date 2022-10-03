package ast;

import pl434.Symbol;

public class Assignment extends Node implements Statement{

    public Assignment(int lineNum, int charPos) {
        super(lineNum, charPos);
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
