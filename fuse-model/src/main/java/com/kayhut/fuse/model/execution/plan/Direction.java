package com.kayhut.fuse.model.execution.plan;

/**
 * Created by User on 22/02/2017.
 */
public enum Direction {
    in,
    out,
    both;

    public Direction reverse() {
        if(this==both)
            return both;
        return in==this ? out : in;
    }
}
