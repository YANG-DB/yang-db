
package com.yangdb.fuse.datagen.storyoficeandfire.entities;

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

import com.github.javafaker.Faker;
import com.yangdb.fuse.datagen.utilities.GenerateRandom;

/**
 *
 * @author smuel
 */
public class Horse extends OntologyEntity{
    
    
    public String color ;
    public int weight ;
    public boolean isFree ;
   
    
    public Horse(String name, int idx) {
        super(name,idx);
        Faker faker = new Faker() ;
        this.color = faker.color().name();
        this.weight = GenerateRandom.genRandomInt(200, 500) ;
        this.isFree = true;
     }
    
    

    @Override
    public String genString() {
        String str = String.valueOf(this.id) + "," + this.name + "," + this.color + "," +  String.valueOf(this.weight);
        return str;
    }
    
    public String genShortString() {
        return String.valueOf(this.id)+","+this.name;
    }
    
    
    
}
