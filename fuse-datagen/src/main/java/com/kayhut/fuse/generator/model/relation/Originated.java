package com.kayhut.fuse.generator.model.relation;

import com.kayhut.fuse.generator.model.enums.RelationType;

import java.util.Date;

/**
 * Created by benishue on 05-Jun-17.
 */
public class Originated extends RelationBase {

    //region Ctrs
    public Originated(String id, String source, String target, Date since) {
        super(id, source, target, RelationType.ORIGINATED);
        this.since = since;
    }
    //endregion

    //region Getters & Setters
    public Date getSince() {
        return since;
    }

    public void setSince(Date since) {
        this.since = since;
    }
    //endregion

    //region Public Methods
    @Override
    public String[] getRecord() {
        return new String[]{
                this.getId(),
                this.getSource(),
                this.getTarget(),
                Long.toString(this.getSince().getTime())
        };
    }
    //endregion

    //region Fields
    private Date since;
    //endregion
}

