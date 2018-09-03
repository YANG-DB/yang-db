package com.kayhut.fuse.datagen.storyoficeandfire;

import com.kayhut.fuse.datagen.dateandtime.DateFactory;
import com.kayhut.fuse.datagen.storyoficeandfire.entities.*;
import com.kayhut.fuse.datagen.storyoficeandfire.relations.DragonRelasion;
import com.kayhut.fuse.datagen.storyoficeandfire.relations.KingdomRelasion;
import com.kayhut.fuse.datagen.storyoficeandfire.relations.PeopleRelasion;
import com.kayhut.fuse.datagen.storyoficeandfire.relations.RelasionList;
import com.kayhut.fuse.datagen.utilities.GenerateRandom;
import com.kayhut.fuse.datagen.utilities.Print2CSVFile;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.GregorianCalendar;
import java.util.List;


/**
 *
 * @author shmuel mashiach
 */
public class StoryOfIceAndFire {

    public static final String PARAMETERS_JSON = "parameters.json";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws URISyntaxException, IOException, ParseException {
        // parse json file 
        JSONObject jsonObject = null ;
        JSONParser parser = new JSONParser();
        if(args.length ==1) {
                Object obj = parser.parse(new FileReader(args[0]));
                jsonObject = (JSONObject) obj;
        } else {
            Path path = Paths.get(ClassLoader.getSystemResource(PARAMETERS_JSON).toURI());
            Object obj = parser.parse(new FileReader(path.toString()));
            jsonObject = (JSONObject) obj;
        }

        String PATH = (String) jsonObject.get("PATH");
        System.out.println("Writing to path["+PATH+"]");

        System.out.println("INIT PEOPLE LIST !!!");
        // 1. generate list of pepole
        
        ListOfEntities<People> peopleList =  InitEntities.initPeopleList(PATH,jsonObject);
        
        System.out.println("INIT HORSES LIST !!!");
        // 2. generate list of horses and dragons 
        ListOfEntities<Horse> horseList =  InitEntities.initHorseList(PATH,jsonObject) ;
        //
        System.out.println("INIT DRAGONS LIST !!!");
        //
        ListOfEntities<Dragon> dragonList = InitEntities.initDragonList(PATH,jsonObject) ;
        //
        System.out.println("INIT KINGDOM LIST !!!");
        ListOfEntities<Kingdom> kingdomList = InitEntities.initKingdomList(PATH,jsonObject) ;
        //
        System.out.println("INIT GUILD LIST !!!");
        ListOfEntities<Guild> guildList = InitEntities.initGuildList(PATH,jsonObject) ;
        //
        // 3. Init relation PeopleRelasion for people ----> horses & people ----> dragons
        
        DateFactory storyDate = new DateFactory() ;
        storyDate.setDateFactory(500, 0);
        //
        System.out.println("INIT RELATIONS IN : " + storyDate.toString());
        // set new owners list
        RelasionList<PeopleRelasion> prList = new RelasionList<PeopleRelasion>() ;
        prList = InitRelationships.initPepoleRelation(jsonObject, peopleList, horseList, dragonList, 
                                                     guildList,storyDate,0 ,peopleList.getListSize(),prList);
        //
             
        RelasionList<KingdomRelasion> krList = InitRelationships.initKingdomRelations(jsonObject,peopleList, horseList, dragonList,guildList,kingdomList) ; 
        RelasionList<DragonRelasion> dragonFireRelList = new RelasionList<DragonRelasion>() ;
        RelasionList<DragonRelasion> dragonFreezeRelList = new RelasionList<DragonRelasion>() ;
        delDragonRelasionFiles(PATH) ;
        // The point when Story begins .... 1/1/500
        System.out.println("THE BEGINING OF THE STORY...");  
        int iter = 1 ; //(int)((long) jsonObject.get("MAX_NUMBER_OF_SPITS_IN_DRAGON_PLAY")) ;
        
        int numberOfYears = (int)((long) jsonObject.get("NUMBER_OF_STORY_YEARS")) ;
        for (int year = 0 ; year < numberOfYears ; year++) {
            // First day in each year ...
            if (storyDate.gc.get(GregorianCalendar.DAY_OF_MONTH) == 31)
                storyDate.addDays(1); 
            System.out.println("DATE : "+storyDate.toString());
            //
            
            int populationAtStartOfYear = peopleList.getListSize() ;
            // create  dragons for this year and update relasion list
            
            dragonList = setDragonPopulation(jsonObject,dragonList) ;
            // create horses for this year
            horseList = setHorsePopulation(jsonObject,horseList) ;
            // create new pepole for the current story year
            peopleList = setPopulationO1YearHistory(jsonObject,storyDate,peopleList) ;
            // create new relation for each new person 
            prList = InitRelationships.initPepoleRelation(jsonObject, peopleList, horseList, dragonList,
                                                          guildList,storyDate,populationAtStartOfYear ,
                                                          peopleList.getListSize() ,prList);
            
                   
            for (int day = 0 ; day < 365 ; day++) {
                prList = OneDayInHistory.findDeadPepole(peopleList, prList, storyDate);
                OneDayInHistory.check4NewOffsprings(peopleList, prList, krList, guildList, horseList, dragonList, 
                                                    storyDate, jsonObject,populationAtStartOfYear);
                
                for (int idx = 0 ; idx < iter ; idx++) {
                    List<Integer> listOfPlayingDragons = OneDayInHistory.generateDragonsPlayersId(dragonList,jsonObject) ;
                    OneDayInHistory.dragonsPalying(dragonList, dragonFireRelList, dragonFreezeRelList ,listOfPlayingDragons, jsonObject, storyDate);
                }
                try{
                    Thread.sleep(10);
                }catch(InterruptedException ex) {
                    ex.getMessage() ;
                    Thread.currentThread().interrupt();
                }
                // new day
                storyDate.addDays(1);
            }
            
            printDragonRelasionList(dragonFireRelList,dragonFreezeRelList,PATH) ;
            dragonFireRelList.clearList();
            dragonFreezeRelList.clearList();
            
        }
        System.out.println("WRITE TO FILE !!!");
        printPeopleList(peopleList,PATH+"PeopleList.csv") ;
        printDragonList(dragonList,PATH+"dragonsList.csv") ;
        printHorseList(horseList,PATH+"HorsesList.csv") ;
        printGuildList(guildList,PATH+"GuildList.csv");
        printKingdomList(kingdomList,PATH+"KingdomList.csv");
        //
        printPeopleRelasion(prList,PATH) ;
        printKIngdomRelasion(krList,PATH) ;
    }
    
