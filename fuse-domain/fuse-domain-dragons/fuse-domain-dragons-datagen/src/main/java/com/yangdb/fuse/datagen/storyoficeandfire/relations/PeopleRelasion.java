
package com.yangdb.fuse.datagen.storyoficeandfire.relations;

/*-
 *
 * fuse-domain-gragons-datagen
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
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
 *
 */

import com.yangdb.fuse.datagen.dateandtime.DateFactory;
import com.yangdb.fuse.datagen.storyoficeandfire.entities.Dragon;
import com.yangdb.fuse.datagen.storyoficeandfire.entities.Guild;
import com.yangdb.fuse.datagen.storyoficeandfire.entities.Horse;
import com.yangdb.fuse.datagen.storyoficeandfire.entities.People;

import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author smuel
 * 
 */
public class PeopleRelasion {
    
    public People person ;
    public List<RelationEntity<Horse>> ownHorses;
    public List<RelationEntity<Dragon>> ownDragons ;
    public List<RelationEntity<Guild>> memberOf ;
    public List<RelationEntity<People>> knows ;
    public List<RelationEntity<People>> offsprings ;
    
    /**
     *
     * @param p
     */
    public PeopleRelasion(People p) {
        
        this.person = p ;
        this.ownHorses = new ArrayList<RelationEntity<Horse>>() ;
        this.ownDragons = new ArrayList<RelationEntity<Dragon>>() ;
        this.memberOf = new ArrayList<RelationEntity<Guild>>() ;
        this.knows = new ArrayList<RelationEntity<People>>() ;
        this.offsprings = new ArrayList<RelationEntity<People>>() ;
    }
    
    public void setPepole(People p) {
        this.person = p ;
    }
    
    public void setNewHorse(Horse horse, DateFactory since) {
        RelationEntity<Horse> obj = new RelationEntity<Horse>(horse,since) ;
        obj.setTill(person.deathDate);
        this.ownHorses.add(obj) ;
        
    }
    
    public void setNewDragon(Dragon dragon, DateFactory since) {
        RelationEntity<Dragon> obj = new RelationEntity<Dragon>(dragon,since) ;
        obj.setTill(person.deathDate);
        this.ownDragons.add(obj);
    }
    
    public void setNewMembership(Guild guild , DateFactory since) {
        RelationEntity<Guild> obj = new RelationEntity<Guild>(guild, since) ;
        obj.setTill(person.deathDate);
        this.memberOf.add(obj) ;
    }
    
    public void setNewKnows(People friend, DateFactory since) {
        RelationEntity<People> obj = new RelationEntity<People>(friend,since) ;
        obj.setTill(person.deathDate);
        this.knows.add(obj) ;
    }
    
    public void setNewOffspring(People offspring) {
       
        DateFactory since = offspring.birthDate ;
        RelationEntity<People> obj = new RelationEntity<People>(offspring,since) ;
        obj.setTill(offspring.deathDate);
        this.offsprings.add(obj) ;
    }
    
    public RelationEntity<Horse> getHorsefromRelation(int idx) {
        return ownHorses.get(idx);
    }  
    
    public RelationEntity<Dragon> getDragonfromRelation(int idx) {
        return ownDragons.get(idx) ;
    }
    
    public void setHorseIntoRelation(RelationEntity<Horse> obj) {
        this.ownHorses.add(obj) ;
    }
    
    public void setDragonIntoRelation(RelationEntity<Dragon> obj) {
        this.ownDragons.add(obj) ;
    }
    
    public String getOwnsHorse(int idx, boolean isShort) {
        String str = isShort ? this.ownHorses.get(idx).obj.genShortString() :this.ownHorses.get(idx).obj.genString() ;
        str += "," + this.ownHorses.get(idx).since.toString() + "," + this.ownHorses.get(idx).till.toString() ;
        return str ;
    }
    
    public String getOwnsDragon(int idx) {
        String str = this.ownDragons.get(idx).obj.genString() ;
        str += "," + this.ownDragons.get(idx).since.toString() + "," + this.ownDragons.get(idx).till.toString() ;
        return str;
    }
    
    public String getMembership(int idx) {
        String str = this.memberOf.get(idx).obj.genString() ;
        str += "," + this.memberOf.get(idx).since.toString() + ","+  this.memberOf.get(idx).till.toString() ;
        return str;
    }
    
    public String getKnows(int idx) {
        String str = this.knows.get(idx).obj.genShortString() ;
        str+="," +  this.knows.get(idx).since.toString() ;
        return str ;
    }
    
    public String getOffspring(int idx) {
        return this.offsprings.get(idx).obj.genShortString() ;
    }
    
    
    public int getHorsesListSize() {
        return this.ownHorses.size() ;
    }
    
    public int getDragonsListSize() {
        return this.ownDragons.size() ;
    }
    
    public int getGuildMembershipSize() {
        return this.memberOf.size() ;
    }
    
    public int getKnowsPepoleSize() {
        return this.knows.size() ;
    }
    
    public int getOffspringsSize() {
        return this.offsprings.size() ;
    }
      
}

 
