package ast;

import pl434.Token;



public abstract class Node implements Visitable {

    private int lineNum;
    private int charPos;
    public static boolean displayLocation = false;

    protected Node (int lineNum, int charPos) {
        this.lineNum = lineNum;
        this.charPos = charPos;
    }

    public int lineNumber () {
        return lineNum;
    }

    public int charPosition () {
        return charPos;
    }

    public String getClassInfo () {
        String loc = "";
        if (displayLocation) {
            loc = "(" + lineNumber() + "," + charPosition() + ")";
        }
        
        return this.getClass().getSimpleName() + loc;
    }

    @Override
    public String toString () {
        String loc = "";
        if (displayLocation) {
            loc = "(" + lineNumber() + "," + charPosition() + ")";
        }
        
        return this.getClass().getSimpleName() + loc;
    }

    // Some factory method
    public static Statement newAssignment (int lineNum, int charPos, Expression dest, Token assignOp, Expression src) {
        // TODO: implement this
        return null;

    }

    public static Expression newExpression (Expression leftSide, Token op, Expression rightSide) {
        // TODO: implement this
        return null; 
    }

    public static Expression newLiteral (Token tok) {
        //TODO: implement this
        return null;
    }
}