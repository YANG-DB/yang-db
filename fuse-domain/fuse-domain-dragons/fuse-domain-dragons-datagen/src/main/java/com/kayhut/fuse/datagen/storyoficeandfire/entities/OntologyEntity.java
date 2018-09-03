
package com.kayhut.fuse.datagen.storyoficeandfire.entities;

/**
 *
 * @author shmuel mashiach
 * 
 */
public abstract class OntologyEntity {

    
    public String name ;
    public int id ;
    
    public OntologyEntity(String name,int idx) {
        this.name = name+"_"+String.valueOf(idx);
        this.id = idx;
    }
    
    public abstract String genString();
    
    public abstract String genShortString() ;
}
