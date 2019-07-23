package com.yangdb.fuse.epb.utils;

import com.yangdb.fuse.dispatcher.ontology.OntologyProvider;
import com.yangdb.fuse.epb.plan.estimation.CostEstimationConfig;
import com.yangdb.fuse.epb.plan.estimation.IncrementalEstimationContext;
import com.yangdb.fuse.epb.plan.estimation.pattern.estimators.PatternCostEstimator;
import com.yangdb.fuse.epb.plan.estimation.pattern.estimators.rule.RulesBasedPatternCostEstimator;
import com.yangdb.fuse.epb.plan.statistics.RuleBasedStatisticalProvider;
import com.yangdb.fuse.epb.plan.statistics.Statistics;
import com.yangdb.fuse.epb.plan.statistics.StatisticsProvider;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.execution.plan.Direction;
import com.yangdb.fuse.model.execution.plan.composite.Plan;
import com.yangdb.fuse.model.execution.plan.costs.CountEstimatesCost;
import com.yangdb.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.query.EBase;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.entity.EConcrete;
import com.yangdb.fuse.model.query.entity.EEntityBase;
import com.yangdb.fuse.model.query.entity.ETyped;
import com.yangdb.fuse.model.query.entity.EUntyped;
import com.yangdb.fuse.model.query.properties.EPropGroup;
import com.yangdb.fuse.model.query.properties.RelPropGroup;
import com.yangdb.fuse.unipop.schemaProviders.*;
import com.yangdb.fuse.unipop.schemaProviders.indexPartitions.StaticIndexPartitions;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Created by lior.perry on 2/20/2018.
 */
public interface DfsTestUtils {
    static PatternCostEstimator<Plan, CountEstimatesCost, IncrementalEstimationContext<Plan, PlanDetailedCost, AsgQuery>> ruleBaseEstimator(Ontology.Accessor ont) {
        return new RulesBasedPatternCostEstimator(
                new CostEstimationConfig(1, 1),
                (ontology) -> statsProvider(),
                new OntologyProvider() {
                    @Override
                    public Optional<Ontology> get(String id) {
                        return Optional.of(ont.get());
                    }

                    @Override
                    public Collection<Ontology> getAll() {
                        return Collections.singleton(ont.get());
                    }
                });
    }

    static StatisticsProvider statsProvider() {
        return new RuleBasedStatisticalProvider() {
            @Override
            public Statistics.SummaryStatistics getNodeStatistics(EEntityBase item) {
                if (item instanceof EConcrete)
                    return new Statistics.SummaryStatistics(1, 1);
                if (item instanceof ETyped)
                    return new Statistics.SummaryStatistics(100, 100);
                if (item instanceof EUntyped)
                    return new Statistics.SummaryStatistics(1000, 1000);

                return new Statistics.SummaryStatistics(1, 1);
            }

            @Override
            public Statistics.SummaryStatistics getNodeFilterStatistics(EEntityBase item, EPropGroup entityFilter) {
                if (entityFilter == null)
                    return new Statistics.SummaryStatistics(Integer.MAX_VALUE, Integer.MAX_VALUE);
                //reduce estimation due to filter existance
                if (item instanceof EConcrete)
                    return new Statistics.SummaryStatistics(1, 1);
                if (item instanceof ETyped)
                    return new Statistics.SummaryStatistics(80, 80);
                if (item instanceof EUntyped)
                    return new Statistics.SummaryStatistics(800, 800);

                //default
                return new Statistics.SummaryStatistics(1, 1);
            }

            @Override
            public Statistics.SummaryStatistics getEdgeStatistics(Rel item, EEntityBase source) {
                return new Statistics.SummaryStatistics(1, 1);
            }

            @Override
            public Statistics.SummaryStatistics getEdgeFilterStatistics(Rel item, RelPropGroup entityFilter, EEntityBase source) {
                return new Statistics.SummaryStatistics(1, 1);
            }

            @Override
            public Statistics.SummaryStatistics getRedundantNodeStatistics(EEntityBase entity, RelPropGroup relPropGroup) {
                return new Statistics.SummaryStatistics(1, 1);
            }

            @Override
            public long getGlobalSelectivity(Rel rel, RelPropGroup filter, EBase entity, Direction direction) {
                return 1;
            }
        };
    }

    static GraphElementSchemaProvider buildSchemaProvider(Ontology.Accessor ont) {
        Iterable<GraphVertexSchema> vertexSchemas =
                Stream.ofAll(ont.entities())
                        .map(entity -> (GraphVertexSchema) new GraphVertexSchema.Impl(
                                entity.geteType(),
                                new StaticIndexPartitions(Collections.singletonList("index"))))
                        .toJavaList();

        Iterable<GraphEdgeSchema> edgeSchemas =
                Stream.ofAll(ont.relations())
                        .map(relation -> (GraphEdgeSchema) new GraphEdgeSchema.Impl(
                                relation.getrType(),
                                new GraphElementConstraint.Impl(__.has(T.label, relation.getrType())),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList("entityA.id"),
                                        Optional.of(relation.getePairs().get(0).geteTypeA()))),
                                Optional.of(new GraphEdgeSchema.End.Impl(
                                        Collections.singletonList("entityB.id"),
                                        Optional.of(relation.getePairs().get(0).geteTypeB()),
                                        Arrays.asList(
                                                new GraphRedundantPropertySchema.Impl("firstName", "entityB.firstName", ont.property$("firstName").getType()),
                                                new GraphRedundantPropertySchema.Impl("gender", "entityB.gender", ont.property$("gender").getType()),
                                                new GraphRedundantPropertySchema.Impl("id", "entityB.id", ont.property$("firstName").getType()),
                                                new GraphRedundantPropertySchema.Impl("type", "entityB.type", ont.property$("type").getType())
                                        ))),
                                org.apache.tinkerpop.gremlin.structure.Direction.OUT,
                                Optional.of(new GraphEdgeSchema.DirectionSchema.Impl("direction", "out", "in")),
                                Optional.empty(),
                                Optional.of(new StaticIndexPartitions(Collections.singletonList("index"))),
                                Collections.emptyList()))
                        .toJavaList();

        return new OntologySchemaProvider(ont.get(), new GraphElementSchemaProvider.Impl(vertexSchemas, edgeSchemas));
    }

}
