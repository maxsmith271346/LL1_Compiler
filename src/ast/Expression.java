package ast;

public interface Expression extends Visitable {
    public void accept(NodeVisitor visitor); // this wasn't in the original code, could get rid of this
}
