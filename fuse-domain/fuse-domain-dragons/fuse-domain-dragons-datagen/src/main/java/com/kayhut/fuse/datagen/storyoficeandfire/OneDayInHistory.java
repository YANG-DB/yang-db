
package com.kayhut.fuse.datagen.storyoficeandfire;

import com.kayhut.fuse.datagen.dateandtime.DateFactory;
import com.kayhut.fuse.datagen.dateandtime.TimeFactory;
import com.kayhut.fuse.datagen.storyoficeandfire.entities.*;
import com.kayhut.fuse.datagen.storyoficeandfire.relations.*;
import com.kayhut.fuse.datagen.utilities.GenerateRandom;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 *
 * @author shmuel mashiach
 */
public class OneDayInHistory {
    
    public static List<Integer> generateDragonsPlayersId(ListOfEntities<Dragon> ld,JSONObject jsonObject) {
        
        List<Integer> listOfPlayingDragons = new ArrayList<Integer>();
        int coupleOfDragons = (int)((long) jsonObject.get("COUPLE_OF_DRAGONS_THAT_PLAY_EACH_DAY")) ;
        int listSize = ld.getListSize() ;
        int cntr = 0 ;
        
        while (cntr < (2*coupleOfDragons) ) {
            int rnd = GenerateRandom.genRandomInt(0, listSize-1) ;
            if (!ld.entities.get(rnd).isFree) {
                listOfPlayingDragons.add(ld.entities.get(rnd).id) ;
                cntr+=1 ;
            }
        }
        
        return listOfPlayingDragons;
    } 
    
    public static void dragonsPalying(ListOfEntities<Dragon> ld, RelasionList<DragonRelasion> dFireRelList,
                                                              RelasionList<DragonRelasion> dFreezesRelList ,
                                                              List<Integer> listOfPlayingDragons,JSONObject jsonObject , DateFactory df) {
        
        int spitFire  = (int)((long) jsonObject.get("CHANCE_OF_DRAGON_SPIT_FIRE")) ;
        
        // run of pair of dragons
        TimeFactory.initTime();
        int timeInterval = GenerateRandom.genRandomInt(420, 1000) ;
        TimeFactory.setTime(timeInterval);
        // save time
        int time = TimeFactory.hour*60 +  TimeFactory.minuts ;
        for (int idx = 0 ; idx < (int)(listOfPlayingDragons.size()/2) ; idx++ ) {
            
            int ida = listOfPlayingDragons.get(2*idx) ;
            int idb = listOfPlayingDragons.get(2*idx+1) ;
            // set spit fire or ice
            boolean dragonSpitFire = Math.random() > ((double)spitFire/100) ; 
            // set duration of spit
            int duration = GenerateRandom.genRandomInt(1, 120) ; // from minute up to 2 hours
            //  set and push new relation
            DragonRelasion dr = new DragonRelasion() ;
            dr.setRelasion(ld.getEntity(ida), ld.getEntity(idb), df, duration, dragonSpitFire);
            if (dragonSpitFire)  
                dFireRelList.setList(dr); 
            else 
                dFreezesRelList.setList(dr) ;                               
            
            // reset dragons play time...
            TimeFactory.initTime();
            timeInterval = GenerateRandom.genRandomInt(420, 1000) ;
            TimeFactory.setTime(timeInterval);
                    
        }
        // set time 
        TimeFactory.initTime();
        TimeFactory.setTime(time) ;
        
    }
    
    public static RelasionList<PeopleRelasion> findDeadPepole(ListOfEntities<People> lp ,RelasionList<PeopleRelasion> prList, DateFactory df) {
        // Go over population and foound out if the death date is today  
        //
        
        for (int idx=0 ; idx < lp.getListSize() ; idx++) {
            People p = lp.getEntity(idx) ;
            if (df.isSameDate(p.deathDate.gc)) {
                // person is dead
                p.isAlive = false ;
                System.out.println("Person ID "+ String.valueOf(p.id) + " Is Dead");
                // check if person is parent
                if (p.offspringsIdList.size() > 0) {
                    prList = inheriteProperty(p,prList) ; 
                }else {
                    prList = releseProperty(p,prList) ;
                }
            }
        }
        return prList ;
    }
    
