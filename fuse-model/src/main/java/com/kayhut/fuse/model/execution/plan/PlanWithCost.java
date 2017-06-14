package com.kayhut.fuse.model.execution.plan;

/**
 * Created by Roman on 20/04/2017.
 */
public class PlanWithCost<P, C> {
    //region Constructors
    public PlanWithCost(P plan, C cost) {
        this.plan = plan;
        this.cost = cost;
    }

    public PlanWithCost(PlanWithCost<P, C> planWithCost) {
        this.cost = planWithCost.cost;//todo clone ?
        this.plan = planWithCost.plan;//todo clone ?
    }
    //endregion

    //region Properties

    public P getPlan() {
        return plan;
    }

    public C getCost() {
        return cost;
    }

    public void setPlan(P plan) {
        this.plan = plan;
    }

    public void setCost(C cost) {
        this.cost = cost;
    }

    //endregion

    //region Fields
    private P plan;
    private C cost;
    //endregion


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlanWithCost<?, ?> that = (PlanWithCost<?, ?>) o;

        if (plan != null ? !plan.equals(that.plan) : that.plan != null) return false;
        return cost != null ? cost.equals(that.cost) : that.cost == null;
    }

    @Override
    public int hashCode() {
        int result = plan != null ? plan.hashCode() : 0;
        result = 31 * result + (cost != null ? cost.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "{" +
                    " plan:" + plan.toString() + "," + "\n" +
                    " cost:" + cost.toString() + "\n" +
                "}";
    }
}
