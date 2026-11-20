
package com.yangdb.fuse.datagen.utilities;

/*-
 * #%L
 * fuse-domain-dragons-datagen
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
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



import com.yangdb.fuse.datagen.storyoficeandfire.entities.*;
import com.yangdb.fuse.datagen.storyoficeandfire.relations.DragonRelasion;
import com.yangdb.fuse.datagen.storyoficeandfire.relations.KingdomRelasion;
import com.yangdb.fuse.datagen.storyoficeandfire.relations.PeopleRelasion;
import com.yangdb.fuse.datagen.storyoficeandfire.relations.RelasionList;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


/**
 *
 * @author shmuel mashiach
 */
public class Print2CSVFile {
    
    
    public static void deleteExistingFile(String filePath) {
        try {
            File file = new File(filePath) ;
            if (file.exists()) 
                file.delete() ;
        }catch (Exception e) {
            e.printStackTrace();
        }
    } 
    
    public static void printPeople2File(FileWriter writer, ListOfEntities<People> people) throws IOException {
        
        String NEW_LINE_SEPERATOR = "\n";
        people.li = people.entities.listIterator() ;
        writer.write(NEW_LINE_SEPERATOR) ;
        
        while (people.li.hasNext()) {
    
            writer.write(people.li.next().genString());
            writer.write(NEW_LINE_SEPERATOR) ;
        }
        
    }
    
    public static void printHorses2File(FileWriter writer, ListOfEntities<Horse> horses) throws IOException { 
        
        String NEW_LINE_SEPERATOR = "\n";
        horses.li = horses.entities.listIterator() ;
        writer.write(NEW_LINE_SEPERATOR) ;
        
        while (horses.li.hasNext()) {
            writer.write(horses.li.next().genString());
            writer.write(NEW_LINE_SEPERATOR) ;
        }
    }
    
    public static void printDragons2File(FileWriter writer, ListOfEntities<Dragon> dragons) throws IOException { 
        
        String NEW_LINE_SEPERATOR = "\n";
        dragons.li = dragons.entities.listIterator() ;
        writer.write(NEW_LINE_SEPERATOR) ;
        
        while (dragons.li.hasNext()) {
            writer.write(dragons.li.next().genString());
            writer.write(NEW_LINE_SEPERATOR) ;
        }
    }
    
    public static void printGuild2File(FileWriter writer, ListOfEntities<Guild> guilds) throws IOException { 
        
        String NEW_LINE_SEPERATOR = "\n";
        guilds.li = guilds.entities.listIterator() ;
        writer.write(NEW_LINE_SEPERATOR) ;
        
        while(guilds.li.hasNext()) {
            writer.write(guilds.li.next().genString()) ;
            writer.write(NEW_LINE_SEPERATOR) ;
        }
    }
    
    public static void printKingdoms2File(FileWriter writer, ListOfEntities<Kingdom> kingdoms) throws IOException { 
        
        String NEW_LINE_SEPERATOR = "\n";
        kingdoms.li = kingdoms.entities.listIterator() ;
        writer.write(NEW_LINE_SEPERATOR) ;
        
        while(kingdoms.li.hasNext()) {
            writer.write(kingdoms.li.next().genString());
            writer.write(NEW_LINE_SEPERATOR) ;
        }
    }
    
