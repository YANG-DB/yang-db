
package com.yangdb.fuse.datagen.storyoficeandfire;

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

import com.yangdb.fuse.datagen.dateandtime.DateFactory;
import com.yangdb.fuse.datagen.storyoficeandfire.entities.*;
import com.yangdb.fuse.datagen.utilities.GenerateRandom;
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
            dragonList.setList(new Dragon("Dragon",idx));
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
