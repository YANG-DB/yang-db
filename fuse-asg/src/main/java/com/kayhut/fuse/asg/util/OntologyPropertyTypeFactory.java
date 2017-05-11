package com.kayhut.fuse.asg.util;

import com.kayhut.fuse.model.ontology.Property;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class OntologyPropertyTypeFactory {

    //region Constructor
    public OntologyPropertyTypeFactory() {
        this.map = new HashMap<>() ;
        this.map.put("string", (exp) -> String.valueOf(exp));
        this.map.put("int", (exp) -> (new Long((Integer) exp)).longValue());
        this.map.put("float", (exp) ->  ((Double) exp).doubleValue());
        this.map.put("double", (exp) ->  ((Double) exp).doubleValue());
        this.map.put("date", (exp) -> ((exp instanceof Long || exp instanceof Integer) ? new Date(Long.parseLong(exp.toString())) : exp)); //Supporting both conversion from Int & Long To Date
    }
    //endregion

    //region Public Methods
    public Object supply(Property prop, Object exp) {
        return this.map.get(prop.getType()).apply(exp);
    }
    //endregion

    //region Fields
    private Map<String, Function<Object, Object>> map;
    //endregion
}
