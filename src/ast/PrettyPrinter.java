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
        println(node, "[" + node.main() + "]");
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

    public void visit(FunctionCall node){
        //TODO implement this - I added this method
        println(node, "");
    }

    public void visit(IfStatement node){
        //TODO implement this - I added this method
        println(node, "");
    }

    public void visit(Relation node){
        //TODO implement this - I added this method
        println(node, "[" + node.operator() + "]");
        depth++; 
        node.leftExpression().accept(this);
        node.rightExpression().accept(this);
        depth--;
    }

    public void visit(RepeatStatement node){
        //TODO implement this - I added this method
        println(node, "");
    }

    public void visit(WhileStatement node){
        //TODO implement this - I added this method
        println(node, "");
    }

    @Override
    public void visit(IntegerLiteral node) {
        println(node, "[" + node.value() + "]");
        
    }

    public void visit(Symbol node){
        println(node);
    }
}