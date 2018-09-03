package com.kayhut.fuse.assembly.knowledge.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by user pc on 5/12/2018.
 */
public class KnowledgeInsight {
    private String _id;
    private Insight _insight;
    private List<KnowledgeEntity> _entitiesList;
    private List<KnowledgeReference> _refsList;

    public void setInsight(Insight i) {
        _insight = i;
    }

    public void setId(int i) {
        _id = "i" + String.format(KnowledgeRawSchemaSingleton.getInstance().getSchema().getIdFormat("insight"), i);
    }

    public String getId() {
        return _id;
    }

    public void addEntity(KnowledgeEntity e) {
        if (_entitiesList == null) {
            _entitiesList = new ArrayList<KnowledgeEntity>();
        }

        _entitiesList.add(e);
    }

    public void addRef(KnowledgeReference r) {
        if (_refsList == null) {
            _refsList = new ArrayList<KnowledgeReference>();
        }

        _refsList.add(r);
    }
}
