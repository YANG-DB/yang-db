package com.kayhut.fuse.model.query;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by User on 23/02/2017.
 */
public enum ConstraintOp {
    @JsonProperty("empty")
    empty,

    @JsonProperty("not empty")
    notEmpty,

    @JsonProperty("eq")
    eq,

    @JsonProperty("ne")
    ne,

    @JsonProperty("gt")
    gt,

    @JsonProperty("ge")
    ge,

    @JsonProperty("lt")
    lt,

    @JsonProperty("le")
    le,

    @JsonProperty("in set")
    inSet,

    @JsonProperty("not in set")
    notInSet,

    @JsonProperty("in range")
    inRange,

    @JsonProperty("not in range")
    notInRange,

    @JsonProperty("contains")
    contains,

    @JsonProperty("not contains")
    notContains,

    @JsonProperty("starts with")
    startsWith,

    @JsonProperty("not starts with")
    notStartsWith,

    @JsonProperty("ends with")
    endsWith,

    @JsonProperty("not ends with")
    notEndsWith,

    @JsonProperty("match")
    match,

    @JsonProperty("not match")
    notMatch,

    @JsonProperty("fuzzy eq")
    fuzzyEq,

    @JsonProperty("fuzzy ne")
    fuzzyNe,

    @JsonProperty("wildcard")
    wildcard
}
