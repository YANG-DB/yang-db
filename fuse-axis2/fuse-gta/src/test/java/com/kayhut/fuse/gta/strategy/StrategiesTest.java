package com.kayhut.fuse.gta.strategy;

import com.kayhut.fuse.gta.translation.PlanUtil;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.execution.plan.EntityOp;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.ontology.EntityType;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.unipop.PromiseGraph;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.DefaultGraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.when;

/**
 * Created by benishue on 12-Mar-17.
 */
public class StrategiesTest {
    @Test
    public void EntityOpStartTranslationStrategyTest() throws Exception {
        EConcrete concrete = new EConcrete();
        concrete.seteNum(1);
        concrete.seteTag("A");
        AsgEBase<EEntityBase> ebaseAsgEBase = AsgEBase.EBaseAsgBuilder.<EEntityBase>anEBaseAsg().withEBase(concrete).build();

        Plan plan = Mockito.mock(Plan.class);
        when(plan.getOps()).thenAnswer(invocationOnMock ->
            {
                return new ArrayList<PlanOpBase>().add(new EntityOp(ebaseAsgEBase));
            }
        );

        Ontology ontology = Mockito.mock(Ontology.class);
        when(ontology.getEntityTypes()).thenAnswer(invocationOnMock ->
            {
                return  new ArrayList<EntityType>().add(EntityType.EntityTypeBuilder.anEntityType()
                .withEType(1).withName("Person").build());
            }
        );

        PlanUtil planUtil = Mockito.mock(PlanUtil.class);
        when(planUtil.isFirst(any(),any())).thenAnswer(invocationOnMock -> true);

        EntityOpStartTranslationStrategy entityOpStartTranslationStrategy = new EntityOpStartTranslationStrategy(new PromiseGraph());

        TranslationStrategyContext translationStrategyContext = Mockito.mock(TranslationStrategyContext.class);
        when(translationStrategyContext.getOntology()).thenAnswer( invocationOnMock -> ontology);
        when(translationStrategyContext.getPlan()).thenAnswer(invocationOnMock -> plan);
        when(translationStrategyContext.getPlanOpBase()).thenAnswer(invocationOnMock -> plan.getOps().get(0));
        entityOpStartTranslationStrategy.apply(translationStrategyContext, new DefaultGraphTraversal());
        
        Assert.assertTrue(traversal.asAdmin().getSteps().iterator().hasNext());

    }

}