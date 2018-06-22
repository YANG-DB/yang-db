package com.kayhut.fuse.generator.knowledge.dataSuppliers;

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
