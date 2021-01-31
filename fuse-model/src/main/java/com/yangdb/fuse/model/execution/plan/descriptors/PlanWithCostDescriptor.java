package com.yangdb.fuse.model.execution.plan.descriptors;

/*-
 * #%L
 * fuse-model
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

/*-
 *
 * PlanWithCostDescriptor.java - fuse-model - yangdb - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import com.google.common.graph.Graph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import com.google.inject.Inject;
import com.yangdb.fuse.model.asgQuery.AsgEBase;
import com.yangdb.fuse.model.descriptors.Descriptor;
import com.yangdb.fuse.model.descriptors.GraphDescriptor;
import com.yangdb.fuse.model.descriptors.ToStringDescriptor;
import com.yangdb.fuse.model.execution.plan.*;
import com.yangdb.fuse.model.execution.plan.composite.*;
import com.yangdb.fuse.model.execution.plan.composite.descriptors.CompositePlanOpDescriptor;
import com.yangdb.fuse.model.execution.plan.composite.descriptors.IterablePlanOpDescriptor;
import com.yangdb.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.yangdb.fuse.model.execution.plan.entity.*;
import com.yangdb.fuse.model.execution.plan.relation.RelationFilterOp;
import com.yangdb.fuse.model.execution.plan.relation.RelationOp;
import com.yangdb.fuse.model.query.EBase;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.properties.BaseProp;
import com.yangdb.fuse.model.query.properties.BasePropGroup;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.yangdb.fuse.model.execution.plan.composite.descriptors.IterablePlanOpDescriptor.getIterablePlanOpDescriptor;
import static com.yangdb.fuse.model.execution.plan.descriptors.QueryDescriptor.printElements;
import static com.yangdb.fuse.model.execution.plan.descriptors.QueryDescriptor.removeRedundentArrow;

/**
 * Created by roman.margolis on 28/11/2017.
 */
public class PlanWithCostDescriptor<P, C> implements Descriptor<PlanWithCost<P, C>>, GraphDescriptor<PlanWithCost<Plan, PlanDetailedCost>> {
    //region Constructors
    @Inject
    public PlanWithCostDescriptor(Descriptor<? super P> planDescriptor, Descriptor<? super C> costDescriptor) {
        this.planDescriptor = planDescriptor;
        this.costDescriptor = costDescriptor;
    }
    //endregion

    //region Descriptor Implementation
    @Override
    public String describe(PlanWithCost<P, C> planWithCost) {
        return "{" +
                " plan:" + this.planDescriptor.describe(planWithCost.getPlan()) + "," + "\n" +
                " cost:" + this.costDescriptor.describe(planWithCost.getCost()) + "\n" +
                "}";
    }
    //endregion

    public static Graph<GraphElement> graph(PlanWithCost<Plan, PlanDetailedCost> planWithCost, boolean cycle) {

        MutableGraph<GraphElement> build = GraphBuilder.directed().allowsSelfLoops(cycle).build();
        List<PlanOp> elements = planWithCost.getPlan().getOps();
        if (elements.isEmpty())
            return build;

        build.addNode(new GraphElement(((AsgEBaseContainer) elements.get(0))));

        for (int i = 1; i < elements.size(); i++) {
            //region optional
            GraphElement nodeV = new GraphElement(((AsgEBaseContainer) elements.get(i)));
            if (elements.get(i) instanceof OptionalOp) {
                //add OptionalOp
                build.addNode(nodeV.withLabel(OptionalOp.class.getSimpleName()));
                //connect
                //add optional branch to graph
                List<PlanOp> ops = ((OptionalOp) elements.get(i)).getOps();
                //add optional starting (GoTo) step (not part of the optional branch but rather its stating point
                GraphElement optionalStep = new GraphElement(((AsgEBaseContainer) ops.get(0)));
                build.addNode(optionalStep);
                //connect starting (GoTo) step with optional step
                build.putEdge(optionalStep, nodeV);
                //connect optional step to continuation of optional branch
                build.putEdge(nodeV, new GraphElement(((AsgEBaseContainer) ops.get(1)), OptionalOp.class.getSimpleName()));

                //continue optional sib branch
                for (int j = 2; j < ops.size(); j++) {
                    GraphElement node = new GraphElement(((AsgEBaseContainer) ops.get(j)), OptionalOp.class.getSimpleName());
                    build.addNode(node);
                    build.putEdge(new GraphElement(((AsgEBaseContainer) ops.get(j - 1)), OptionalOp.class.getSimpleName()), node);
                }
            } else {
                //endregion
                build.addNode(nodeV);
                if (cycle && elements.get(i - 1) instanceof OptionalOp) {
                    OptionalOp planOp = (OptionalOp) elements.get(i - 1);
                    AsgEBaseContainer lastOp = (AsgEBaseContainer) planOp.getOps().get(planOp.getOps().size() - 1);
                    build.putEdge(new GraphElement(lastOp), nodeV);
                } else {
                    if (!cycle && elements.get(i) instanceof GoToEntityOp) {
                        continue;
                    }
                    build.putEdge(new GraphElement(((AsgEBaseContainer) elements.get(i - 1))), nodeV);
                }
            }
        }
        return build;
    }

