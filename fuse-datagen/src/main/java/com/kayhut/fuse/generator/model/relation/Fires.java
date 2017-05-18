package com.kayhut.fuse.generator.model.relation;

import com.kayhut.fuse.generator.model.enums.RelationType;

import java.util.Date;

/**
 * Created by benishue on 15-May-17.
 */
public class Fires extends RelationBase {

    //region Ctrs
    public Fires() {
    }

    public Fires(String id, String source, String target, Date date) {
        super(id, source, target, RelationType.FIRES);
        this.date = date;
    }
    //endregion

    //region Getters & Setters
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
    //endregion

    //region Public Methods
    @Override
    public String[] getRecord(){
        return new String[] { this.getId(), this.getSource(), this.getTarget(), this.getDate().getTime() + "" };
    }
    //endregion

    //region Fields
    private Date date;
    //endregion
}
