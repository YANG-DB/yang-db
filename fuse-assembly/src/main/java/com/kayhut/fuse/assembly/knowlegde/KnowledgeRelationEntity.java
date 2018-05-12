package com.kayhut.fuse.assembly.knowlegde;

/**
 * Created by user pc on 5/12/2018.
 */
public class KnowledgeRelationEntity {
    private RelationEntity _relEntity;
    private Relation _relaton;
    private Entity _aEntity, _bEntity;

    public void setRelation(Relation r) {
        _relaton = r;
    }

    public void setRelationEntities(Entity a, Entity b) {
        _aEntity = a;
        _bEntity = b;
    }
}
