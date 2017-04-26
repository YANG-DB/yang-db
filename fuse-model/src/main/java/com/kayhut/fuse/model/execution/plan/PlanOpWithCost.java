package com.kayhut.fuse.model.execution.plan;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;

/**
 * Created by moti on 3/27/2017.
 */
public class PlanOpWithCost<C> {
    //region Constructors
    public PlanOpWithCost(C cost, double countEstimates, PlanOpBase ... opBase) {
        this.cost = cost;
        this.opBase = Arrays.asList(opBase);
        this.countEstimates = new Stack<>();
        this.countEstimates.push(countEstimates);
    }
    //endregion

    //region Methods
    public C getCost() {
        return cost;
    }

    public List<PlanOpBase> getOpBase() {
        return opBase;
    }

    public Stack<Double> getCountEstimates() {
        return countEstimates;
    }

    //endregion

    //region Members
    private C cost;
    private List<PlanOpBase> opBase;
    private Stack<Double> countEstimates;
    //endregion
    
    public static <C> C get(List<PlanOpWithCost<C>> ops,int index) {
        ops.size();
        for (int i = 0; i < ops.size(); i++) {
             if(i==index)
                 return ops.get(i).getCost();
            
        }
        throw new RuntimeException("No cost found for index "+index);
    }

    public static <C> PlanOpWithCost<C> of(C cost, int lambda, PlanOpBase op) {
        return new PlanOpWithCost<C>(cost,lambda,op);
    }

    public double push(double value) {
        return countEstimates.push(value);
    }

    public double peek() {
        return countEstimates.peek();
    }


}
