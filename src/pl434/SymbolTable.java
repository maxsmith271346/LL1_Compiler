package pl434;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {

    // TODO: Create Symbol Table structure
    private SymbolTable parentTable;
    private String scopeName; // TODO: is this needed?
    private Map<String, Symbol> symbols;

    public SymbolTable (String scopeName, SymbolTable parentTable) {
        this.parentTable = parentTable;
        this.scopeName = scopeName;
        this.symbols = new HashMap<String, Symbol>();
    }

    public SymbolTable (String scopeName) {
        this.parentTable = null;
        this.scopeName = scopeName;
        this.symbols = new HashMap<String, Symbol>();
    }

    // lookup name in SymbolTable
    public Symbol lookup (String name) throws SymbolNotFoundError {
        // TODO: Need to handle function overloading?
        SymbolTable currTable = this;
        Symbol s;
        do {
            s = currTable.symbols.get(name);
            if (s != null) {
                return s;
            }
            currTable = currTable.parentTable;
        } while (currTable != null);
        
        throw new SymbolNotFoundError(name);
    }

    // insert name in SymbolTable
    // public Symbol insert (String name) throws RedeclarationError {}
    public void insert (String name, Symbol symbol) throws RedeclarationError {
        if (symbols.get(name) != null) {
            throw new RedeclarationError(name);
        }
        
        symbols.put(name, symbol);
    }

    public Map<String, Symbol> getSymbols () {
        return symbols;
    }

    public SymbolTable getParentTable () {
        return parentTable;
    }

    public String getScopeName () {
        return scopeName;
    }

}

class SymbolNotFoundError extends Error {

    private static final long serialVersionUID = 1L;
    private final String name;

    public SymbolNotFoundError (String name) {
        super("Symbol " + name + " not found.");
        this.name = name;
    }

    public String name () {
        return name;
    }
}

class RedeclarationError extends Error {

    private static final long serialVersionUID = 1L;
    private final String name;

    public RedeclarationError (String name) {
        super("Symbol " + name + " being redeclared.");
        this.name = name;
    }

    public String name () {
        return name;
    }
}