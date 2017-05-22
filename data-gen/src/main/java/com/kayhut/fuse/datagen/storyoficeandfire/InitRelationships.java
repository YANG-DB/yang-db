
package com.kayhut.fuse.datagen.storyoficeandfire;

import com.kayhut.fuse.datagen.dateandtime.DateFactory;
import com.kayhut.fuse.datagen.storyoficeandfire.entities.*;
import com.kayhut.fuse.datagen.storyoficeandfire.relations.KingdomRelasion;
import com.kayhut.fuse.datagen.storyoficeandfire.relations.PeopleRelasion;
import com.kayhut.fuse.datagen.storyoficeandfire.relations.RelasionList;
import com.kayhut.fuse.datagen.utilities.GenerateRandom;
import org.json.simple.JSONObject;


/**
 *
 * @author smuel
 */
public class InitRelationships {
    
    
    public static RelasionList<PeopleRelasion> initPepoleRelation(JSONObject jsonObject, ListOfEntities<People> peopleList, 
                                      ListOfEntities<Horse> horseList, ListOfEntities<Dragon> dragonList ,
                                      ListOfEntities<Guild> guildList,DateFactory df, int startIdx ,int relListSize,RelasionList<PeopleRelasion> ownersList) {
        
        int max_horses = (int)((long) jsonObject.get("MAX_HORSES_PER_ENTITY")) ;
        int max_dragons = (int)((long) jsonObject.get("MAX_DRAGONS_PER_ENTITY")) ;
        int numberOFguild = (int)((long) jsonObject.get("NUMBER_OF_GUILD")) ;
        int maxNumberOfKnowens = (int)((long) jsonObject.get("MAX_NUMBER_OF_PEOPLE_PERSON_KNOWS")) ;
        
        //
        for (int idx = startIdx ; idx < relListSize; idx++) {
            // 1. select person node from people list.
            PeopleRelasion owner = new PeopleRelasion(peopleList.getEntity(idx)) ;
            // 2. create number of person --> horse relationships ...
            int iter_h = GenerateRandom.genRandomWithDiffDelta(max_horses,10 ,0.05) ;
            int iter_d = GenerateRandom.genRandomWithDiffDelta(max_dragons, 10 , 0.05) ;
            int iter_g = GenerateRandom.genRandomWithDiffDelta(numberOFguild, 5 , 0.05) ;
            int iter_k = GenerateRandom.genRandomInt(0, maxNumberOfKnowens) ;
            
            if (iter_h > 0) {
                for (int i = 0 ; i < iter_h ; i++) {
                    // Select horse from horseList check if he is free
                    // if free : add to owner horse list and change horse status.
                    int idx_h =  GenerateRandom.genRandomInt(0, horseList.getListSize()-1) ;
                    Horse horse = horseList.entities.get(idx_h) ;
                    if (horse.isFree) {
                        // create relationships : horse is owned by new owner
                        horse.isFree = false;
                        owner.setNewHorse(horse, df);
                        
                    }
                }
            }
            // 3. create number of person --> dragon relationships ...
            if (iter_d > 0) {
                for (int i = 0 ; i < iter_d ; i++) {
                    // Select dragon from dragonList check if he is free
                    // if free : add to owner dragon list and change dragon status.
                    int idx_d =  GenerateRandom.genRandomInt(0, dragonList.getListSize()-1) ;
                    Dragon dragon = dragonList.entities.get(idx_d);
                    if (dragon.isFree) {
                        // create relationships : dragon is owned by new owner
                        dragon.isFree = false;
                        owner.setNewDragon(dragon, df);
                    }
                        
                }
                
            }
            if (iter_g > 0) {
                for (int i = 0 ; i < iter_g ; i++) {
                    int idx_g = GenerateRandom.genRandomInt(0, guildList.getListSize()-1) ;
                    owner.setNewMembership(guildList.entities.get(idx_g), df);
                }
            }
            if (iter_k > 0) {
                for (int i = 0; i < iter_k ; i++) {
                    int idx_k = GenerateRandom.genRandomInt(0,peopleList.getListSize()-1) ;
                    if (idx_k != idx) { // person does not knows himself
                         owner.setNewKnows(peopleList.entities.get(idx_k), df);
                    }
                }
            }
            
            
            ownersList.setList(owner);
        }
        
        return ownersList ;
    }
    
    public static RelasionList<KingdomRelasion> initKingdomRelations(JSONObject jsonObject,ListOfEntities<People> peopleList,
                                        ListOfEntities<Horse> horseList, ListOfEntities<Dragon> dragonList,
                                        ListOfEntities<Guild> guildList, ListOfEntities<Kingdom> kingdomList) {
        
        RelasionList<KingdomRelasion> kingdomRelasionList = new RelasionList<KingdomRelasion>() ;
        int numberOfKingdoms = (int)((long) jsonObject.get("NUMBER_OF_KINGDOMS")) ;
        int numberOfGuild = (int)((long) jsonObject.get("NUMBER_OF_GUILD")) ;
        int numberOfPeople = (int)((long) jsonObject.get("INIT_PEOPLE_POPULATION")) ;
        int numberOfDragons = (int)((long) jsonObject.get("TOTAL_NUMBER_OF_DRAGONS")) ;
        int numberOfHorses = (int)((long) jsonObject.get("TOTAL_NUMBER_OF_HORSES")) ;
        
        // 1. set kingdoms for all relasions
        for (int idx = 0 ; idx < numberOfKingdoms ; idx++) {
            KingdomRelasion kr = new KingdomRelasion(kingdomList.getEntity(idx));
            kingdomRelasionList.setList(kr);
        }
        
        // 2. start building all relasions 
        // RegiteredIn(Guild, Kingdom )
        for (int idx = 0 ; idx < numberOfGuild; idx++) {
            // for each guild choose random kingdom and set relasion : RegiteredIn 
            int rnd = GenerateRandom.genRandomInt(0, numberOfKingdoms-1) ;
            kingdomRelasionList.getRelasion(rnd).setRegisteredIn(guildList.getEntity(idx));
        }
        //SubjectOf(Pepole, Kingdom)
        for (int idx = 0 ; idx < numberOfPeople ; idx++) {
            int rnd = GenerateRandom.genRandomInt(0, numberOfKingdoms-1) ;
            kingdomRelasionList.getRelasion(rnd).setSubjectOf(peopleList.getEntity(idx));
        }
        //OriginatedIn(Horse,Kingdom)
        for (int idx = 0 ; idx < numberOfHorses ; idx++) {
            int rnd = GenerateRandom.genRandomInt(0, numberOfKingdoms-1) ;
            kingdomRelasionList.getRelasion(rnd).setHorseOrigin(horseList.getEntity(idx));
        }
        //OriginatedIn(Dragon,Kingdom)
        for (int idx = 0 ; idx < numberOfDragons ; idx++) {
            int rnd = GenerateRandom.genRandomInt(0, numberOfKingdoms-1) ;
            kingdomRelasionList.getRelasion(rnd).setDragonOrigin(dragonList.getEntity(idx));
        }
        return kingdomRelasionList ;
    }
    
    
}
