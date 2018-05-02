package com.kayhut.fuse.epb.plan.statistics;

import com.google.inject.Provider;
import com.kayhut.fuse.dispatcher.gta.PlanTraversalTranslator;
import com.kayhut.fuse.dispatcher.gta.TranslationContext;
import com.kayhut.fuse.executor.ontology.UniGraphProvider;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.execution.plan.Direction;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.costs.CountEstimatesCost;
import com.kayhut.fuse.model.execution.plan.costs.DoubleCost;
import com.kayhut.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.kayhut.fuse.model.execution.plan.entity.EntityFilterOp;
import com.kayhut.fuse.model.execution.plan.entity.EntityOp;
import com.kayhut.fuse.model.execution.plan.relation.RelationFilterOp;
import com.kayhut.fuse.model.execution.plan.relation.RelationOp;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.RedundantRelProp;
import com.kayhut.fuse.model.query.properties.RelPropGroup;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ElasticCountStatisticsProvider implements StatisticsProvider  {

    public ElasticCountStatisticsProvider(PlanTraversalTranslator planTraversalTranslator, Ontology ontology, Provider<UniGraphProvider> uniGraphProvider) {
        this.planTraversalTranslator = planTraversalTranslator;
        this.ontology = ontology;
        this.uniGraphProvider = uniGraphProvider;
    }

    @Override
    public Statistics.SummaryStatistics getNodeStatistics(EEntityBase item) {
        Plan plan = new Plan(new EntityOp(new AsgEBase<>(item)));
        return getSummaryStatistics(plan);
    }

    private Statistics.SummaryStatistics getSummaryStatistics(Plan plan) {
        GraphTraversal<?, ?> traversal;
        try {
             traversal = planTraversalTranslator.translate(new PlanWithCost<>(plan, new PlanDetailedCost(new DoubleCost(1), Collections.singleton(new PlanWithCost<>(plan, new CountEstimatesCost(1, 1)))))
                    , new TranslationContext(new Ontology.Accessor(ontology), uniGraphProvider.get().getGraph(ontology).traversal()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        long count = traversal.count().next();

        return new Statistics.SummaryStatistics(count, count);
    }

    @Override
    public Statistics.SummaryStatistics getNodeFilterStatistics(EEntityBase item, EPropGroup entityFilter) {
        Plan plan = new Plan(new EntityOp(new AsgEBase<>(item)), new EntityFilterOp(new AsgEBase<>(entityFilter)));
        return getSummaryStatistics(plan);
    }

    @Override
    public Statistics.SummaryStatistics getEdgeStatistics(Rel item, EEntityBase source) {
        Plan plan = new Plan(new EntityOp(new AsgEBase<>(source)), new RelationOp(new AsgEBase<>(item)));
        return getSummaryStatistics(plan);
    }

    @Override
    public Statistics.SummaryStatistics getEdgeFilterStatistics(Rel item, RelPropGroup entityFilter, EEntityBase source) {
        Plan plan = new Plan(new EntityOp(new AsgEBase<>(source)), new RelationOp(new AsgEBase<>(item)), new RelationFilterOp(new AsgEBase<>(entityFilter)));
        return getSummaryStatistics(plan);
    }

    @Override
    public Statistics.SummaryStatistics getRedundantNodeStatistics(EEntityBase entity, RelPropGroup relPropGroup) {
        List<RedundantRelProp> pushdownProps = relPropGroup.getProps().stream().filter(prop -> prop instanceof RedundantRelProp).
                map(RedundantRelProp.class::cast).collect(Collectors.toList());

        EPropGroup ePropGroup = new EPropGroup(pushdownProps.stream().map(prop -> EProp.of(prop.geteNum(), prop.getpType(), prop.getCon())).collect(Collectors.toList()));
        return getNodeFilterStatistics(entity, ePropGroup);
    }

    @Override
    public long getGlobalSelectivity(Rel rel, RelPropGroup filter, EBase entity, Direction direction) {
        return 100;
    }

    private PlanTraversalTranslator planTraversalTranslator;
    private Ontology ontology;
    private Provider<UniGraphProvider> uniGraphProvider;


}
