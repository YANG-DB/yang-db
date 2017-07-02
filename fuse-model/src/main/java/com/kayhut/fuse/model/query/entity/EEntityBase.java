package com.kayhut.fuse.model.query.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kayhut.fuse.model.Below;
import com.kayhut.fuse.model.Next;
import com.kayhut.fuse.model.query.EBase;
import javaslang.collection.Stream;

import java.util.Collections;
import java.util.List;

/**
 * Created by User on 27/02/2017.
 */
public abstract class EEntityBase extends EBase implements Next<Integer>, Below<Integer> {
    //region Constructors
    public EEntityBase() {
        this.reportProps = Collections.emptyList();
    }

    public EEntityBase(int eNum, String eTag, int next, int b) {
        this(eNum, eTag, Collections.emptyList(), next, b);
    }

    public EEntityBase(int eNum, String eTag, List<String> reportProps, int next, int b) {
        super(eNum);

        this.eTag = eTag;
        this.next = next;
        this.b = b;

        this.reportProps = reportProps != null ?
                Stream.ofAll(reportProps).toJavaList() :
                Collections.emptyList();
    }
    //endregion

    //region Override Methods
    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) return false;

        EEntityBase that = (EEntityBase) o;
        if (eTag == null) {
            if (that.eTag != null)
                return false;
        } else {
            if (!eTag.equals(that.eTag)) return false;
        }
        if (next != that.next) return false;
        if (b != that.b) return false;

        if ((reportProps == null && that.reportProps != null) ||
                (reportProps != null && that.reportProps == null)) {
            return false;
        }

        if (reportProps != null) {
            return reportProps.equals(that.reportProps);
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();

        result = 31 * result + next;
        result = 31 * result + b;

        result = 31 * result + (eTag != null ? eTag.hashCode() : 0);
        result = 31 * result + (reportProps!=null ? reportProps.hashCode() : 0);
        return result;
    }
    //endregion

    //region Properties
    public String geteTag() {
        return eTag;
    }

    public void seteTag(String eTag) {
        this.eTag = eTag;
    }

    public Integer getNext() {
        return next;
    }

    public void setNext(Integer next) {
        this.next = next;
    }

    public Integer getB() {
        return b;
    }

    public void setB(Integer b) {
        this.b = b;
    }

    public List<String> getReportProps() {
        return reportProps;
    }

    public void setReportProps(List<String> reportProps) {
        this.reportProps = reportProps;
    }
    //endregion

    //region Fields
    private String eTag;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int next;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int b;

    private List<String> reportProps;
    //endregion
}
