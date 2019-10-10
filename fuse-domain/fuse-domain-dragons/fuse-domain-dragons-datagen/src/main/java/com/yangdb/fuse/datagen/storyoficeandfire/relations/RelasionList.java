
package com.yangdb.fuse.datagen.storyoficeandfire.relations;

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
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
/**
 *
 * @author smuel
 */
public class RelasionList<T> {
    public List<T> rList ;
    public ListIterator<T> li ;
    
    public RelasionList() {
        this.rList = new ArrayList<T>() ;
    }
    
    public void setList(T rlist) {
        this.rList.add(rlist) ;
        
    }
    
    public T getRelasion(int idx) {
        return this.rList.get(idx) ;
    }
    
    public void clearList() {
        rList.clear();
    }
}
