package com.kayhut.fuse.assembly.knowledge.domain;

import com.kayhut.fuse.assembly.knowledge.KnowledgeRawSchemaShort;
import com.kayhut.fuse.executor.ontology.schema.RawSchema;

/**
 * Created by user pc on 5/12/2018.
 */
public class KnowledgeRawSchemaSingleton {

    public static final String cIndexType = "pge";
    private static KnowledgeRawSchemaSingleton myObj;
    private KnowledgeRawSchemaShort _schema;
    /**
     * Create private constructor
     */
    private KnowledgeRawSchemaSingleton(){
        _schema = new KnowledgeRawSchemaShort();
    }
    /**
     * Create a static method to get instance.
     */
    public static KnowledgeRawSchemaSingleton getInstance(){
        if(myObj == null){
            myObj = new KnowledgeRawSchemaSingleton();
        }
        return myObj;
    }

    public RawSchema getSchema(){
        return _schema;
    }
}
