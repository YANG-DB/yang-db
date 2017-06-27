package com.kayhut.fuse.epb.tests;

import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.dispatcher.utils.PlanUtil;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.*;
import com.kayhut.fuse.model.execution.plan.costs.Cost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.query.Constraint;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.*;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.model.query.properties.RelPropGroup;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by liorp on 4/26/2017.
 */
public interface PlanMockUtils {

    String EDGE_FILTER_STATISTICS = "edgeFilterStatistics";
    String EDGE_STATISTICS = "edgeTypeStatistics";
    String NODE_FILTER_STATISTICS = "nodeFilterStatistics";
    String NODE_STATISTICS = "nodeTypeStatistics";

    enum Type {
        CONCRETE(EConcrete.class, true),
        TYPED(ETyped.class, true),
        UN_TYPED(EUntyped.class, false);

        private Class<? extends EEntityBase> type;
        private boolean eTyped;

        Type(Class<? extends EEntityBase> type, boolean eTyped) {
            this.type = type;
            this.eTyped = eTyped;
        }

        public Class<? extends EEntityBase> getType() {
            return type;
        }

        public boolean iseTyped() {
            return eTyped;
        }
    }

    class PlanMockBuilder {
        private AsgQuery asgQuery;
        private Map<String, Double> nodeTypeStatistics;
        private Map<String, Double> nodeFilterStatistics;
        private Map<String, Double> edgeTypeStatistics;
        private Map<String, Double> edgeFilterStatistics;
        private Map<PlanOpBase, Double> costs;

        private Plan plan;
        private Plan oldPlan;
        private int enumIndex;

        private PlanMockBuilder(AsgQuery asgQuery) {
            this();
            this.asgQuery = asgQuery;
        }

        public PlanMockBuilder() {
            //plan
            plan = new Plan();
            //statistics
            nodeTypeStatistics = new HashMap<>();
            nodeFilterStatistics = new HashMap<>();
            edgeTypeStatistics = new HashMap<>();
            edgeFilterStatistics = new HashMap<>();
            costs = new HashMap<>();

        }

        public static PlanMockBuilder mock() {
            return new PlanMockBuilder();
        }

        public static PlanMockBuilder mock(AsgQuery asgQuery) {
            return new PlanMockBuilder(asgQuery);
        }

        public PlanMockBuilder entity(Type type, long total, String eType) throws Exception {
            int eNum = enumIndex++;
            EntityOp entityOp = new EntityOp();
            EEntityBase instance = type.type.newInstance();
            instance.seteNum(eNum);
            //no type => max nodes return
            nodeTypeStatistics.put(eType, Double.MAX_VALUE);
            if (eType != null) {
                ((Typed.eTyped) instance).seteType(eType);
                nodeTypeStatistics.put(eType, (double) total);
            }
            entityOp.setAsgEBase(new AsgEBase<>(instance));
            plan = plan.withOp(entityOp);
            //statistics simulator
            costs.put(entityOp, (double) total);
            return this;
        }

        public PlanMockBuilder entity(int num) {
            plan = plan.withOp(new EntityOp(AsgQueryUtil.element$(asgQuery, num)));
            return this;
        }

        public PlanMockBuilder entity(EEntityBase instance, long total, String eType) throws Exception {
            EntityOp entityOp = new EntityOp();
            //no type => max nodes return
            nodeTypeStatistics.put(eType, Double.MAX_VALUE);
            if (eType != null) {
                ((Typed.eTyped) instance).seteType(eType);
                nodeTypeStatistics.put(eType, (double) total);
            }
            entityOp.setAsgEBase(new AsgEBase<>(instance));
            plan = plan.withOp(entityOp);
            //statistics simulator
            costs.put(entityOp, (double) total);
            return this;
        }

        public PlanMockBuilder rel(int num) {
            plan = plan.withOp(new RelationOp(AsgQueryUtil.element$(asgQuery, num)));
            return this;
        }

        public PlanMockBuilder rel(int num, Rel.Direction direction) {
            plan = plan.withOp(new RelationOp(AsgQueryUtil.element$(asgQuery, num), direction));
            return this;
        }

