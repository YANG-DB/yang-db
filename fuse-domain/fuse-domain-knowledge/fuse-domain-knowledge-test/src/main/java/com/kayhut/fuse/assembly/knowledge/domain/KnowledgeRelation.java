package com.kayhut.fuse.assembly.knowledge.domain;

/**
 * Created by user pc on 5/11/2018.
 */
public class KnowledgeRelation {
    private String _id;
    private KnowledgeEntity _aEntity, _bEntity;
    private Relation _rel;

    public void setId(String value) {
        _id = value;
    }

    public String getId() {
        return _id;
    }

    public void setEntity(KnowledgeEntity entity, boolean isA) {
        if (isA) {
            _aEntity = entity;
        } else {
            _bEntity = entity;
        }
    }

    public Relation getRelation() {
        if (_rel == null)
            _rel = new Relation();

        return _rel;
    }
}
