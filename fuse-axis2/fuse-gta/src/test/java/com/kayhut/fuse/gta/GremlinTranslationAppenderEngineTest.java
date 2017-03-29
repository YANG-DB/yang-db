package com.kayhut.fuse.gta;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.execution.plan.EntityOp;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.PlanOpBase;
import com.kayhut.fuse.model.execution.plan.RelationOp;
import com.kayhut.fuse.model.ontology.EntityType;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.ontology.RelationshipType;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.entity.EUntyped;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.mockito.Mockito.when;

/**
 * Created by benishue on 13-Mar-17.
 */
public class GremlinTranslationAppenderEngineTest {

    @Test
    public void createTraversal_Con_Rel_Unt_Test1() throws Exception {
        Plan planOf_Con_Rel_Unt = create_Con_Rel_Unt_PathQuery();
        Ontology ontology = getOntology();
        GremlinTranslationAppenderEngine gtae = new GremlinTranslationAppenderEngine();
        Traversal traversal = gtae.createTraversal(ontology,planOf_Con_Rel_Unt);
        Assert.assertEquals(traversal.asAdmin().getSteps().size(),5);
    }

    @Test
    public void createTraversal_Con_Rel_Typ_Test2() throws Exception {
        Plan planOf_Con_Rel_Typ = create_Con_Rel_Typ_PathQuery();
        Ontology ontology = getOntology();
        GremlinTranslationAppenderEngine gtae = new GremlinTranslationAppenderEngine();
        Traversal traversal = gtae.createTraversal(ontology,planOf_Con_Rel_Typ);
        Assert.assertEquals(traversal.asAdmin().getSteps().size(),6);
    }

    @Test
    public void createTraversal_Typ_Rel_Typ_Test3() throws Exception {
        Plan planOf_Typ_Rel_Typ = create_Typ_Rel_Typ_PathQuery();
        Ontology ontology = getOntology();
        GremlinTranslationAppenderEngine gtae = new GremlinTranslationAppenderEngine();
        Traversal traversal = gtae.createTraversal(ontology,planOf_Typ_Rel_Typ);
        Assert.assertEquals(traversal.asAdmin().getSteps().size(),6);
    }

    @Test
    public void createTraversal_Typ_Rel_Con_Test4() throws Exception {
        Plan planOf_Typ_Rel_Con = create_Typ_Rel_Con_PathQuery();
        Ontology ontology = getOntology();
        GremlinTranslationAppenderEngine gtae = new GremlinTranslationAppenderEngine();
        Traversal traversal = gtae.createTraversal(ontology,planOf_Typ_Rel_Con);
        Assert.assertEquals(traversal.asAdmin().getSteps().size(),6);
    }

    @Test
    public void createTraversal_Con_Rel_Typ_Rel_Unt_Test5() throws Exception {
        Plan planOf_Con_Rel_Typ_Rel_Unt = create_Con_Rel_Typ_Rel_Unt_PathQuery();
        Ontology ontology = getOntology();
        GremlinTranslationAppenderEngine gtae = new GremlinTranslationAppenderEngine();
        Traversal traversal = gtae.createTraversal(ontology,planOf_Con_Rel_Typ_Rel_Unt);
        Assert.assertEquals(traversal.asAdmin().getSteps().size(),9);
    }


    //region Building Plans
    private Plan create_Typ_Rel_Con_PathQuery() {

        EConcrete concrete = new EConcrete();
        concrete.seteNum(3);
        concrete.seteTag("A");
        concrete.seteID("12345678");
        concrete.seteType(1); //Person
        concrete.seteName("Dardas Aba");
        AsgEBase<EConcrete> concreteAsg = AsgEBase.EBaseAsgBuilder.<EConcrete>anEBaseAsg().withEBase(concrete).build();

        Rel rel = new Rel();
        rel.seteNum(2);
        rel.setDir("R");
        rel.setrType(1);
        AsgEBase<Rel> relAsg = AsgEBase.EBaseAsgBuilder.<Rel>anEBaseAsg().withEBase(rel).withNext(concreteAsg).build();

        ETyped eTyped = new ETyped();
        eTyped.seteNum(1);
        eTyped.seteTag("B");
        eTyped.seteType(2);
        AsgEBase<ETyped> eTypedAsg = AsgEBase.EBaseAsgBuilder.<ETyped>anEBaseAsg().withEBase(eTyped).withNext(relAsg).build();

        Start start = new Start();
        start.seteNum(0);
        start.setNext(1);
        AsgEBase<Start> startAsg = AsgEBase.EBaseAsgBuilder.<Start>anEBaseAsg().withEBase(start).withNext(eTypedAsg).build();

        List<PlanOpBase> ops = new LinkedList<>();

        AsgEBase<EEntityBase> eTypBaseAsg = (AsgEBase<EEntityBase>) startAsg.getNext().get(0);
        EntityOp typOp = new EntityOp(eTypBaseAsg);
        ops.add(typOp);

        AsgEBase<Rel> relBaseAsg = (AsgEBase<Rel>) eTypBaseAsg.getNext().get(0);
        RelationOp relOp = new RelationOp(relBaseAsg);
        ops.add(relOp);

        AsgEBase<EEntityBase> conAsg = (AsgEBase<EEntityBase>) relBaseAsg.getNext().get(0);
        EntityOp concOp = new EntityOp(conAsg);
        ops.add(concOp);

        return new Plan(ops);
    }