    public static String print(PlanWithCost<Plan, PlanDetailedCost> planWithCost, boolean printId) {
        List<String> builder = new LinkedList<>();
        builder.add("cost:" + planWithCost.getCost().getGlobalCost() + "\n");
        print(new HashMap<>(), builder, planWithCost.getPlan().getOps(), 1, printId);
        return builder.toString();
    }


    static <T extends EBase> String print(Map<Integer, Integer> cursorLocations, List<String> builder, List<PlanOp> ops, int level, boolean printId) {
        builder.add("");
        int currentLine = 0;
        for (PlanOp currentOp : ops) {
            if (currentOp instanceof CompositeAsgEBasePlanOp) {
                builder.add(print(cursorLocations, new ArrayList<>(), ((CompositeAsgEBasePlanOp<T>) currentOp).getOps(), 0, printId));
            } else if (currentOp instanceof AsgEBasePlanOp) {
                AsgEBasePlanOp<T> planOp = (AsgEBasePlanOp<T>) currentOp;
                String text = QueryDescriptor.getPrefix(isTail(planOp), planOp.getAsgEbase().geteBase()) + shortLabel(planOp, new StringJoiner(":"), printId);
                if (planOp instanceof GoToEntityOp) {
                    char[] zeros = new char[cursorLocations.get(((GoToEntityOp) planOp).getAsgEbase().geteNum()) - 5];
                    Arrays.fill(zeros, ' ');
                    builder.add("\n" + String.valueOf(zeros) + text);
                    level++;
                } else if (planOp instanceof EntityJoinOp) {
                    char[] zeros = new char[cursorLocations.getOrDefault(((EntityJoinOp) planOp).getAsgEbase().geteNum(), 0)];
                    Arrays.fill(zeros, ' ');
                    builder.add("\n" + String.valueOf(zeros) + text);
                    level++;
                    builder.add("\n\t\t" + print(cursorLocations, new ArrayList<>(), ((EntityJoinOp) planOp).getLeftBranch().getOps(), 0, printId));
                    level++;
                    builder.add("\n\t\t" + print(cursorLocations, new ArrayList<>(), ((EntityJoinOp) planOp).getRightBranch().getOps(), 0, printId));
                    level++;
                } else if (planOp instanceof EntityNoOp) {
                    char[] zeros = new char[cursorLocations.get(((EntityNoOp) planOp).getAsgEbase().geteNum()) - 5];
                    Arrays.fill(zeros, ' ');
                    builder.add("\n" + String.valueOf(zeros) + text);
                    level++;
                } else {
                    builder.set(level + currentLine, builder.get(level + currentLine) + text);
                    cursorLocations.put(planOp.getAsgEbase().geteNum(), builder.get(level + currentLine).length() - 1);
                }
            }
        }
        return builder.toString();
    }

    private static <T extends EBase> boolean isTail(AsgEBasePlanOp<T> planOp) {
        return planOp instanceof EntityNoOp || planOp instanceof GoToEntityOp || planOp instanceof EntityJoinOp;
    }

