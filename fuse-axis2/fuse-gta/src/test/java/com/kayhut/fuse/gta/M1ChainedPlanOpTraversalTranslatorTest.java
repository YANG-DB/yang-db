package com.kayhut.fuse.gta;

import com.kayhut.fuse.executor.uniGraphProvider.UniGraphProvider;
import com.kayhut.fuse.gta.strategy.M1PlanOpTranslationStrategy;
import com.kayhut.fuse.gta.translation.ChainedPlanOpTraversalTranslator;
import com.kayhut.fuse.gta.translation.PlanTraversalTranslator;
import com.kayhut.fuse.gta.translation.TranslationContext;
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
import com.kayhut.fuse.unipop.controller.GlobalConstants;
import com.kayhut.fuse.unipop.promise.Constraint;
import com.kayhut.fuse.unipop.promise.Promise;
import com.kayhut.fuse.unipop.promise.PromiseGraph;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.T;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.unipop.structure.UniGraph;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by Roman on 14/05/2017.
 */
public class M1ChainedPlanOpTraversalTranslatorTest {
    UniGraphProvider uniGraphProvider;
    PlanTraversalTranslator translator;

    @Before
    public void setUp() throws Exception {
        translator = new ChainedPlanOpTraversalTranslator(new M1PlanOpTranslationStrategy());
        UniGraph uniGraph = mock(UniGraph.class);
        when(uniGraph.traversal()).thenReturn(new GraphTraversalSource(uniGraph));
        this.uniGraphProvider = mock(UniGraphProvider.class);
        when(uniGraphProvider.getGraph(any())).thenReturn(uniGraph);
    }

    @Test
    public void test_concrete_rel_untyped() throws Exception {
        Plan plan = create_Con_Rel_Unt_PathQuery();
        Ontology ontology = getOntology();
        Traversal actualTraversal = translator.translate(plan, new TranslationContext(ontology, new PromiseGraph().traversal()));

        Traversal expectedTraversal =
                __.start().V().as("A")
                        .has(GlobalConstants.HasKeys.CONSTRAINT, Constraint.by(__.has(T.id, "12345678")))
                        .outE(GlobalConstants.Labels.PROMISE).as("A-->B")
                        .has(GlobalConstants.HasKeys.CONSTRAINT, Constraint.by(__.and(__.has(T.label, "Fire"), __.has("direction", Direction.OUT))))
                        .otherV().as("B")
                        .path();

        Assert.assertEquals(expectedTraversal, actualTraversal);
    }

    @Test
    public void test_concrete_rel_typed() throws Exception {
        Plan plan = create_Con_Rel_Typ_PathQuery();
        Ontology ontology = getOntology();
        Traversal actualTraversal = translator.translate(plan, new TranslationContext(ontology, new PromiseGraph().traversal()));

        Traversal expectedTraversal =
                new PromiseGraph().traversal().V().as("A")
                        .has(GlobalConstants.HasKeys.CONSTRAINT, Constraint.by(__.has(T.id, "12345678")))
                        .outE(GlobalConstants.Labels.PROMISE).as("A-->B")
                        .has(GlobalConstants.HasKeys.CONSTRAINT, Constraint.by(__.and(__.has(T.label, "Fire"), __.has("direction", Direction.OUT))))
                        .otherV().as("B")
                        .path();

        Assert.assertEquals(expectedTraversal, actualTraversal);
    }

    @Test
    public void test_typed_rel_concrete() throws Exception {
        Plan plan = create_Typ_Rel_Con_PathQuery();
        Ontology ontology = getOntology();
        Traversal actualTraversal = translator.translate(plan, new TranslationContext(ontology, new PromiseGraph().traversal()));

        Traversal expectedTraversal =
                new PromiseGraph().traversal().V().as("B")
                        .has(GlobalConstants.HasKeys.CONSTRAINT, Constraint.by(__.has(T.label, "Dragon")))
                        .outE(GlobalConstants.Labels.PROMISE).as("B-->A")
                        .has(GlobalConstants.HasKeys.CONSTRAINT, Constraint.by(__.and(__.has(T.label, "Fire"), __.has("direction", Direction.OUT))))
                        .otherV().as("A")
                        .path();

        Assert.assertEquals(expectedTraversal, actualTraversal);
    }