    private Plan create_Typ_Rel_Typ_PathQuery() {
        ETyped eTyped2 = new ETyped();
        eTyped2.seteNum(3);
        eTyped2.seteTag("B");
        eTyped2.seteType(2); //Dragon
        AsgEBase<ETyped> eTypedAsg2 = AsgEBase.EBaseAsgBuilder.<ETyped>anEBaseAsg().withEBase(eTyped2).build();

        Rel rel = new Rel();
        rel.seteNum(2);
        rel.setDir("R");
        rel.setrType(1);
        AsgEBase<Rel> relAsg = AsgEBase.EBaseAsgBuilder.<Rel>anEBaseAsg().withEBase(rel).withNext(eTypedAsg2).build();

        ETyped eTyped1 = new ETyped();
        eTyped1.seteNum(1);
        eTyped1.seteTag("A");
        eTyped1.seteType(1); //Person
        AsgEBase<ETyped> eTypedAsg1 = AsgEBase.EBaseAsgBuilder.<ETyped>anEBaseAsg().withEBase(eTyped1).withNext(relAsg).build();

        Start start = new Start();
        start.seteNum(0);
        start.setNext(1);
        AsgEBase<Start> startAsg = AsgEBase.EBaseAsgBuilder.<Start>anEBaseAsg().withEBase(start).withNext(eTypedAsg1).build();

        List<PlanOpBase> ops = new LinkedList<>();

        AsgEBase<EEntityBase> typBaseAsg1 = (AsgEBase<EEntityBase>) startAsg.getNext().get(0);
        EntityOp typOp1 = new EntityOp(typBaseAsg1);
        ops.add(typOp1);

        AsgEBase<Rel> relBaseAsg = (AsgEBase<Rel>) typBaseAsg1.getNext().get(0);
        RelationOp relOp = new RelationOp(relBaseAsg);
        ops.add(relOp);

        AsgEBase<EEntityBase> typBaseAsg2 = (AsgEBase<EEntityBase>) relBaseAsg.getNext().get(0);
        EntityOp typOp2 = new EntityOp(typBaseAsg2);
        ops.add(typOp2);

        return new Plan(ops);
    }

    private Plan create_Con_Rel_Typ_PathQuery() {
        ETyped eTyped = new ETyped();
        eTyped.seteNum(3);
        eTyped.seteTag("B");
        eTyped.seteType(2);
        AsgEBase<ETyped> eTypedAsg = AsgEBase.EBaseAsgBuilder.<ETyped>anEBaseAsg().withEBase(eTyped).build();

        Rel rel = new Rel();
        rel.seteNum(2);
        rel.setDir("R");
        rel.setrType(1);
        AsgEBase<Rel> relAsg = AsgEBase.EBaseAsgBuilder.<Rel>anEBaseAsg().withEBase(rel).withNext(eTypedAsg).build();

        EConcrete concrete = new EConcrete();
        concrete.seteNum(1);
        concrete.seteTag("A");
        concrete.seteID("12345678");
        concrete.seteType(1); //Person
        concrete.seteName("Moshe Ufnik");
        AsgEBase<EConcrete> concreteAsg1 = AsgEBase.EBaseAsgBuilder.<EConcrete>anEBaseAsg().withEBase(concrete).withNext(relAsg).build();

        Start start = new Start();
        start.seteNum(0);
        start.setNext(1);
        AsgEBase<Start> startAsg = AsgEBase.EBaseAsgBuilder.<Start>anEBaseAsg().withEBase(start).withNext(concreteAsg1).build();


        List<PlanOpBase> ops = new LinkedList<>();

        AsgEBase<EEntityBase> entityAsg = (AsgEBase<EEntityBase>) startAsg.getNext().get(0);
        EntityOp concOp = new EntityOp(entityAsg);
        ops.add(concOp);

        AsgEBase<Rel> relBaseAsg = (AsgEBase<Rel>) entityAsg.getNext().get(0);
        RelationOp relOp = new RelationOp(relBaseAsg);
        ops.add(relOp);

        AsgEBase<EEntityBase> typBaseAsg = (AsgEBase<EEntityBase>) relBaseAsg.getNext().get(0);
        EntityOp typOp = new EntityOp(typBaseAsg);
        ops.add(typOp);

        return new Plan(ops);
    }