    public static void ownsHorses2file(String filePath,  PeopleRelasion pr  ) {
        
             
        String NEW_LINE_SEPERATOR = "\n";
        String str = String.valueOf(pr.person.id) + "," + pr.person.firstName + " " + pr.person.lastName  ;
        
        // 1. person -owns-> horse  
        int listSize = pr.getHorsesListSize() ;
            
        try (FileWriter writer = new FileWriter(filePath,true)) {                   
            for(int idx=0 ; idx<listSize ; idx++) {
                writer.write(NEW_LINE_SEPERATOR) ;
                writer.write(str+ "," + pr.getOwnsHorse(idx,true));
                    
            }
            writer.flush();
            writer.close();
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
    }
    public static void ownsDragons2file(String filePath,  PeopleRelasion pr  ) {
        String NEW_LINE_SEPERATOR = "\n";
        String str = String.valueOf(pr.person.id) + "," + pr.person.firstName + " " + pr.person.lastName  ;
        // 2 person -owns -> deragon  
        int listSize = pr.getDragonsListSize() ;    
        try (FileWriter writer = new FileWriter(filePath,true)) {
                
            for(int idx=0 ; idx<listSize ; idx++) {
                writer.write(NEW_LINE_SEPERATOR) ;
                writer.write(str+ "," +pr.getOwnsDragon(idx));
                    
            }
            writer.flush();
            writer.close();
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
    }
    public static void memberOfGuild2File(String filePath,  PeopleRelasion pr  ) {
        String NEW_LINE_SEPERATOR = "\n";
        String str = String.valueOf(pr.person.id) + "," + pr.person.firstName + " " + pr.person.lastName  ; 
        // 3. person -- member of -> guild
        int listSize = pr.getGuildMembershipSize() ;
           
        try (FileWriter writer = new FileWriter(filePath,true)) {        
            for(int idx=0 ; idx<listSize ; idx++) {
                writer.write(NEW_LINE_SEPERATOR) ;
                writer.write(str+ "," +pr.getMembership(idx));    
            }
            writer.flush();
            writer.close();
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
    }
    public static void knowsPeson2File(String filePath,  PeopleRelasion pr ) {
        String NEW_LINE_SEPERATOR = "\n";
        String str = String.valueOf(pr.person.id) + "," + pr.person.firstName + " " + pr.person.lastName  ; 
        // 4. person -knows-> person 
        int listSize = pr.getKnowsPepoleSize() ;
            
        try (FileWriter writer = new FileWriter(filePath,true)) {
               
            for(int idx=0 ; idx<listSize ; idx++) {
                writer.write(NEW_LINE_SEPERATOR);
                writer.write(str+ "," +pr.getKnows(idx));
                    
            }
            writer.flush();
            writer.close();
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
    }
    public static void offspring2File(String filePath,  PeopleRelasion pr ) {
        String NEW_LINE_SEPERATOR = "\n";
        String str = String.valueOf(pr.person.id) + "," + pr.person.firstName + " " + pr.person.lastName  ; 
        // 5. person -offspring-> person 
        int listSize = pr.getOffspringsSize() ;
            
        try (FileWriter writer = new FileWriter(filePath,true)) {
                
            for(int idx=0 ; idx<listSize ; idx++) {
                writer.write(NEW_LINE_SEPERATOR) ;
                writer.write(pr.getOffspring(idx) + "," + str) ;    
            }
            writer.flush();
            writer.close();
        }catch (IOException e){
            System.out.println(e.getMessage());
        }   
    }
    
    public static void printHorseOriginatedIn2File(String filePath, KingdomRelasion kr  ) {
        
        String str = kr.kingdom.genString() ;
        String NEW_LINE_SEPERATOR = "\n";
        // horse -originated in -> kingdom
        int listSize = kr.getHorseOriginSize() ;
             
        try (FileWriter writer = new FileWriter(filePath,true)) {
                
            for(int idx=0 ; idx<listSize ; idx++) {
                writer.write(NEW_LINE_SEPERATOR) ;
                writer.write(kr.getHorseOriginRelation(idx) + "," + str);
            }
            writer.flush();
            writer.close();
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
    }
    public static void printDragonOriginatedIn2File(String filePath, KingdomRelasion kr ) {
        
        String str = kr.kingdom.genString() ;
        String NEW_LINE_SEPERATOR = "\n";
        // dragon -originated in -> kingdom
        int listSize = kr.getDragonOriginSize() ;
            
        try (FileWriter writer = new FileWriter(filePath,true)) {
                
            for(int idx=0 ; idx<listSize ; idx++) {
                writer.write(NEW_LINE_SEPERATOR);
                writer.write(kr.getDragonOriginRelation(idx) + "," + str);    
            }
            writer.flush();
            writer.close();
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
    }
    public static void printPeopleSubjectOf2File(String filePath, KingdomRelasion kr ) {
        String str = kr.kingdom.genString() ;
        String NEW_LINE_SEPERATOR = "\n";
        // person -subject of -> kingdom
        int listSize = kr.getsubjectOfSize() ;
         
        try (FileWriter writer = new FileWriter(filePath,true)) {        
            for(int idx=0 ; idx<listSize ; idx++) {
                writer.write(NEW_LINE_SEPERATOR) ;
                writer.write(kr.getSubjectOfRelation(idx) + "," + str);
                    
            }
            writer.flush();
            writer.close();
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
    }
    public static void printGuildRegisterIn2File(String filePath, KingdomRelasion kr ) {
        String str = kr.kingdom.genString() ;
        String NEW_LINE_SEPERATOR = "\n";
        // guild -registerd in -> kingdom
        int listSize = kr.getRegisteredInSize() ;
                
        try (FileWriter writer = new FileWriter(filePath,true)) {
            for(int idx=0 ; idx<listSize ; idx++) {
                writer.write(NEW_LINE_SEPERATOR);
                writer.write(kr.getRegisteredInRelation(idx) + "," + str);    
            }
            writer.flush();
            writer.close();
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
    }
    
    public static void printDragonFiresAt2File(String filePath, RelasionList<DragonRelasion> drRelList  ) {
        
        String NEW_LINE_SEPERATOR = "\n";
               
        try (FileWriter writer = new FileWriter(filePath,true)) {
            for (int idx =0 ; idx < drRelList.rList.size() ; idx++) {
                writer.write(NEW_LINE_SEPERATOR) ;
                writer.write(drRelList.getRelasion(idx).getRelasion()) ;
            }
            writer.flush();
            writer.close();
                
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
    }
            
    public static void printDragonFreezes2File(String filePath, RelasionList<DragonRelasion> drRelList  ) {    
            
        String NEW_LINE_SEPERATOR = "\n";    
            
        try (FileWriter writer = new FileWriter(filePath,true)) {
            for (int idx =0 ; idx < drRelList.rList.size() ; idx++) {
                writer.write(NEW_LINE_SEPERATOR) ;
                writer.write(drRelList.getRelasion(idx).getRelasion()) ;
            }
            writer.flush();
            writer.close();
                
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
    }
    
    
}