    @Test
    public void test_typed_rel_typed() throws Exception {
        Plan plan = create_Typ_Rel_Typ_PathQuery();
        Ontology ontology = getOntology();
        Traversal actualTraversal = translator.translate(plan, new TranslationContext(ontology, new PromiseGraph().traversal()));

        Traversal expectedTraversal =
                new PromiseGraph().traversal().V().as("A")
                        .has(GlobalConstants.HasKeys.CONSTRAINT, Constraint.by(__.has(T.label, "Person")))
                        .outE(GlobalConstants.Labels.PROMISE).as("A-->B")
                        .has(GlobalConstants.HasKeys.CONSTRAINT, Constraint.by(__.and(
                                __.has(T.label, "Fire"),
                                __.has(GlobalConstants.HasKeys.DIRECTION, Direction.OUT))))
                        .otherV().as("B")
                        .path();

        Assert.assertEquals(expectedTraversal, actualTraversal);
    }

    @Test
    public void test_concrete_rel_typed_rel_untyped() throws Exception {
        Plan plan = create_Con_Rel_Typ_Rel_Unt_PathQuery();
        Ontology ontology = getOntology();
        Traversal actualTraversal = translator.translate(plan, new TranslationContext(ontology, new PromiseGraph().traversal()));

        Traversal expectedTraversal =
                new PromiseGraph().traversal().V().as("A")
                        .has(GlobalConstants.HasKeys.CONSTRAINT, Constraint.by(__.has(T.id, "12345678")))
                        .outE(GlobalConstants.Labels.PROMISE).as("A-->B")
                        .has(GlobalConstants.HasKeys.CONSTRAINT, Constraint.by(__.and(
                                __.has(T.label, "Fire"),
                                __.has(GlobalConstants.HasKeys.DIRECTION, Direction.OUT))))
                        .otherV().as("B")
                        .outE(GlobalConstants.Labels.PROMISE).as("B-->C")
                        .has(GlobalConstants.HasKeys.CONSTRAINT, Constraint.by(__.and(
                                __.has(T.label, "Fire"),
                                __.has(GlobalConstants.HasKeys.DIRECTION, Direction.OUT))))
                        .otherV().as("C")
                        .path();

        Assert.assertEquals(expectedTraversal, actualTraversal);
    }