    private Plan create_Con_Rel_Unt_PathQuery() {
        EUntyped untyped = new EUntyped();
        untyped.seteNum(3);
        untyped.seteTag("B");
        AsgEBase<EUntyped> unTypedAsg = AsgEBase.EBaseAsgBuilder.<EUntyped>anEBaseAsg().withEBase(untyped).build();

        Rel rel = new Rel();
        rel.seteNum(2);
        rel.setDir("R");
        rel.setrType(1);
        AsgEBase<Rel> relAsg = AsgEBase.EBaseAsgBuilder.<Rel>anEBaseAsg().withEBase(rel).withNext(unTypedAsg).build();

        EConcrete concrete = new EConcrete();
        concrete.seteNum(1);
        concrete.seteTag("A");
        concrete.seteID("12345678");
        concrete.seteType(1); //Person
        concrete.seteName("Moshe Ufnik");
        AsgEBase<EConcrete> concreteAsg = AsgEBase.EBaseAsgBuilder.<EConcrete>anEBaseAsg().withEBase(concrete).withNext(relAsg).build();

        Start start = new Start();
        start.seteNum(0);
        start.setNext(1);
        AsgEBase<Start> startAsg = AsgEBase.EBaseAsgBuilder.<Start>anEBaseAsg().withEBase(start).withNext(concreteAsg).build();

        List<PlanOpBase> ops = new LinkedList<>();

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

    private Plan create_Con_Rel_Typ_Rel_Unt_PathQuery()
    {
        EUntyped untyped = new EUntyped();
        untyped.seteNum(5);
        untyped.seteTag("B");
        AsgEBase<EUntyped> unTypedAsg = AsgEBase.EBaseAsgBuilder.<EUntyped>anEBaseAsg().withEBase(untyped).build();


        Rel rel2 = new Rel();
        rel2.seteNum(4);
        rel2.setDir("R");
        rel2.setrType(1);
        AsgEBase<Rel> rel2Asg = AsgEBase.EBaseAsgBuilder.<Rel>anEBaseAsg().withEBase(rel2).withNext(unTypedAsg).build();

        ETyped eTyped = new ETyped();
        eTyped.seteNum(3);
        eTyped.seteTag("B");
        eTyped.seteType(2);
        AsgEBase<ETyped> eTypedAsg = AsgEBase.EBaseAsgBuilder.<ETyped>anEBaseAsg().withEBase(eTyped).withNext(rel2Asg).build();

        Rel rel1 = new Rel();
        rel1.seteNum(2);
        rel1.setDir("R");
        rel1.setrType(1);
        AsgEBase<Rel> relAsg = AsgEBase.EBaseAsgBuilder.<Rel>anEBaseAsg().withEBase(rel1).withNext(eTypedAsg).build();

        EConcrete concrete = new EConcrete();
        concrete.seteNum(1);
        concrete.seteTag("A");
        concrete.seteID("12345678");
        concrete.seteType(1); //Person
        concrete.seteName("Moshe Ufnik");
        AsgEBase<EConcrete> concreteAsg = AsgEBase.EBaseAsgBuilder.<EConcrete>anEBaseAsg().withEBase(concrete).withNext(relAsg).build();

        Start start = new Start();
        start.seteNum(0);
        start.setNext(1);
        AsgEBase<Start> startAsg = AsgEBase.EBaseAsgBuilder.<Start>anEBaseAsg().withEBase(start).withNext(concreteAsg).build();

        List<PlanOpBase> ops = new LinkedList<>();

        AsgEBase<EEntityBase> entityAsg = (AsgEBase<EEntityBase>) startAsg.getNext().get(0);
        EntityOp concOp = new EntityOp(entityAsg);
        ops.add(concOp);

        AsgEBase<Rel> rel1BaseAsg = (AsgEBase<Rel>) entityAsg.getNext().get(0);
        RelationOp rel1Op = new RelationOp(rel1BaseAsg);
        ops.add(rel1Op);


        AsgEBase<EEntityBase> typBaseAsg = (AsgEBase<EEntityBase>) rel1BaseAsg.getNext().get(0);
        EntityOp typOp = new EntityOp(typBaseAsg);
        ops.add(typOp);

        AsgEBase<Rel> rel2BaseAsg = (AsgEBase<Rel>) typBaseAsg.getNext().get(0);
        RelationOp rel2Op = new RelationOp(rel1BaseAsg);
        ops.add(rel2Op);


        AsgEBase<EEntityBase> unBaseAsg = (AsgEBase<EEntityBase>) rel2BaseAsg.getNext().get(0);
        EntityOp unOp = new EntityOp(unBaseAsg);
        ops.add(unOp);

        Plan plan  = new Plan(ops);

        return plan;
    }
    //endregion


    private Ontology getOntology() {
        Ontology ontology = Mockito.mock(Ontology.class);
        when(ontology.getEntityTypes()).thenAnswer(invocationOnMock ->
                {
                    ArrayList<EntityType> entityTypes = new ArrayList<>();
                    entityTypes.add(EntityType.EntityTypeBuilder.anEntityType()
                            .withEType(1).withName("Person").build());
                    entityTypes.add(EntityType.EntityTypeBuilder.anEntityType()
                            .withEType(2).withName("Dragon").build());
                    return  entityTypes;
                }
        );
        when(ontology.getRelationshipTypes()).thenAnswer(invocationOnMock ->
                {
                    ArrayList<RelationshipType> relTypes = new ArrayList<>();
                    relTypes.add(RelationshipType.RelationshipTypeBuilder.aRelationshipType()
                            .withRType(1).withName("Fire").build());
                    return  relTypes;
                }
        );
        return ontology;
    }

}