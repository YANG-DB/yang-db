package com.kayhut.fuse.unipop.process.traversal.traverser;

import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ByteOpenHashMap;

/**
 * Created by Roman on 1/27/2018.
 */
public class HashStringOrdinalDictionary implements StringOrdinalDictionary {
    //region Constructors
    public HashStringOrdinalDictionary() {
        this.maxOrdinal = 0;
        this.stringToOrdinal = new Object2ByteOpenHashMap<>();
        this.stringToOrdinal.defaultReturnValue((byte)0);
        this.ordinalToString = new Byte2ObjectOpenHashMap<>();
    }
    //endregion

    //region StringOrdinalDictionary Implementation
    @Override
    public String getString(byte ordinal) {
        return ordinal <= 0 ? null : this.ordinalToString.get(ordinal);
    }

    @Override
    public byte getOrdinal(String string) {
        return this.stringToOrdinal.getByte(string);
    }

    @Override
    public byte getOrCreateOrdinal(String string) {
        byte ordinal = this.stringToOrdinal.getByte(string);
        if (ordinal > 0) {
            return ordinal;
        }

        byte newStringOrdinal = ++this.maxOrdinal;
        this.stringToOrdinal.put(string, newStringOrdinal);
        this.ordinalToString.put(newStringOrdinal, string);

        return newStringOrdinal;
    }
    //endregion

    //region Fields
    private Object2ByteOpenHashMap<String> stringToOrdinal;
    private Byte2ObjectOpenHashMap<String> ordinalToString;

    private byte maxOrdinal;
    //endregion
}
