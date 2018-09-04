package com.kayhut.fuse.model.validation;

import javaslang.collection.Stream;

import java.util.Arrays;
import java.util.StringJoiner;

/**
 * Created by liorp on 5/29/2017.
 */
public class ValidationResult {
    public static ValidationResult OK = new ValidationResult(true, "none");

    public static String print(Object... elements) {
        StringJoiner joiner = new StringJoiner(":", "[", "]");
        Arrays.asList(elements).forEach(element -> joiner.add(element.toString()));
        return joiner.toString();
    }

    //region Constructors
    public ValidationResult(boolean valid, String validator, String... errors) {
        this(valid, validator, Stream.of(errors));
    }

    public ValidationResult(boolean valid, String validator, Iterable<String> errors) {
        this.valid = valid;
        this.validator = validator;
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
        if (valid())
            return "valid";
        return print(errors + ":" + validator);
    }

    public String getValidator() {
        return validator;
    }
//endregion

    //region Fields
    private final String validator;
    private boolean valid;
    private Iterable<String> errors;
    //endregion
}
