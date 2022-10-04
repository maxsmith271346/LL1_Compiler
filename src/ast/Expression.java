package ast;

public interface Expression extends Visitable {
    public void accept(NodeVisitor visitor);
}