    public static ListOfEntities<Dragon> setDragonPopulation(JSONObject jsonObj , ListOfEntities<Dragon> dlist  ) {
        
        int dragonPopulation = dlist.getListSize() ;
        int populationGrowth = (int)((long) jsonObj.get("ANIMALS_GROWTH_PERCENTAGE")) ;
        int maxGrowth = (int)((long) jsonObj.get("MAX_GROWTH")) ;
        Double growth = ((double)populationGrowth/1000) *dragonPopulation ;
        int add2Population = Math.min(maxGrowth, growth.intValue()) ;
        for (int idx = dragonPopulation ; idx < (dragonPopulation+add2Population) ; idx++) {
            dlist.setList(new Dragon("Dragon",idx));
        }
        
        System.out.println("Dragon Population : " + String.valueOf(dlist.getListSize()) );
        return dlist ;
    }
    
    public static ListOfEntities<Horse> setHorsePopulation(JSONObject jsonObj, ListOfEntities<Horse> hlist) {
        
        int horsePopulation = hlist.getListSize() ;
        int populationGrowth = (int)((long) jsonObj.get("ANIMALS_GROWTH_PERCENTAGE")) ;
        int maxGrowth = (int)((long) jsonObj.get("MAX_GROWTH")) ;
        Double growth = ((double)populationGrowth/1000) * horsePopulation ;
        int add2Population = Math.min(maxGrowth, growth.intValue()) ;
        for (int idx = horsePopulation ; idx < (horsePopulation+add2Population) ; idx++) {
            hlist.setList(new Horse("Horse",idx));
        }
        System.out.println("Horse Population : " + String.valueOf(hlist.getListSize()) );
        return hlist ;
    }
    
    public static ListOfEntities<People> setPopulationO1YearHistory(JSONObject jsonObj , DateFactory df , ListOfEntities<People> peopleList) {
        
        int populationGrowth = (int)((long) jsonObj.get("POPULATION_GROWTH_PERCENTAGE")) ;
        int maxGrowth = (int)((long) jsonObj.get("MAX_GROWTH")) ;
        int population = peopleList.getListSize() ;
        Double growth = ((double)populationGrowth/1000) *population ;
        int add2Population =  Math.min(maxGrowth, growth.intValue()) ;
        // System.out.println("Population Growth : " + String.valueOf(populationGrowth)+ " Add : " + String.valueOf(add2Population));
        for (int idx = population ; idx < (population+add2Population) ; idx++) {
            int rnd = GenerateRandom.genRandomInt(1, 355) ; 
            DateFactory birthDay = new DateFactory(df.gc) ;
            birthDay.addDays(rnd);
            People born = new People(idx, jsonObj, birthDay) ;
            born.setDeathDate(false, jsonObj);
            peopleList.entities.add(born) ;
        }
        //System.out.println("Population : " + String.valueOf(peopleList.entities.size()));
        return peopleList ;
    }
    
