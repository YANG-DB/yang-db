package com.kayhut.fuse.generator.model.relation;

import com.kayhut.fuse.generator.model.enums.RelationType;

/**
 * Created by benishue on 15-May-17.
 */
public abstract class RelationBase {

    //region Ctrs
    public RelationBase() {
    }

    public RelationBase(String id, String source, String target, RelationType relationType) {
        this.id = id;
        this.source = source;
        this.target = target;
        this.relationType = relationType;
    }
    //endregion

    //region Getters & Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public RelationType getRelationType() {
        return relationType;
    }

    public void setRelationType(RelationType relationType) {
        this.relationType = relationType;
    }

    //endregion

    //region Abstract Methods
    public abstract String[] getRecord();
    //endregion

    //region Fields
    private String id;
    private String source;
    private String target;
    private RelationType relationType;
    //endregion
}
