package com.kayhut.fuse.generator.model.relation;

import com.kayhut.fuse.generator.model.enums.RelationType;

import java.util.Date;

/**
 * Created by benishue on 05-Jun-17.
 */
public class MemberOf extends RelationBase {

    //region Ctrs
    public MemberOf(String id, String source, String target, Date since, Date till) {
        super(id, source, target, RelationType.MEMBER_OF);
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
    public String[] getRecord() {
        return new String[]{this.getId(),
                this.getSource(),
                this.getTarget(),
                Long.toString(this.getSince().getTime()),
                Long.toString(this.getTill().getTime())
        };
    }
    //endregion

    //region Fields
    private Date since;
    private Date till;
    //endregion
}

