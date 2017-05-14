package com.kayhut.fuse.stat.model.configuration.histogram;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Created by benishue on 30-Apr-17.
 */

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "histogramType")
@JsonSubTypes({
        @JsonSubTypes.Type(name = "numeric", value = HistogramNumeric.class),
        @JsonSubTypes.Type(name = "string", value = HistogramString.class),
        @JsonSubTypes.Type(name = "manual", value = HistogramManual.class),
        @JsonSubTypes.Type(name = "composite", value = HistogramComposite.class)
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class Histogram {

    //region Ctrs
    public Histogram() {
    }

    public Histogram(HistogramType histogramType) {
        this.histogramType = histogramType;
    }

    public HistogramType getHistogramType() {
        return histogramType;
    }
    //endregion

    //region Fields
    private HistogramType histogramType;
    //endregion
}
