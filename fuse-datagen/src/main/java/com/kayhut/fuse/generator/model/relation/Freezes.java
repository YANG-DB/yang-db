package com.kayhut.fuse.generator.model.relation;

import com.kayhut.fuse.generator.model.enums.RelationType;

import java.util.Date;

/**
 * Created by benishue on 15-May-17.
 */
public class Freezes extends RelationBase {

    //region Ctrs
    public Freezes() {

    }

    public Freezes(String id, String source, String target, Date since, Date till) {
        super(id, source, target, RelationType.FREEZES);
        this.since = since;
        this.till = till;
    }
    //endregion

    //region Getters & Setters
    public Date getSince() {
        return since;
    }

    public void setSince(Date since) {
        this.since = since;
    }

    public Date getTill() {
        return till;
    }

    public void setTill(Date till) {
        this.till = till;
    }

    //endregion

    //region Public Methods
    @Override
    public String[] getRecord(){
        return new String[] { this.getId(), this.getSource(), this.getTarget(), this.getSince().getTime() + "" , this.getTill().getTime() + "" };
    }
    //endregion

    //region Fields
    private Date since;
    private Date till;
    //endregion
}
