
package com.kayhut.fuse.datagen.storyoficeandfire.relations;

/*-
 * #%L
 * fuse-domain-gragons-datagen
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.kayhut.fuse.datagen.dateandtime.DateFactory;
import com.kayhut.fuse.datagen.dateandtime.TimeFactory;
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

