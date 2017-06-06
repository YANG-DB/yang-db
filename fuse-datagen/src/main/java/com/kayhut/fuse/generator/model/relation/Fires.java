package com.kayhut.fuse.generator.model.relation;

import com.kayhut.fuse.generator.model.enums.RelationType;

import java.util.Date;

/**
 * Created by benishue on 15-May-17.
 */
public class Fires extends RelationBase {

    //region Ctrs

    public Fires(String id, String source, String target, Date date, int temperature) {
        super(id, source, target, RelationType.FIRES);
        this.date = date;
        this.temperature = temperature;
    }
    //endregion

    //region Getters & Setters
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    //endregion

    //region Public Methods
    @Override
    public String[] getRecord() {
        return new String[]{this.getId(),
                this.getSource(),
                this.getTarget(),
                Long.toString(this.getDate().getTime()),
                Integer.toString(this.temperature)};
    }
    //endregion

    //region Fields
    private Date date;
    private int temperature;
    //endregion
}