    //region Building Plans
    private Plan create_Typ_Rel_Con_PathQuery() {

        EConcrete concrete = new EConcrete();
        concrete.seteNum(3);
        concrete.seteTag("A");
        concrete.seteID("12345678");
        concrete.seteType(1); //Person
        concrete.seteName("Dardas Aba");
        AsgEBase<EConcrete> concreteAsg = AsgEBase.Builder.<EConcrete>get().withEBase(concrete).build();

        Rel rel = new Rel();
        rel.seteNum(2);
        rel.setDir(Rel.Direction.R);
        rel.setrType(1);
        AsgEBase<Rel> relAsg = AsgEBase.Builder.<Rel>get().withEBase(rel).withNext(concreteAsg).build();

        ETyped eTyped = new ETyped();
        eTyped.seteNum(1);
        eTyped.seteTag("B");
        eTyped.seteType(2);
        AsgEBase<ETyped> eTypedAsg = AsgEBase.Builder.<ETyped>get().withEBase(eTyped).withNext(relAsg).build();

        Start start = new Start();
        start.seteNum(0);
        start.setNext(1);
        AsgEBase<Start> startAsg = AsgEBase.Builder.<Start>get().withEBase(start).withNext(eTypedAsg).build();

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
        AsgEBase<ETyped> eTypedAsg2 = AsgEBase.Builder.<ETyped>get().withEBase(eTyped2).build();

        Rel rel = new Rel();
        rel.seteNum(2);
        rel.setDir(Rel.Direction.R);
        rel.setrType(1);
        AsgEBase<Rel> relAsg = AsgEBase.Builder.<Rel>get().withEBase(rel).withNext(eTypedAsg2).build();

        ETyped eTyped1 = new ETyped();
        eTyped1.seteNum(1);
        eTyped1.seteTag("A");
        eTyped1.seteType(1); //Person
        AsgEBase<ETyped> eTypedAsg1 = AsgEBase.Builder.<ETyped>get().withEBase(eTyped1).withNext(relAsg).build();

        Start start = new Start();
        start.seteNum(0);
        start.setNext(1);
        AsgEBase<Start> startAsg = AsgEBase.Builder.<Start>get().withEBase(start).withNext(eTypedAsg1).build();

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
        AsgEBase<ETyped> eTypedAsg = AsgEBase.Builder.<ETyped>get().withEBase(eTyped).build();

        Rel rel = new Rel();
        rel.seteNum(2);
        rel.setDir(Rel.Direction.R);
        rel.setrType(1);
        AsgEBase<Rel> relAsg = AsgEBase.Builder.<Rel>get().withEBase(rel).withNext(eTypedAsg).build();

        EConcrete concrete = new EConcrete();
        concrete.seteNum(1);
        concrete.seteTag("A");
        concrete.seteID("12345678");
        concrete.seteType(1); //Person
        concrete.seteName("Moshe Ufnik");
        AsgEBase<EConcrete> concreteAsg1 = AsgEBase.Builder.<EConcrete>get().withEBase(concrete).withNext(relAsg).build();

        Start start = new Start();
        start.seteNum(0);
        start.setNext(1);
        AsgEBase<Start> startAsg = AsgEBase.Builder.<Start>get().withEBase(start).withNext(concreteAsg1).build();


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
        AsgEBase<EUntyped> unTypedAsg = AsgEBase.Builder.<EUntyped>get().withEBase(untyped).build();

        Rel rel = new Rel();
        rel.seteNum(2);
        rel.setDir(Rel.Direction.R);
        rel.setrType(1);
        AsgEBase<Rel> relAsg = AsgEBase.Builder.<Rel>get().withEBase(rel).withNext(unTypedAsg).build();

        EConcrete concrete = new EConcrete();
        concrete.seteNum(1);
        concrete.seteTag("A");
        concrete.seteID("12345678");
        concrete.seteType(1); //Person
        concrete.seteName("Moshe Ufnik");
        AsgEBase<EConcrete> concreteAsg = AsgEBase.Builder.<EConcrete>get().withEBase(concrete).withNext(relAsg).build();

        Start start = new Start();
        start.seteNum(0);
        start.setNext(1);
        AsgEBase<Start> startAsg = AsgEBase.Builder.<Start>get().withEBase(start).withNext(concreteAsg).build();

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
        untyped.seteTag("C");
        AsgEBase<EUntyped> unTypedAsg = AsgEBase.Builder.<EUntyped>get().withEBase(untyped).build();


        Rel rel2 = new Rel();
        rel2.seteNum(4);
        rel2.setDir(Rel.Direction.R);
        rel2.setrType(1);
        AsgEBase<Rel> rel2Asg = AsgEBase.Builder.<Rel>get().withEBase(rel2).withNext(unTypedAsg).build();

        ETyped eTyped = new ETyped();
        eTyped.seteNum(3);
        eTyped.seteTag("B");
        eTyped.seteType(2);
        AsgEBase<ETyped> eTypedAsg = AsgEBase.Builder.<ETyped>get().withEBase(eTyped).withNext(rel2Asg).build();

        Rel rel1 = new Rel();
        rel1.seteNum(2);
        rel1.setDir(Rel.Direction.R);
        rel1.setrType(1);
        AsgEBase<Rel> relAsg = AsgEBase.Builder.<Rel>get().withEBase(rel1).withNext(eTypedAsg).build();

        EConcrete concrete = new EConcrete();
        concrete.seteNum(1);
        concrete.seteTag("A");
        concrete.seteID("12345678");
        concrete.seteType(1); //Person
        concrete.seteName("Moshe Ufnik");
        AsgEBase<EConcrete> concreteAsg = AsgEBase.Builder.<EConcrete>get().withEBase(concrete).withNext(relAsg).build();

        Start start = new Start();
        start.seteNum(0);
        start.setNext(1);
        AsgEBase<Start> startAsg = AsgEBase.Builder.<Start>get().withEBase(start).withNext(concreteAsg).build();

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

        return new Plan(ops);
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
                            .withEType(2).withName("dragon").build());
                    return  entityTypes;
                }
        );
        when(ontology.getRelationshipTypes()).thenAnswer(invocationOnMock ->
                {
                    ArrayList<RelationshipType> relTypes = new ArrayList<>();
                    relTypes.add(RelationshipType.Builder.get()
                            .withRType(1).withName("Fire").build());
                    return  relTypes;
                }
        );
        return ontology;
    }

}
