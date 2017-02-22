package com.kayhut.fuse.model.results;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by benishue on 21-Feb-17.
 */
public class Pattern {

    public String getOnt() {
        return ont;
    }

    public void setOnt(String ont) {
        this.ont = ont;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Object> getElements() {
        return elements;
    }

    public void setElements(List<Object> elements) {
        this.elements = elements;
    }

    @Override
    public String toString()
    {
        return "Pattern [ont = "+ont+", name = "+name+", elements = "+elements+"]";
    }


    //region Fields
    public String ont;
    public String name;
    @JsonProperty(required = true)
    public List<Object> elements;
    //endregion
}