    private static <T extends EBase> String shortLabel(AsgEBasePlanOp<T> element, StringJoiner joiner, boolean printId) {
        if (element instanceof GoToEntityOp) {
            return joiner.add("goTo[" + element.getAsgEbase().geteNum() + "]").toString();
        }
        if (element instanceof EntityJoinOp) {
            AsgQueryDescriptor.shortLabel(element.getAsgEbase(), joiner, printId);
            joiner.add("join[" + element.getAsgEbase().geteNum() + "]");
            return joiner.toString();
        }
        if (element instanceof EntityNoOp) {
            return joiner.add("Opt[" + element.getAsgEbase().geteNum() + "]").toString();
        } else
            return AsgQueryDescriptor.shortLabel(element.getAsgEbase(), joiner, printId);
    }

    /**
     * print execution plan visualize
     *
     * @param plans
     * @return
     */
    public static String printGraph(List<PlanWithCost<Plan, PlanDetailedCost>> plans) {
        //todo
        return "";
    }
    public static String printGraph(PlanWithCost<Plan, PlanDetailedCost> plan) {
        return new PlanWithCostDescriptor<>(
                new CompositePlanOpDescriptor(getIterablePlanOpDescriptor(IterablePlanOpDescriptor.Mode.full)),
                new ToStringDescriptor<>()).visualize(plan);
    }

    public String visualize(PlanWithCost<Plan, PlanDetailedCost> plan) {
        StringBuilder sb = new StringBuilder();
        // name
        sb.append("digraph G { \n");
        //left to right direction
        sb.append("\t rankdir=LR; \n");
        //general node shape
        sb.append("\t node [shape=Mrecord]; \n");
        //append start node shape (first node in plan elements list)
        sb.append("\t start [shape=Mdiamond, color=blue, style=\"rounded\"]; \n");


        //iterate over the plan
        List<PlanOp> ops = plan.getPlan().getOps();
        if (!ops.isEmpty()) {
            dot(plan, sb);
        }
        sb.append("\n\t }");
        return sb.toString();
    }

    /**
     * print first (root) level of plan
     *
     * @param plan
     * @param root
     */
    public void dot(PlanWithCost<Plan, PlanDetailedCost> plan, StringBuilder root) {
        Iterator<PlanOp> iterator = plan.getPlan().getOps().iterator();
        root.append("start->");
        while (iterator.hasNext()) {
            PlanOp next = iterator.next();
            if (GoToEntityOp.class.isAssignableFrom(next.getClass())) {
                removeRedundentArrow(root);
                root.append("\n");
                root.append(print((AsgEBasePlanOp) next)).append("->");
            } else {
                root.append(print((AsgEBasePlanOp) next)).append("->");
            }
        }
        removeRedundentArrow(root);
        root.append("\n");
        //print non plan op cost clusters
        root.append(printElementsDef(plan));
    }


    private String print(AsgEBasePlanOp planOp) {
        return printElements(Collections.singletonList(planOp.getAsgEbase().geteBase()));
    }

    private static String printElementsDef(PlanWithCost<Plan, PlanDetailedCost> plan) {
        AtomicInteger steps = new AtomicInteger();
        StringBuilder builder = new StringBuilder();
        plan.getCost().getPlanStepCosts().forEach(planWithCost -> {
            steps.getAndIncrement();

            StringBuilder sb = new StringBuilder();
            String name = planWithCost.getPlan().getClass().getSimpleName();
            String cost = planWithCost.getCost().toString();

            sb.append(" \n subgraph cluster_step_" + steps + "_"+name+" { \n");
            sb.append(" \t color=blue; \n");
            sb.append(" \t node [style=filled]; \n");
            sb.append(" \t label = \"step("+steps+") cost:" + cost + "\"; \n");
            //print each plan op related to its cost step
            planWithCost.getPlan()
                    .getOps().forEach(op -> printElementType(sb,plan.getPlan(), op)) ;
            //print plan op related elements
            sb.append("\t }\n");
            //append
            builder.append(sb);
        });
        return builder.toString();
    }