        public PlanMockBuilder relFilter(int num) {
            plan = plan.withOp(new RelationFilterOp(AsgQueryUtil.element$(asgQuery, num)));
            return this;
        }

        public PlanMockBuilder rel(Direction direction, String relType, long total) throws Exception {
            Rel rel = new Rel();
            rel.setDir(Direction.valueOf(direction.name()).to());
            rel.setrType(relType);
            RelationOp relationOp = new RelationOp(new AsgEBase<>(rel));
            //no type => max nodes return
            if (relType != null) {
                edgeTypeStatistics.put(relType, (double) total);
            }

            plan = plan.withOp(relationOp);
            //statistics simulator
            costs.put(relationOp, (double) total);
            return this;
        }

        public PlanMockBuilder entityFilter(int num) {
            plan = plan.withOp(new EntityFilterOp(AsgQueryUtil.element$(asgQuery, num)));
            return this;
        }

        public PlanMockBuilder entityFilter(double factor, int eNum, int pType, Constraint constraint) throws Exception {
            return entityFilter(factor,eNum,Integer.toString(pType),constraint);
        }

        public PlanMockBuilder entityFilter(double factor, int eNum, String pType, Constraint constraint) throws Exception {
            EPropGroup ePropGroup = new EPropGroup(Collections.singletonList(EProp.of(pType, eNum, constraint)));
            EntityFilterOp filterOp = new EntityFilterOp(new AsgEBase<>(ePropGroup));
            EntityOp last = PlanUtil.last$(plan, planOp -> true);
            plan = plan.withOp(filterOp);
            nodeFilterStatistics.put(String.valueOf(eNum), factor);
            //statistics simulator
            costs.put(filterOp, 0d);
            costs.put(last,costs.get(last)*factor);
            return this;
        }

        public PlanMockBuilder relFilter(double factor, int eNum, int pType, Constraint constraint) throws Exception {
            return relFilter(factor,eNum,Integer.toString(pType),constraint);
        }

        public PlanMockBuilder relFilter(double factor, int eNum, String pType, Constraint constraint) throws Exception {
            RelPropGroup relPropGroup = new RelPropGroup(Collections.singletonList(RelProp.of(pType, eNum, constraint)));
            RelationFilterOp relationFilterOp = new RelationFilterOp(new AsgEBase<>(relPropGroup));

            RelationOp last = PlanUtil.last$(plan, planOp -> true);
            plan = plan.withOp(relationFilterOp);
            edgeFilterStatistics.put(String.valueOf(eNum), factor );
            //statistics simulator
            costs.put(relationFilterOp, factor);
            costs.put(last,costs.get(last)*factor);
            return this;
        }

        public Plan plan() {
            return plan;
        }


        public PlanWithCost<Plan, PlanDetailedCost> oldPlanWithCost(long globalCost, long total) {
            Cost cost = new Cost(globalCost );
            List<PlanOpWithCost<Cost>> collect = oldPlan.getOps().stream().map(element -> new PlanOpWithCost<>(getCost(element), total, element)).collect(Collectors.toList());
            return new PlanWithCost<>(oldPlan, new PlanDetailedCost(cost, collect));
        }

        private Cost getCost(PlanOpBase opBase) {
            return new Cost(costs.getOrDefault(opBase, 1d));
        }

        public Map<PlanOpBase, Double> costs() {
            return costs;
        }

        public Map<String, Map<String, Double>> statistics() {
            Map<String, Map<String, Double>> map = new HashMap<>();
            map.put(EDGE_FILTER_STATISTICS, edgeFilterStatistics);
            map.put(EDGE_STATISTICS, edgeTypeStatistics);
            map.put(NODE_FILTER_STATISTICS, nodeFilterStatistics);
            map.put(NODE_STATISTICS, nodeTypeStatistics);
            return map;
        }

        public PlanMockBuilder startNewPlan() {
            oldPlan = new Plan(plan.getOps());
            return this;
        }

        public PlanMockBuilder goTo(int num) {
            plan = plan.withOp(new GoToEntityOp(AsgQueryUtil.element$(asgQuery, num)));
            return this;
        }
    }
}
