package pl434;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SymbolTable {

    // TODO: Create Symbol Table structure
    private SymbolTable parentTable;
    private String scopeName; // TODO: is this needed?
    //private Map<String, Symbol> symbols;
    private Map<String, List<Symbol>> symbols;

    public SymbolTable (String scopeName, SymbolTable parentTable) {
        this.parentTable = parentTable;
        this.scopeName = scopeName;
        this.symbols = new HashMap<String, List<Symbol>>();
    }

    public SymbolTable (String scopeName) {
        this.parentTable = null;
        this.scopeName = scopeName;
        this.symbols = new HashMap<String, List<Symbol>>();
    }

    // lookup name in SymbolTable
    public List<Symbol> lookup (String name) throws SymbolNotFoundError {
        // TODO: Need to handle function overloading?
        SymbolTable currTable = this;
        List<Symbol> s;
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
            // if there is a name clash, check if the clash is with a function
            if (symbol.getSymbolType().equals("func") || symbols.get(name).get(0).getSymbolType().equals("func")){
                // go ahead and add the new symbol to the list
                // will check the param types later
                symbols.get(name).add(0, symbol);    
            }
            else{
                throw new RedeclarationError(name);
            }
        }
        else{
            List<Symbol> symbolList = new ArrayList<Symbol>();
            symbolList.add(symbol);
            symbols.put(name, symbolList);
        }
    }

    public Map<String, List<Symbol>> getSymbols () {
        return symbols;
    }

    public SymbolTable getParentTable () {
        return parentTable;
    }

    public String getScopeName () {
        return scopeName;
    }

    public void checkParamConflicts(String ident){
        if (symbols.get(ident).get(0).getSymbolType() == "func"){
           if (symbols.get(ident).size() > 1){
                for (int i = 0; i < symbols.get(ident).size(); i++){
                    for (int j = i + 1; j < symbols.get(ident).size(); j++){ 
                        if (symbols.get(ident).get(i).getParamTypes().equals(symbols.get(ident).get(j).getParamTypes())){
                            symbols.get(ident).remove(j);
                            throw new RedeclarationError(ident);
                        }
                    }
                }
           }
        }
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