    public static void check4NewOffsprings(ListOfEntities<People> lp ,RelasionList<PeopleRelasion> prList , RelasionList<KingdomRelasion> kngList ,
                                     ListOfEntities<Guild> gl , ListOfEntities<Horse> hl , ListOfEntities<Dragon> dl ,
                                     DateFactory df, JSONObject jsonObject, int populationStartOfY) {
        
        int population = lp.entities.size() ;
        int maxNumOfOffsprings = (int)((long) jsonObject.get("MAX_NUMBER_OF_OFFSPRINGS")) ;
        int minAge4HavingOffsprings = (int)((long) jsonObject.get("MIN_AGE_4_HAVING_OFFSPRINGS")) ;
        int watchDog ;
        int parent_id  ;
        // position the list iterator only on people that were born this year .
        ListIterator<People> li = lp.entities.listIterator(populationStartOfY) ;
        while (li.hasNext()) {
            People baby = li.next() ;
            if (df.isSameDate(baby.birthDate.gc)) {
                watchDog = (int)((long) jsonObject.get("WATCH_DOG_COUNTER")) ;
                // new baby is born today 
                // 1. match a parent which is alive and is kingdom
                boolean match = false ;
                while(!match && watchDog>0) {
                    parent_id = GenerateRandom.genRandomInt(0, population-1) ;
                    People parent = lp.getEntity(parent_id) ;
                    // if parent is not dead , not the baby itself & is number of offsprings < maxNumOfOffsprings we have a match 
                    if ((parent_id != baby.id) && (parent.isAlive) && 
                            (parent.offspringsIdList.size() < maxNumOfOffsprings) && 
                             parent.Age(df) >= minAge4HavingOffsprings) {
                        match = true ;
                        // set parent and baby and set a new offspring relation
                        baby.parentId = parent_id ;
                        parent.offspringsIdList.add(baby.id) ;
                        if( parent.offspringsIdList.size() == 1)
                            baby.isOldestOffspring = true ;
                        // set baby and parent relation 
                        prList.rList.get(baby.id).setPepole(baby);
                        prList.rList.get(parent_id).setPepole(lp.getEntity(parent_id));
                        // set offspring relation :
                        prList.rList.get(parent_id).setNewOffspring(baby);
                        // set subject of kingdom relation :
                        kngList.rList.get(lp.getEntity(parent_id).kingdom_id).setSubjectOf(baby);                     
                    }
                    watchDog-=1 ;
                }
                if (watchDog == 0) 
                    throw new IllegalArgumentException("Baby Is Orphan !!!");
                
            } // if 
        } // while   
    }
    
    private static RelasionList<PeopleRelasion> inheriteProperty(People dead , RelasionList<PeopleRelasion> prList) {
        
        // find dead person offsprings and pass his horses & dragon to the oldest
        int oldestId = prList.rList.get(dead.id).person.offspringsIdList.get(0) ;
        
        if (prList.rList.get(oldestId).person.isOldestOffspring) {
            System.out.println("Offspring ID "+ String.valueOf(prList.rList.get(oldestId).person.id) + " Inherit horses and dragons");
            // this is the oldest offspring that inharit all dragons and horses
            int hsize = prList.rList.get(dead.id).getHorsesListSize() ;
            int dsize = prList.rList.get(dead.id).getDragonsListSize() ;
            for (int idx = 0 ; idx < hsize ; idx++) {
                
                RelationEntity<Horse> ownHorse = new RelationEntity<Horse>(prList.rList.get(dead.id).ownHorses.get(idx).obj) ;
                ownHorse.setSince(prList.rList.get(dead.id).person.deathDate);
                ownHorse.setTill(prList.rList.get(oldestId).person.deathDate);
                prList.rList.get(oldestId).ownHorses.add(ownHorse);
            }
            for (int idx = 0 ; idx < dsize ; idx++) {
                RelationEntity<Dragon> ownDragon = new RelationEntity<Dragon>(prList.rList.get(dead.id).ownDragons.get(idx).obj) ;
                ownDragon.setSince(prList.rList.get(dead.id).person.deathDate);
                ownDragon.setTill(prList.rList.get(oldestId).person.deathDate);
                prList.rList.get(oldestId).ownDragons.add(ownDragon);
            }
        } else {
            throw new IllegalArgumentException("Offspring should be oldest.");
        }
        return prList ;
    }
    
    private static RelasionList<PeopleRelasion> releseProperty(People dead, RelasionList<PeopleRelasion> prList) {
        
        // the dead person has no offsprings so all dragons and horses are relesed 
        System.out.println("No offsprings, free all horses and dragons") ;
        int hSize = prList.rList.get(dead.id).getHorsesListSize();
        int dSize = prList.rList.get(dead.id).getDragonsListSize() ;
        for (int idx = 0 ; idx < hSize ; idx++) {           
            prList.rList.get(dead.id).getHorsefromRelation(idx).obj.isFree = true ;
        }
        for (int idx = 0 ; idx < dSize ; idx++) {
            prList.rList.get(dead.id).getDragonfromRelation(idx).obj.isFree = true ;
        }
        
        return prList ;
    }

    
}


