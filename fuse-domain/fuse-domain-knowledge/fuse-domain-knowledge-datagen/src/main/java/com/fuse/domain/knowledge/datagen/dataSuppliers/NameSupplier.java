package com.fuse.domain.knowledge.datagen.dataSuppliers;

/*-
 * #%L
 * fuse-domain-knowledge-datagen
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

import javaslang.collection.Stream;

import java.util.List;
import java.util.Set;

/**
 * Created by Roman on 6/22/2018.
 */
public class NameSupplier extends RandomDataSupplier<String> {
    //region Constructors
    public NameSupplier(int minLength, int maxLength) {
        this(minLength, maxLength, 0);
    }

    public NameSupplier(int minLength, int maxLength, long seed) {
        super(seed);
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.lengthDiff = this.maxLength - this.minLength;
    }
    //endregion

    //region RandomDataSupplier Implementation
    @Override
    public String get() {
        int nameLength = this.minLength + this.random.nextInt(lengthDiff);

        StringBuilder sb = new StringBuilder();
        List<Character> charList = this.consonants;
        while(sb.length() < nameLength) {
            sb.append(charList.get(this.random.nextInt(charList.size())));
            charList = charList == this.consonants ? this.vowels : this.consonants;
        }

        return sb.toString();
    }
    //endregion

    //region Fields
    private int maxLength;
    private int minLength;
    private int lengthDiff;

    private List<Character> consonants = Stream.of('b', 'c', 'd', 'f', 'g', 'h', 'j', 'k', 'l' , 'm', 'n', 'p', 'q', 'r', 's', 't', 'v', 'w', 'x', 'z').toJavaList();
    private List<Character> vowels = Stream.of('a', 'e', 'i', 'o', 'u', 'y').toJavaList();
    //endregion
}
