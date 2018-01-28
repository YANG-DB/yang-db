package com.kayhut.fuse.unipop.process.traversal.traverser;

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
