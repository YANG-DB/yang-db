package com.kayhut.fuse.model;

/**
 * Created by lior on 19/02/2017.
 */
public class Plan implements Content {
    private String data;
    private String id;

    public Plan() {}


    public void setData(String data) {
        this.data = data;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getData() {
        return data;
    }


    @Override
    public boolean isCompleted() {
        return true;
    }

    @Override
    public long getResults() {
        return data.length();
    }

    public static class PlanBuilder {
        private Plan plan;

        public PlanBuilder(String id) {
            plan = new Plan();
            plan.setId(id);
        }

        public static PlanBuilder builder(String id) {
            PlanBuilder builder = new PlanBuilder(id);
            return builder;
        }

        public PlanBuilder data(String data) {
            this.plan.setData(data);
            return this;
        }

        public Plan compose() {
            return plan;
        }
    }
}
