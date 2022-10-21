package ast;

import SSA.Operand;
import types.*;

public interface Expression extends Visitable {
    public void accept(NodeVisitor visitor);
    public Type type();
    public Operand getOperand();
}
