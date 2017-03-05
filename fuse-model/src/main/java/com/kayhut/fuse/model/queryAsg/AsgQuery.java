package com.kayhut.fuse.model.queryAsg;

import com.kayhut.fuse.model.query.Start;

import java.util.List;

/**
 * Created by benishue on 23-Feb-17.
 */
public class AsgQuery {

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

    public EBaseAsg<Start> getStart() {
        return start;
    }

    public void setStart(EBaseAsg<Start> start) {
        this.start = start;
    }

    public static final class AsgQueryBuilder {
        //region Fields
        private String ont;
        private String name;
        private EBaseAsg start;

        private AsgQueryBuilder() {
        }

        public static AsgQueryBuilder anAsgQuery() {
            return new AsgQueryBuilder();
        }

        public AsgQueryBuilder withOnt(String ont) {
            this.ont = ont;
            return this;
        }

        public AsgQueryBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public AsgQueryBuilder withStart(EBaseAsg<Start> start) {
            this.start = start;
            return this;
        }

        public AsgQuery build() {
            AsgQuery asgQuery = new AsgQuery();
            asgQuery.setOnt(ont);
            asgQuery.setName(name);
            asgQuery.setStart(start);
            return asgQuery;
        }
    }

    //region Fields
    private String ont;
    private String name;
    private EBaseAsg<Start> start;
    //endregion


}