    public static void printPeopleRelasion(RelasionList<PeopleRelasion> prList,String PATH) {
        
        
        // Print all PeopleRelasion relationships
        prList.li = prList.rList.listIterator() ;
        Print2CSVFile.deleteExistingFile(PATH+"PersonOwnsHorses.csv");
        Print2CSVFile.deleteExistingFile(PATH+"PersonOwnsDregons.csv");
        Print2CSVFile.deleteExistingFile(PATH+"PersonMemberOfGuild.csv");
        Print2CSVFile.deleteExistingFile(PATH+"PersonKnowsPerson.csv");
        Print2CSVFile.deleteExistingFile(PATH+"OffspringRelation.csv");
        while (prList.li.hasNext()) {
            PeopleRelasion pr = prList.li.next() ;
            Print2CSVFile.ownsHorses2file(PATH+"PersonOwnsHorses.csv", pr) ;
            Print2CSVFile.ownsDragons2file(PATH+"PersonOwnsDregons.csv", pr) ;
            Print2CSVFile.memberOfGuild2File(PATH+"PersonMemberOfGuild.csv", pr ) ;
            Print2CSVFile.knowsPeson2File(PATH+"PersonKnowsPerson.csv", pr ) ;
            Print2CSVFile.offspring2File(PATH+"OffspringRelation.csv", pr) ;
        }
    }
    
    public static void printKIngdomRelasion(RelasionList<KingdomRelasion> krList, String PATH) {
        
        krList.li = krList.rList.listIterator() ;
        Print2CSVFile.deleteExistingFile(PATH+"HorseOriginatedInKingdom.csv");
        Print2CSVFile.deleteExistingFile(PATH+"DragonOriginatedInKingdom.csv");
        Print2CSVFile.deleteExistingFile(PATH+"PeopleSubjectOfKingdom.csv");
        Print2CSVFile.deleteExistingFile(PATH+"GuildRegisterInKingdom.csv");
        while (krList.li.hasNext()) {
            KingdomRelasion kr = krList.li.next() ;
            Print2CSVFile.printHorseOriginatedIn2File(PATH+"HorseOriginatedInKingdom.csv" , kr);
            Print2CSVFile.printDragonOriginatedIn2File(PATH+"DragonOriginatedInKingdom.csv", kr);
            Print2CSVFile.printPeopleSubjectOf2File(PATH+"PeopleSubjectOfKingdom.csv", kr);
            Print2CSVFile.printGuildRegisterIn2File(PATH+"GuildRegisterInKingdom.csv", kr);
        }
        
    }
   
    public static void delDragonRelasionFiles(String PATH) {
        Print2CSVFile.deleteExistingFile(PATH+"DragonFiresAt.csv");
        Print2CSVFile.deleteExistingFile(PATH+"DragonFreezes.csv");
    }
    
    public static void printDragonRelasionList (RelasionList<DragonRelasion> dFireRelList , RelasionList<DragonRelasion> dFreezeRelList, String PATH) {
      
        Print2CSVFile.printDragonFiresAt2File(PATH+"DragonFiresAt.csv", dFireRelList );
        Print2CSVFile.printDragonFreezes2File(PATH+"DragonFreezes.csv", dFreezeRelList);
        
    }
    
    public static void printPeopleList( ListOfEntities<People> peopleList , String filePath) {
        
        try(FileWriter writer = new FileWriter(filePath,false)) {
            Print2CSVFile.printPeople2File(writer, peopleList);
            System.out.println("Writing ["+filePath+"] ");

        }catch (IOException e){
                System.out.println(e.getMessage());
        }
    }
    
    public static void printDragonList(ListOfEntities<Dragon> dragonList , String filePath) {
        
        try(FileWriter writer = new FileWriter(filePath,false)) {
            
            Print2CSVFile.printDragons2File(writer, dragonList);
            System.out.println("Writing ["+filePath+"] ");

        }catch (IOException e){
                System.out.println(e.getMessage());
        }
    }
    
    public static void printHorseList(ListOfEntities<Horse> horseList , String filePath) {
        
        try(FileWriter writer = new FileWriter(filePath,false)) {
            
            Print2CSVFile.printHorses2File(writer, horseList);
            System.out.println("Writing ["+filePath+"] ");

        }catch (IOException e){
                System.out.println(e.getMessage());
        }
    }
    
    public static void printGuildList(ListOfEntities<Guild> guildList , String filePath) {
        
        try(FileWriter writer = new FileWriter(filePath,false)) {
            
            Print2CSVFile.printGuild2File(writer, guildList);
            System.out.println("Writing ["+filePath+"] ");

        }catch (IOException e){
                System.out.println(e.getMessage());
        }
    }
    
    public static void printKingdomList(ListOfEntities<Kingdom> kingdomList , String filePath) {
        
        try(FileWriter writer = new FileWriter(filePath,false)) {
            
            Print2CSVFile.printKingdoms2File(writer, kingdomList);
            System.out.println("Writing ["+filePath+"] ");

        }catch (IOException e){
                System.out.println(e.getMessage());
        }
    }
    
}
