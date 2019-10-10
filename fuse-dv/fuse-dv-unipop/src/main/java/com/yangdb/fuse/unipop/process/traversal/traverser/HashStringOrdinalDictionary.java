package com.yangdb.fuse.unipop.process.traversal.traverser;

/*-
 *
 * fuse-dv-unipop
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

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Roman on 1/27/2018.
 */
public class HashStringOrdinalDictionary implements StringOrdinalDictionary {
    //region Constructors
    public HashStringOrdinalDictionary() {
        this.maxOrdinal = 0;
        this.stringToOrdinal = new HashMap<>();
        this.ordinalToString = new HashMap<>();
    }
    //endregion

    //region StringOrdinalDictionary Implementation
    @Override
    public String getString(byte ordinal) {
        return ordinal <= 0 ? null : this.ordinalToString.get(ordinal);
    }

    @Override
    public byte getOrdinal(String string) {
        Byte ordinal = this.stringToOrdinal.get(string);
        return ordinal == null ? (byte)0 : ordinal;
    }

    @Override
    public byte getOrCreateOrdinal(String string) {
        Byte ordinal = this.stringToOrdinal.get(string);
        if (ordinal != null && ordinal > 0) {
            return ordinal;
        }

        byte newStringOrdinal = ++this.maxOrdinal;
        this.stringToOrdinal.put(string, newStringOrdinal);
        this.ordinalToString.put(newStringOrdinal, string);

        return newStringOrdinal;
    }
    //endregion

    //region Fields
    private Map<String, Byte> stringToOrdinal;
    private Map<Byte, String> ordinalToString;

    private byte maxOrdinal;
    //endregion
}
