package ast;

public class ReturnStatement extends Node implements Statement {
        private Expression returnVal;

        public ReturnStatement(int lineNum, int charPos, Expression returnVal) {
            super(lineNum, charPos);
            this.returnVal = returnVal;
        }
    
        @Override
        public void accept(NodeVisitor visitor) {
            visitor.visit(this);
        }
    
        public Expression returnValue() {
            return returnVal;
        }
}
