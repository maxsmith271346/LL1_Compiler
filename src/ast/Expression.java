package ast;

import java.util.HashMap;
import java.util.HashSet;

import SSA.Operand;
import types.*;
import pl434.Symbol;

public interface Expression extends Visitable {
    public void accept(NodeVisitor visitor);
    public Type type();
    public Operand getOperand(HashMap<Symbol, HashSet<Symbol>> varMap);
}
