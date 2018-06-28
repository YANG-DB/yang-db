
package com.kayhut.fuse.datagen.storyoficeandfire.entities;

/**
 *
 * @author smuel
 */
public class Guild extends OntologyEntity{
    
    public Guild(String name , int idx) {
        super(name,idx) ;
    }

    @Override
    public String genString() {
        String str = String.valueOf(this.id) + "," + this.name ;
        return str ;
    }
    
    @Override
    public String genShortString() {
        return String.valueOf(this.id);
    }
}
