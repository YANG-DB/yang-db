package com.kayhut.fuse.assembly.knowledge.parser.model;

import com.kayhut.fuse.model.query.Rel;

public enum Direction {
    in(Rel.Direction.L), out(Rel.Direction.R), both(Rel.Direction.RL);


    private Rel.Direction direction;

    Direction(Rel.Direction direction) {
        this.direction = direction;
    }

    public Rel.Direction getDirection() {
        return direction;
    }
}
