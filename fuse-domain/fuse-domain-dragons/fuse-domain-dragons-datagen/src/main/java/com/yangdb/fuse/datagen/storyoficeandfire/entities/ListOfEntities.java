
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


 
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 *
 * @author smuel
 * @param <T>
 
 */
public class ListOfEntities <T> {
   
    public List<T> entities ;
    public ListIterator<T> li ;
    
    public ListOfEntities() {
        this.entities = new ArrayList<T>();
    }
    
    public void setList(T entity) {
        this.entities.add(entity);
    }
    
    public int getListSize() {
        return this.entities.size() ;
    }
    
    public T getEntity(int idx) {
        return entities.get(idx) ;
    } 
    
}
