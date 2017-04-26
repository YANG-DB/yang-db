package com.kayhut.fuse.model.execution.plan;

import com.kayhut.fuse.model.query.Rel;

/**
 * Created by User on 22/02/2017.
 */
public enum Direction {
    in,
    out,
    both;

    public Direction reverse() {
        if (this == both)
            return both;
        return in == this ? out : in;
    }

    public static Direction of(Rel.Direction dir) {
        switch (dir) {
            case L:
                return in;
            case R:
                return out;
            case RL:
                return both;
        }
        return both;
    }

    public Rel.Direction to() {
        switch (this) {
            case both:
                return Rel.Direction.RL;
            case in:
                return Rel.Direction.L;
            case out:
                return Rel.Direction.R;

        }
        return Rel.Direction.RL;
    }
}