    private static void printElementType(StringBuilder builder, Plan plan, PlanOp op) {
        AsgEBase<? extends EBase> asgEbase = ((AsgEBaseContainer<? extends EBase>) op).getAsgEbase();
        EBase element = asgEbase.geteBase();
        //print plan op related definitions
        if(op instanceof GoToEntityOp) {
            Optional<PlanOp> prev = PlanUtil.adjacentPrev(plan, op);
            if(prev.isPresent()) {
                builder.append(((AsgEBasePlanOp)prev.get()).getAsgEbase().geteNum() + "->"+ ((AsgEBasePlanOp)op).getAsgEbase().geteNum());
                builder.append("[shape=inv, label=\"goto\", color=red] \n");
            }
        } else if (op instanceof RelationOp) {
            //print relationship symbol for the quant
            //append directed rel
                builder.append(element.geteNum() + " [ label=\"" + AsgQueryDescriptor.shortLabel(asgEbase, new StringJoiner(""), true) + "\", shape = " + (((Rel) element).getDir().equals(Rel.Direction.R) ? "rarrow" : "larrow") + "]; \n");
        }//print props group symbol
        else if ((op instanceof RelationFilterOp) || (op instanceof EntityFilterOp)) {
            builder.append(printProps((BasePropGroup) element));
        }//print prop group step
        else if (op instanceof EntityOp) {
            builder.append(element.geteNum() + " [ label=\"" + AsgQueryDescriptor.shortLabel(asgEbase, new StringJoiner(""), true) + "\" ,shape = Mrecord]; \n");
        }//print typed steps
        else if (op instanceof  OptionalOp) {
            // - todo
        }
        else if (op instanceof CountOp) {
            // - todo
        }
        else if (op instanceof UnionOp) {
            // - todo
        }
    }

    public static String printProps(BasePropGroup element) {
        //add subgraph for the entire quant
        StringBuilder prpoBuilder = new StringBuilder();
        prpoBuilder.append(" \n subgraph cluster_Props_" + element.geteNum() + " { \n");
        prpoBuilder.append(" \t color=green; \n");
        prpoBuilder.append(" \t node [shape=component]; \n");
        prpoBuilder.append(" \t " + element.geteNum() + " [color=green, shape=folder, label=\"" + element.getQuantType() + "\"]; \n");
        // label the prop group type
        prpoBuilder.append(" \t label = \" Props[" + element.geteNum() + "];\"; \n");
        //print the prop group list path itself
        //non inclusive for additional group inside the path - they will be printed separately
        List<BaseProp> props = (List<BaseProp>) element.getProps()
                .stream()
                .map(p -> ((BaseProp) p).clone())
                .collect(Collectors.toList());

        //give specific number to each property in the group
        for (int i = 0; i < props.size(); i++) {
            props.get(i).seteNum(element.geteNum() * 100 + i);
        }
        //print elements graph
        prpoBuilder.append("\n " + element.geteNum() + "->" + printElements(props));
        removeRedundentArrow(prpoBuilder);

        prpoBuilder.append("\n } \n");
        return prpoBuilder.toString();
    }

    public static class GraphElement {
        private AsgEBaseContainer planOp;
        private String label;
        private String planOpType;
        private double cost;

        public GraphElement(AsgEBaseContainer planOp) {
            this(planOp, 0, planOp.getClass().getSimpleName(), "");
        }

        public GraphElement(AsgEBaseContainer planOp, String label) {
            this(planOp, 0, planOp.getClass().getSimpleName(), label);
        }

        public GraphElement(AsgEBaseContainer planOp, double cost, String opType, String label) {
            this.planOp = planOp;
            this.cost = cost;
            this.planOpType = opType;
            this.label = label;
        }

        public AsgEBaseContainer getPlanOp() {
            return planOp;
        }

        public String getPlanOpType() {
            return planOpType;
        }

        public double getCost() {
            return cost;
        }

        public String getLabel() {
            return label;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            GraphElement that = (GraphElement) o;

            return planOp != null ? planOp.getAsgEbase().equals(that.planOp.getAsgEbase()) : that.planOp == null;
        }

        @Override
        public int hashCode() {
            return planOp != null ? planOp.getAsgEbase().hashCode() : 0;
        }

        public GraphElement withLabel(String label) {
            this.label = label;
            return this;
        }
    }

    //region Fields
    private Descriptor<? super P> planDescriptor;
    private Descriptor<? super C> costDescriptor;
    //endregion
}
