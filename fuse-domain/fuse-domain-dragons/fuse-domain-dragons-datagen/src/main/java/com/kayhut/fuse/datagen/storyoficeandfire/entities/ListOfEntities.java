
package com.kayhut.fuse.datagen.storyoficeandfire.entities;
 
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 *
 * @author smuel
 * @param <T>
 
 */
public class ListOfEntities <T> {
   
    public List<T> entities ;
    public ListIterator<T> li ;
    
    public ListOfEntities() {
        this.entities = new ArrayList<T>();
    }
    
    public void setList(T entity) {
        this.entities.add(entity);
    }
    
    public int getListSize() {
        return this.entities.size() ;
    }
    
    public T getEntity(int idx) {
        return entities.get(idx) ;
    } 
    
}
