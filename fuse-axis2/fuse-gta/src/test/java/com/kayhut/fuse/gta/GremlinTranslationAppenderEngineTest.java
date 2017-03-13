package com.kayhut.fuse.gta;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.EntityOp;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.execution.plan.RelationOp;
import com.kayhut.fuse.model.ontology.EntityType;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.entity.EUntyped;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import javax.management.relation.Relation;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * Created by benishue on 13-Mar-17.
 */
public class GremlinTranslationAppenderEngineTest {
    private static Plan planOf2;

    @Test
    public void createTraversalTest1() throws Exception {

        Ontology ontology = Mockito.mock(Ontology.class);
        when(ontology.getEntityTypes()).thenAnswer(invocationOnMock ->
                {
                    ArrayList<EntityType> entityTypes = new ArrayList<>();
                    entityTypes.add(EntityType.EntityTypeBuilder.anEntityType()
                            .withEType(1).withName("Person").build());
                    return  entityTypes;
                }
        );

        GremlinTranslationAppenderEngine gtae = new GremlinTranslationAppenderEngine(ontology);
        Traversal traversal = gtae.CreateTraversal(planOf2);
    }

    @BeforeClass
    public static void setUpOnce() {
        createPlanOf2();
    }

    private static void createPlanOf2() {
        AsgQuery twoEntitiesPathQuery = createTwoEntitiesPathQuery();
        planOf2 = createPlanForTwoEntitiesPathQuery(twoEntitiesPathQuery);
    }

    public static AsgQuery createTwoEntitiesPathQuery() {
        EUntyped untyped = new EUntyped();
        untyped.seteNum(3);
        untyped.seteTag("B");

        AsgEBase<EUntyped> unTypedAsg3 = AsgEBase.EBaseAsgBuilder.<EUntyped>anEBaseAsg().withEBase(untyped).build();

        Rel rel = new Rel();
        rel.seteNum(2);
        rel.setDir("R");
        AsgEBase<Rel> relAsg2 = AsgEBase.EBaseAsgBuilder.<Rel>anEBaseAsg().withEBase(rel).withNext(unTypedAsg3).build();

        EConcrete concrete = new EConcrete();
        concrete.seteNum(1);
        concrete.seteTag("A");
        concrete.seteID("12345678");
        concrete.seteType(1); //Person
        concrete.seteName("Moshe Ufnik");
        AsgEBase<EConcrete> concreteAsg1 = AsgEBase.EBaseAsgBuilder.<EConcrete>anEBaseAsg().withEBase(concrete).withNext(relAsg2).build();

        Start start = new Start();
        start.seteNum(0);
        start.setNext(1);
        AsgEBase<Start> startAsg = AsgEBase.EBaseAsgBuilder.<Start>anEBaseAsg().withEBase(start).withNext(concreteAsg1).build();

        AsgQuery query = AsgQuery.AsgQueryBuilder.anAsgQuery().withStart(startAsg).build();

        return query;
    }

    public static Plan createPlanForTwoEntitiesPathQuery(AsgQuery asgQuery) {
        List<PlanOpBase> ops = new LinkedList<>();

        AsgEBase<Start> startAsg = asgQuery.getStart();
        AsgEBase<EEntityBase> entityAsg = (AsgEBase<EEntityBase>) startAsg.getNext().get(0);

        EntityOp concOp = new EntityOp(entityAsg);
        ops.add(concOp);

        AsgEBase<Rel> relBaseAsg = (AsgEBase<Rel>) entityAsg.getNext().get(0);
        RelationOp relOp = new RelationOp(relBaseAsg);
        ops.add(relOp);

        AsgEBase<EEntityBase> unBaseAsg = (AsgEBase<EEntityBase>) relBaseAsg.getNext().get(0);
        EntityOp unOp = new EntityOp(unBaseAsg);
        ops.add(unOp);

        return new Plan(ops);
    }

}