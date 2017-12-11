package com.kayhut.fuse.dispatcher.utils;

import java.util.Arrays;
import java.util.StringJoiner;

/**
 * Created by liorp on 5/29/2017.
 */
public class ValidationContext {
    public static ValidationContext OK = new ValidationContext(true);

    private boolean valid;
    private String[] errors;

    public ValidationContext(boolean valid, String ... errors) {
        this.valid = valid;
        this.errors = errors;
    }

    public boolean valid() {
        return valid;
    }

    public String[] errors() {
        return errors;
    }

    @Override
    public String toString() {
        if(valid())
            return "valid";
        return print(errors);
    }

    public static String print(Object ... elements) {
        StringJoiner joiner = new StringJoiner(":","[","]");
        Arrays.asList(elements).forEach(element -> joiner.add(element.toString()));
        return joiner.toString();
    }
}
