package com.kayhut.fuse.assembly.knowledge.domain;

/**
 * Created by user pc on 5/12/2018.
 */
public class KnowledgeRelationValue {
    private String _id;
    private KnowledgeRelation _relation;
    private RelationValue _relationValue;

    public RelationValue getRelationValue() {
        return _relationValue;
    }

    public void setRelation(KnowledgeRelation r) {
        _relation = r;
    }

    public void setId(String id) {_id = id;}
}
