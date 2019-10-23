
package com.yangdb.fuse.datagen.storyoficeandfire.entities;

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



import com.github.javafaker.Faker;
import com.github.javafaker.Name;
import com.yangdb.fuse.datagen.dateandtime.DateFactory;
import com.yangdb.fuse.datagen.utilities.GenerateRandom;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

/**
 *
 * @author shmuel mashiach
 */
public class People {
    
    public enum Gender {
        MALE, FEMALE
    }
    
    public int id ;
    public String firstName ;
    public String lastName ;
    public Gender gender ;
    public DateFactory birthDate ;
    public DateFactory deathDate ;
    public int height ;
    public boolean isOldestOffspring ;
    public boolean isAlive ;
    public int parentId ;
    public int kingdom_id ;
    public List<Integer> offspringsIdList ;
    
    public People(int idx , JSONObject jsonObject,DateFactory bDate) {
        
        
        this.id = idx ;
        //
        this.offspringsIdList = new ArrayList<Integer>() ; ;
        this.isOldestOffspring = false ;
        this.parentId = -1 ;
        this.kingdom_id = -1 ;
        this.isAlive = true ;
        //
        Faker faker = new Faker() ;
        Name name = faker.name() ;
        this.firstName = name.firstName() ;
        this.lastName = name.lastName() ;
        this.height =  GenerateRandom.genRandomInt((int)((long) jsonObject.get("MIN_HEIGHT_4_PERSON")), (int)((long) jsonObject.get("MAX_HEIGHT_4_PERSON")));
        this.gender = GenerateRandom.testWithProb((int)((long) jsonObject.get("MALE_PROBABILITY") )) ? Gender.MALE : Gender.FEMALE ;  
        // set date of birth
        this.birthDate = new DateFactory(bDate.gc) ;
        // Genrate date of death
        this.deathDate = new DateFactory(this.birthDate.gc) ;      
        
        
        
    }
    
    public People(People p) {
        this.id = p.id ;
        //
        this.offspringsIdList = p.offspringsIdList ;
        this.isOldestOffspring = p.isOldestOffspring ;
        this.isAlive = p.isAlive ;
        this.parentId = p.parentId ;
        this.kingdom_id = p.kingdom_id ;
        //
        this.birthDate = p.birthDate ;
        this.deathDate = p.deathDate ;
        this.height = p.height ;
        this.firstName = p.firstName;
        this.lastName = p.lastName ;
        this.gender = p.gender ;
    }
    
    public void setDeathDate(boolean initStage,JSONObject jsonObject) {
        // add number of days with normal distrobution with Mean of 50 years and standard deviation of 15 years
        int lifeExpectancy = (int)((long) jsonObject.get("LIFE_EXPECTANCY")) ;
        if (initStage) 
            this.deathDate.addDays(GenerateRandom.genRandomInt((365*30), (365*50))) ;
        else 
            this.deathDate.addDays(GenerateRandom.genRandomWithNormalDistribution( (365*15) , (365*lifeExpectancy) ));
    }
    
    public int Age(DateFactory current) {
        int ret = current.gc.get(GregorianCalendar.YEAR) - this.birthDate.gc.get(GregorianCalendar.YEAR) ; 
        return ret ;
    } 
    
    public String genString() {
        String str = String.valueOf(this.id) + "," + this.firstName + " " + this.lastName + 
                "," + String.valueOf(this.gender) + "," + String.valueOf(this.height) +
                "," + this.birthDate.toString() + "," + this.deathDate.toString();
        return str  ;
    }
    
    public String genShortString() {
        String str = String.valueOf(this.id) + "," + this.firstName + " " + this.lastName ;
        return str ;
    }
    
}
