package com.kayhut.fuse.model;

/**
 * Created by lior on 19/02/2017.
 */
public class Plan implements Content<Plan> {
    private Plan data;
    private String id;

    public Plan() {}


    public void setData(Plan data) {
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
    public Plan getData() {
        return data;
    }


    @Override
    public boolean isCompleted() {
        return true;
    }

    @Override
    public long getResults() {
        return data.toString().length();
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

        public PlanBuilder data(Plan data) {
            this.plan.setData(data);
            return this;
        }

        public Plan compose() {
            return plan;
        }
    }
}
