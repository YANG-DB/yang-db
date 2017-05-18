
package com.kayhut.fuse.datagen.storyoficeandfire;

import com.kayhut.fuse.datagen.dateandtime.DateFactory;
import com.kayhut.fuse.datagen.storyoficeandfire.entities.*;
import com.kayhut.fuse.datagen.utilities.GenerateRandom;
import org.json.simple.JSONObject;


/**
 *
 * @author smuel
 */
public class InitEntities {
    
    public static ListOfEntities<People> initPeopleList(String PATH , JSONObject jsonObject  ) {
        
        DateFactory birthYear = new DateFactory() ;
        ListOfEntities<People> peopleList = new ListOfEntities<People>() ;
        //
        int peoplePopulation = (int)((long) jsonObject.get("INIT_PEOPLE_POPULATION")) ;
        for (int idx=0 ; idx < peoplePopulation; idx++) {
            int rnd = GenerateRandom.genRandomInt(1, 3650) ;
            birthYear.setDateFactory(471, rnd);
            People p = new People(idx,jsonObject,birthYear);
            p.setDeathDate(true, jsonObject);
            peopleList.entities.add(p);
        }
        
        return peopleList ;
    }
    
    public static ListOfEntities<Horse> initHorseList(String PATH , JSONObject jsonObject) {
        
        ListOfEntities<Horse> horseList = new ListOfEntities<Horse>() ;
        //
        int horsesPopulation = (int)((long) jsonObject.get("TOTAL_NUMBER_OF_HORSES")) ;
        for (int idx=0 ; idx < horsesPopulation; idx++) {
            horseList.setList(new Horse("Horse",idx));
        }
        
        return horseList;
    }
    
     
    public static ListOfEntities<Dragon> initDragonList(String PATH , JSONObject jsonObject) {
        
        ListOfEntities<Dragon> dragonList = new ListOfEntities<Dragon>() ;
        //
        int dargonsPopulation = (int)((long) jsonObject.get("TOTAL_NUMBER_OF_DRAGONS")) ;
        for (int idx=0 ; idx < dargonsPopulation; idx++) {
            dragonList.setList(new Dragon("dragon",idx));
        }
        
        return dragonList;
    }
    
    public static ListOfEntities<Kingdom>initKingdomList(String PATH , JSONObject jsonObject)  {
        
        ListOfEntities<Kingdom> kingdomList = new ListOfEntities<Kingdom>() ;
        //
        int kingdoms = (int)((long) jsonObject.get("NUMBER_OF_KINGDOMS")) ;
        for (int idx=0 ; idx < kingdoms ; idx++) {
            kingdomList.setList(new Kingdom("Kingdom", idx));
        }
        
        return kingdomList;
    }
    
    public static ListOfEntities<Guild> initGuildList(String PATH , JSONObject jsonObject) {
        
        ListOfEntities<Guild> guildList = new ListOfEntities<Guild>() ;
        int guilds = (int)((long) jsonObject.get("NUMBER_OF_GUILD")) ;
        for (int idx=0 ; idx < guilds ; idx++) {
            guildList.setList(new Guild("Guild", idx));
        }
       
        return guildList ;
    }
    
}
