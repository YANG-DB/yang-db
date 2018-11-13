
package com.kayhut.fuse.datagen.storyoficeandfire.relations;

/*-
 * #%L
 * fuse-domain-gragons-datagen
 * %%
 * Copyright (C) 2016 - 2018 kayhut
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

/**
 *
 * @author smuel
 * @param <T>
 */
public class RelationEntity <T>{
   
    
    public T obj ;
    public DateFactory since ;
    public DateFactory till ;
    
    public RelationEntity( T obj ) {
        this.obj = obj ;
    }
    
    public RelationEntity( T obj , DateFactory since) {
        
        this.obj = obj ;
        this.since = new DateFactory(since.gc) ;
        
    }
    
    public void setSince (DateFactory since) {
        this.since = new DateFactory(since.gc) ;
    }
    
    public void setTill(DateFactory till) {
        
        this.till = new DateFactory(till.gc) ;
    }
}
   
