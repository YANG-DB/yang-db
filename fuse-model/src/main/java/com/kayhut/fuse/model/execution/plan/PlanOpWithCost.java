package com.kayhut.fuse.model.execution.plan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import static com.kayhut.fuse.model.Utils.fullPattern;

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

    public PlanOpWithCost(C cost, double countEstimates, List<PlanOpBase> ops){
        this(cost, countEstimates);
        this.opBase = new ArrayList<>(ops);
    }

    public PlanOpWithCost(C cost, Stack<Double> countEstimates, List<PlanOpBase> ops){
        this.cost = cost;
        this.countEstimates = (Stack<Double>) countEstimates.clone();
        this.opBase = new ArrayList<>(ops);
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

    @Override
    public String toString() {
        return fullPattern(getOpBase()) +
                "[estimation=" + cost +"]";
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
        throw new RuntimeException("No estimation found for index "+index);
    }

    public static <C> PlanOpWithCost<C> of(C cost, int lambda, PlanOpBase op) {
        return new PlanOpWithCost<C>(cost,lambda,op);
    }


}
