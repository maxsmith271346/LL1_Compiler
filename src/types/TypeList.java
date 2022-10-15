package types;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TypeList extends Type implements Iterable<Type> {

    private List<Type> list;

    public TypeList () {
        list = new ArrayList<>();
    }

    public void append (Type type) {
        list.add(type);
    }

    public List<Type> getList () {
        return list;
    }

    @Override
    public Iterator<Type> iterator () {
        return list.iterator();
    }

    @Override
    public String toString(){
        List<String> listWithMessages = new ArrayList<>();
        for (Type t : list){
            if (t instanceof ErrorType){ 
                listWithMessages.add(t.getClass().getSimpleName() + "(" + ((ErrorType) t).getMessage() + ")");
            }
            else{ 
                listWithMessages.add(t.toString());
            }
        }
        String listStr = listWithMessages.toString();
        return this.getClass().getSimpleName() + "(" + listStr.substring(1, listStr.length() - 1) + ")";
    }
    
}
