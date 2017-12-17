package com.kayhut.fuse.model.validation;

import javaslang.collection.Stream;

import java.util.Arrays;
import java.util.StringJoiner;

/**
 * Created by liorp on 5/29/2017.
 */
public class QueryValidation {
    public static QueryValidation OK = new QueryValidation(true);

    public static String print(Object ... elements) {
        StringJoiner joiner = new StringJoiner(":","[","]");
        Arrays.asList(elements).forEach(element -> joiner.add(element.toString()));
        return joiner.toString();
    }

    //region Constructors
    public QueryValidation(boolean valid, String ... errors) {
        this(valid, Stream.of(errors));
    }

    public QueryValidation(boolean valid, Iterable<String> errors) {
        this.valid = valid;
        this.errors = Stream.ofAll(errors).toJavaList();
    }
    //endregion

    //region Public Methods
    public boolean valid() {
        return valid;
    }

    public Iterable<String> errors() {
        return errors;
    }
    //endregion

    //region Override Methods
    @Override
    public String toString() {
        if(valid())
            return "valid";
        return print(errors);
    }
    //endregion

    //region Fields
    private boolean valid;
    private Iterable<String> errors;
    //endregion
}
