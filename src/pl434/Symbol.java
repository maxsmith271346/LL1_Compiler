package pl434;

import java.util.ArrayList;
import java.util.List;

public class Symbol {

    public enum Type{ 
        VOID("void"),
        BOOL("bool"),
        INT("int"),
        FLOAT("float");

        private String defaultTypeStr; 

        Type (String typeStr){
            defaultTypeStr = typeStr;
        }

        public String getDefaultTypeStr(){ 
            return this.defaultTypeStr;
        }

        public boolean matches (String type){
            if (this.defaultTypeStr.equals(type)){
                return true; 
            }
            else{ 
                return false; 
            }
        }
    }

    private String name;
    private Type type; 
    private String symbolType; // function, variable, etc TODO enum here - maybe we want to make classes that extend symbol for the different kinds? 
    public List<String> paramTypes;

    public Symbol (String name, String type, String symbolType) {
        this.name = name;
        this.symbolType = symbolType; 
        paramTypes = new ArrayList<String>();

        for (Type t: Type.values()){
            if(type.equals(t.getDefaultTypeStr())){
                this.type = t; 
                break;
            }
        }
    }
    public String name () {
        return name;
    }

    @Override
    public String toString(){
        if (symbolType.equals("function")){
            String paramTypesStr = paramTypes.toString();
            return name + ":(" + paramTypesStr.replace("[", "").replace("]", "") + ")->" + type.getDefaultTypeStr();
        } 
        else if (symbolType.equals("variable")){ 
            return name + ":" + type.getDefaultTypeStr();
        }  
        else{ 
            return "";
        }
    }


}
