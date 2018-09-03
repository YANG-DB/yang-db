
package com.kayhut.fuse.datagen.storyoficeandfire.entities;

/**
 *
 * @author smuel
 */
public class Kingdom extends OntologyEntity {
    
    public Kingdom(String name , int idx) {
        super(name,idx) ;
    }
    
    public Kingdom(Kingdom k) {
        super(k.name,k.id);
    }

    @Override
    public String genString() {
        String str = String.valueOf(this.id)+ "," + this.name ;
        return str;
    }
    @Override
    public String genShortString() {
        return String.valueOf(this.id);
    }
    
}
