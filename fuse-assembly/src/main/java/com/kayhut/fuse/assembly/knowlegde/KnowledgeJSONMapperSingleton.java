package com.kayhut.fuse.assembly.knowlegde;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Created by rani on 5/17/2018.
 */
public class KnowledgeJSONMapperSingleton {
    private static KnowledgeJSONMapperSingleton myObj;

    private SimpleDateFormat sdf;
    private ObjectMapper _mapper;

    /**
     * Create private constructor
     */
    private KnowledgeJSONMapperSingleton(){
        _mapper = new ObjectMapper();
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        _mapper.setDateFormat(sdf);
    }

    /**
     * Create a static method to get instance.
     */
    public static KnowledgeJSONMapperSingleton getInstance(){
        if(myObj == null){
            myObj = new KnowledgeJSONMapperSingleton();
        }
        return myObj;
    }

    public ObjectMapper getMapper() {
        return _mapper;
    }
}
