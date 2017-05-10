package com.kayhut.fuse.asg.util;

import com.kayhut.fuse.model.ontology.Property;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class OntologyPropertyTypeFactory {

    //region Constructor
    public OntologyPropertyTypeFactory() {
        this.map = new HashMap<>() ;
        this.map.put("string", (exp) -> String.valueOf(exp));
        this.map.put("int", (exp) -> ((Integer) exp).intValue());
        this.map.put("float", (exp) ->  ((Double) exp).doubleValue());
        this.map.put("double", (exp) ->  ((Double) exp).doubleValue());
        this.map.put("date", (exp) -> new Date((Long) exp));
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
