
package com.kayhut.fuse.datagen.storyoficeandfire.entities;

import com.github.javafaker.Faker;
import com.kayhut.fuse.datagen.utilities.GenerateRandom;

/**
 *
 * @author smuel
 */
public class Horse extends OntologyEntity{
    
    
    public String color ;
    public int weight ;
    public boolean isFree ;
   
    
    public Horse(String name, int idx) {
        super(name,idx);
        Faker faker = new Faker() ;
        this.color = faker.color().name();
        this.weight = GenerateRandom.genRandomInt(200, 500) ;
        this.isFree = true;
     }
    
    

    @Override
    public String genString() {
        String str = String.valueOf(this.id) + "," + this.name + "," + this.color + "," +  String.valueOf(this.weight);
        return str;
    }
    
    public String genShortString() {
        return String.valueOf(this.id)+","+this.name;
    }
    
    
    
}
