package ast;
import types.Type;

public class ReturnStatement extends Node implements Statement {
        private Expression returnVal;

        public ReturnStatement(int lineNum, int charPos, Expression returnVal) {
            super(lineNum, charPos);
            this.returnVal = returnVal;
        }
    
        @Override
        public Type accept(NodeVisitor visitor) {
            return visitor.visit(this);
        }
    
        public Expression returnValue() {
            return returnVal;
        }
}
