package ast;
import types.Type;


public interface Declaration extends Visitable {
    public Type accept(NodeVisitor visitor);
}
