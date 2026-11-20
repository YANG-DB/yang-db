
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



/**
 *
 * @author smuel
 */
public class Guild extends OntologyEntity{
    
    public Guild(String name , int idx) {
        super(name,idx) ;
    }

    @Override
    public String genString() {
        String str = String.valueOf(this.id) + "," + this.name ;
        return str ;
    }
    
    @Override
    public String genShortString() {
        return String.valueOf(this.id);
    }
}
