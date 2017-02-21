
package com.kayhut.fuse.datagen.storyoficeandfire.relations;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
/**
 *
 * @author smuel
 */
public class RelasionList<T> {
    public List<T> rList ;
    public ListIterator<T> li ;
    
    public RelasionList() {
        this.rList = new ArrayList<T>() ;
    }
    
    public void setList(T rlist) {
        this.rList.add(rlist) ;
        
    }
    
    public T getRelasion(int idx) {
        return this.rList.get(idx) ;
    }
    
    public void clearList() {
        rList.clear();
    }
}
