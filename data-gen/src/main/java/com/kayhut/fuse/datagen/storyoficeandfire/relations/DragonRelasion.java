
package com.kayhut.fuse.datagen.storyoficeandfire.relations;

import com.kayhut.fuse.datagen.dateandtime.TimeFactory;
import com.kayhut.fuse.datagen.dateandtime.DateFactory;
import com.kayhut.fuse.datagen.storyoficeandfire.entities.Dragon;

/**
 *
 * @author smuel
 */


public class DragonRelasion {
    
    /**
     *
     */
    
    private Rel rel ;  
    public  boolean dragonFire ;
    
    public DragonRelasion() {
        this.rel = null;
    }
    
    
    public void setRelasion(Dragon dragona, Dragon dragonb, DateFactory df ,int duration , boolean dragonSpitFire) {
        this.rel = new Rel(dragona,dragonb,duration,df) ;
        this.dragonFire = dragonSpitFire ;
        
    } 
    
    public String getRelasion() { 
        String str = this.rel.dragonA.genString() ;
        if (dragonFire) {
            str += "," + this.rel.dragonB.genString() +  "," +  this.rel.playDate.toString() + " " +this.rel.time ;
        }else {
            str += "," + this.rel.dragonB.genString() + "," + this.rel.playDate.toString() + " " + this.rel.time + "," + 
                String.valueOf(this.rel.duration) + " Minuts" ; 
        }
        return str ;
    }
   
            
}


class Rel {
    
    public Dragon dragonA ;
    public Dragon dragonB ;
    public int duration ;
    public String time ;
    public DateFactory playDate ;
    
    public Rel(Dragon dragona , Dragon dragonb, int duration,DateFactory df ) {
        this.dragonA = new Dragon(dragona) ;
        this.dragonB = new Dragon(dragonb);
        this.duration = duration ;
        this.time = TimeFactory.getTime() ;
        playDate = new DateFactory(df.gc) ;
    }
    
    
    
}

