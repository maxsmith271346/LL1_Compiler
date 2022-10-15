package ast;

import pl434.Symbol;

public class PrettyPrinter implements NodeVisitor {

    private int depth = 0;
    private StringBuilder sb = new StringBuilder();

    private void println (Node n, String message) {
        String indent = "";
        for (int i = 0; i < depth; i++) {
            indent += "  ";
        }
        sb.append(indent + n.getClassInfo() + message + "\n");
    }

    private void println(Symbol symbol){
        String indent = "";
        for (int i = 0; i < depth; i++) {
            indent += "  ";
        }
        sb.append(indent + symbol + "\n");
        if (symbol.arrayIndex != null){
            symbol.arrayIndex.accept(this);
        }
    }

    @Override
    public String toString () {
        return sb.toString();
    }

    @Override
    public void visit (StatementSequence node) {
        println(node, "");
        depth++;
        for (Statement s : node.statSeq) { // TODO: make statement sequence iterable
            s.accept(this);
        }
        depth--;
    }

    @Override
    public void visit (VariableDeclaration node) {
        println(node, "[" + node.symbol() + "]");
    }

    @Override
    public void visit (FunctionDeclaration node) {
        println(node, "[" + node.function() + "]");
        depth++;
        node.body().accept(this);
        depth--;
    }

    @Override
    public void visit (DeclarationList node) {
        if (node.empty()) return;
        println(node, "");
        depth++;
        for (Declaration d : node.decList) { // TODO: make statement sequence iterable
            d.accept(this);
        }
        depth--;
    }

    @Override
    public void visit (Computation node) {
        println(node, "[" + node.main()+ "]");
        depth++;
        node.variables().accept(this);
        node.functions().accept(this);
        node.mainStatementSequence().accept(this);
        depth--;
    }

    @Override
    public void visit(FunctionBody node) {
        // TODO implement this - I tried to just mimic the computation one
        println(node, "");
        depth++;
        node.variables().accept(this);
        node.statements().accept(this);
        depth--;
        
    }

    @Override
    public void visit(Assignment node){
        //TODO implement this - I added this method
        println(node, ""); 
        depth++;
        node.lhsDesignator().accept(this);
        node.rhsExpr().accept(this); // TODO: This assumes that the rhs is an expression 
        depth--;
    }
    @Override
    public void visit(FunctionCall node){
        //TODO implement this - I added this method
        println(node, "[" + node.function() + "]");
        depth++; 
        node.argList.accept(this);
        depth--;
    }
    @Override
    public void visit(ArgumentList node){
        //TODO implement this - I added this method
        println(node, "");
        depth++; 
        if (!node.empty()){
            for (Expression e : node.argList) { // TODO: make statement sequence iterable
                e.accept(this);
            }
        }
        depth--;
    }
    @Override
    public void visit(IfStatement node){
        //TODO implement this - I added this method
        println(node, "");
        depth++; 
        node.condition().accept(this);
        node.thenStatementSeq().accept(this);
        if (node.elseStatementSeq() != null){
            node.elseStatementSeq().accept(this);
        }
        depth--;
    }

    @Override
    public void visit(Relation node){
        //TODO implement this - I added this method
        println(node, "[" + node.operator() + "]");
        depth++; 
        node.leftExpression().accept(this);
        node.rightExpression().accept(this);
        depth--;
    }

    @Override
    public void visit(RepeatStatement node){
        //TODO implement this - I added this method
        println(node, "");
        depth++;
        node.statementSeq().accept(this);
        node.condition().accept(this);
        depth--;
    }
    @Override
    public void visit(WhileStatement node){
        //TODO implement this - I added this method
        println(node, "");
        depth++; 
        node.condition().accept(this);
        node.statementSeq().accept(this);

    }

    @Override
    public void visit(IntegerLiteral node) {
        println(node, "[" + node.value() + "]");
        
    }
    @Override
    public void visit(FloatLiteral node) {
        println(node, "[" + node.value() + "]");
        
    }
    @Override
    public void visit(BoolLiteral node) {
        println(node, "[" + node.value() + "]");
        
    }

    @Override
    public void visit(Symbol node){
        println(node);
    }

    @Override
    public void visit(Addition node){
        println(node, "");
        depth++; 
        node.leftExpression().accept(this);
        node.rightExpression().accept(this);
        depth--;
    }

    @Override
    public void visit(Subtraction node){
        println(node, "");
        depth++; 
        node.leftExpression().accept(this);
        node.rightExpression().accept(this);
        depth--;
    }

    @Override
    public void visit(LogicalOr node){
        println(node, "");
        depth++; 
        node.leftExpression().accept(this);
        node.rightExpression().accept(this);
        depth--;
    }

    @Override
    public void visit(Multiplication node){
        println(node, "");
        depth++; 
        node.leftExpression().accept(this);
        node.rightExpression().accept(this);
        depth--;
    }
    @Override
    public void visit(Division node){
        println(node, "");
        depth++; 
        node.leftExpression().accept(this);
        node.rightExpression().accept(this);
        depth--;
    }
    @Override
    public void visit(Modulo node){
        println(node, "");
        depth++; 
        node.leftExpression().accept(this);
        node.rightExpression().accept(this);
        depth--;
    }
    @Override
    public void visit(LogicalAnd node){
        println(node, "");
        depth++; 
        node.leftExpression().accept(this);
        node.rightExpression().accept(this);
        depth--;
    }
    @Override
    public void visit(Power node){
        println(node, "");
        depth++; 
        node.leftExpression().accept(this);
        node.rightExpression().accept(this);
        depth--;
    }
    @Override
    public void visit(LogicalNot node){
        println(node, "");
        depth++; 
        node.expr().accept(this);
        depth--;
    }

    @Override
    public void visit(ReturnStatement node) {
        println(node, "");
        depth++; 
        if (node.returnValue() != null){
            node.returnValue().accept(this);
        }
        depth--;
    }

	@Override
	public void visit(ArrayIndex node) {
		println(node, "");
        depth++; 
        //node.leftExpr().accept(this);
        //node.rightExpr().accept(this);
        depth--;
		
	}
}