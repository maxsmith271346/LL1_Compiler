package ast;

import pl434.Symbol;

public class DeclarationList extends Node {

    // TODO: add elements here
    private int lineNum;
    private int charPos;

    protected DeclarationList (int lineNum, int charPos) {
        super(lineNum, charPos);
    }
	
    @Override
	public void accept(NodeVisitor visitor) {
		// TODO Auto-generated method stub
		
	}
}