package ast;

import types.*;

public interface Expression extends Visitable {
    public void accept(NodeVisitor visitor);
    public Type type();
}
