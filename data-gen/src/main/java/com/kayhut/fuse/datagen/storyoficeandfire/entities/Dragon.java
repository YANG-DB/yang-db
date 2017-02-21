

package com.kayhut.fuse.datagen.storyoficeandfire.entities;

/**
 *
 * @author smuel
 */
public class Dragon extends OntologyEntity{
    
    
    
    public boolean isFree ;
    
    
    public Dragon(String name , int idx) {
        
        super(name,idx) ;
        this.isFree = true ;
       
    }
    
    public Dragon(Dragon d) {
        super(d.name,d.id);
        this.isFree = true ;
        
    }
    
    

    @Override
    public String genString() {
        String str = String.valueOf(this.id) + "," + this.name ;
        return str;
    }

    @Override
    public String genShortString() {
        return String.valueOf(this.id);
    }
    
    
}
