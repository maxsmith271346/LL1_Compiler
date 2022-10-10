package ast;

import types.Type;

public interface Expression extends Visitable {
    public Type accept(NodeVisitor visitor); // this wasn't in the original code, could get rid of this
}
