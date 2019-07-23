package com.yangdb.fuse.epb.plan.statistics;

/*-
 * #%L
 * fuse-dv-epb
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
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

import com.google.inject.Provider;
import com.yangdb.fuse.dispatcher.gta.PlanTraversalTranslator;
import com.yangdb.fuse.dispatcher.gta.TranslationContext;
import com.yangdb.fuse.epb.plan.statistics.configuration.ElasticCountStatisticsConfig;
import com.yangdb.fuse.executor.ontology.UniGraphProvider;
import com.yangdb.fuse.model.asgQuery.AsgEBase;
import com.yangdb.fuse.model.execution.plan.Direction;
import com.yangdb.fuse.model.execution.plan.PlanWithCost;
import com.yangdb.fuse.model.execution.plan.composite.Plan;
import com.yangdb.fuse.model.execution.plan.costs.CountEstimatesCost;
import com.yangdb.fuse.model.execution.plan.costs.DoubleCost;
import com.yangdb.fuse.model.execution.plan.costs.PlanDetailedCost;
import com.yangdb.fuse.model.execution.plan.entity.EntityFilterOp;
import com.yangdb.fuse.model.execution.plan.entity.EntityOp;
import com.yangdb.fuse.model.execution.plan.relation.RelationFilterOp;
import com.yangdb.fuse.model.execution.plan.relation.RelationOp;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.query.EBase;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.entity.EEntityBase;
import com.yangdb.fuse.model.query.entity.ETyped;
import com.yangdb.fuse.model.query.properties.EProp;
import com.yangdb.fuse.model.query.properties.EPropGroup;
import com.yangdb.fuse.model.query.properties.RedundantRelProp;
import com.yangdb.fuse.model.query.properties.RelPropGroup;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ElasticCountStatisticsProvider implements StatisticsProvider  {

    public ElasticCountStatisticsProvider(PlanTraversalTranslator planTraversalTranslator, Ontology ontology, Provider<UniGraphProvider> uniGraphProvider, ElasticCountStatisticsConfig elasticCountStatisticsConfig) {
        this.planTraversalTranslator = planTraversalTranslator;
        this.ontology = ontology;
        this.uniGraphProvider = uniGraphProvider;
        this.elasticCountStatisticsConfig = elasticCountStatisticsConfig;
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
        if(entity instanceof ETyped) {
            ETyped eTyped = (ETyped) entity;
            return elasticCountStatisticsConfig.getRelationSelectivity(rel.getrType(), eTyped.geteType(), direction);
        }
        return elasticCountStatisticsConfig.getRelationSelectivity(rel.getrType(), "",direction);
    }

    private PlanTraversalTranslator planTraversalTranslator;
    private Ontology ontology;
    private Provider<UniGraphProvider> uniGraphProvider;
    private ElasticCountStatisticsConfig elasticCountStatisticsConfig;


}
