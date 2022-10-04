package ast;

public interface Declaration extends Visitable {
    public void accept(NodeVisitor visitor);
}
