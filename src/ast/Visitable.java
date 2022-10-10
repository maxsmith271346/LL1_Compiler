package ast;

import types.Type;

public interface Visitable {

    public Type accept (NodeVisitor visitor);
}
