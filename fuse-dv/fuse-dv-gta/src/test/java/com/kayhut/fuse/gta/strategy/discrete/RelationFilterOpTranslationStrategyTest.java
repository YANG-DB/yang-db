package com.kayhut.fuse.gta.strategy.discrete;

import com.kayhut.fuse.model.asgQuery.AsgQueryUtil;
import com.kayhut.fuse.dispatcher.gta.TranslationContext;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.model.execution.plan.relation.RelationFilterOp;
import com.kayhut.fuse.model.execution.plan.relation.RelationOp;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.ontology.Property;
import com.kayhut.fuse.model.ontology.RelationshipType;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.model.query.properties.RelPropGroup;
import com.kayhut.fuse.unipop.controller.promise.GlobalConstants;
import com.kayhut.fuse.unipop.promise.Constraint;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import com.kayhut.fuse.unipop.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.T;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.*;
import static com.kayhut.fuse.model.query.properties.constraint.Constraint.of;
import static com.kayhut.fuse.model.query.properties.constraint.ConstraintOp.eq;
import static com.kayhut.fuse.model.query.properties.constraint.ConstraintOp.gt;
import static com.kayhut.fuse.model.query.Rel.Direction.R;
import static com.kayhut.fuse.model.query.quant.QuantType.all;
import static com.kayhut.fuse.model.query.quant.QuantType.some;
import static org.mockito.Mockito.when;

/**
 * Created by Roman on 10/05/2017.
 */
public class RelationFilterOpTranslationStrategyTest {
    static AsgQuery simpleQuery2(String queryName, String ontologyName) {
        return AsgQuery.Builder.start(queryName, ontologyName)
                .next(typed(1, "1"))
                .next(rel(2, "1", R).below(relProp(10, RelProp.of(10, "2", of(gt, 10)))))
                .next(typed(3, "2"))
                .next(quant1(4, all))
                .in(eProp(9, EProp.of(9, "1", of(eq, "value1")), EProp.of(9, "2", of(eq, 30)))
                        , rel(5, "4", R)
                                .next(unTyped(6, "C"))
                        , rel(7, "5", R)
                                .below(relProp(11, RelProp.of(11, "5", of(eq, "value5")), RelProp.of(11, "4", of(eq, "value4"))))
                                .next(concrete(8, "concrete1", "3", "Concrete1", "D"))
                )
                .build();
    }

    static AsgQuery simpleQueryOr(String queryName, String ontologyName) {

        return AsgQuery.Builder.start(queryName, ontologyName)
                .next(typed(1, "1"))
                .next(rel(2, "1", R).below(relPropGroup(3, some, RelProp.of(4, "4", of(gt, 10)), RelProp.of(5, "5", of(gt, 10)))))
                .build();
    }

    static AsgQuery simpleQueryAnd(String queryName, String ontologyName) {
        return AsgQuery.Builder.start(queryName, ontologyName)
                .next(typed(1, "1"))
                .next(rel(2, "1", R).below(relPropGroup(3, all, RelProp.of(4, "4", of(gt, 10)), RelProp.of(5, "5", of(gt, 10)))))
                .build();
    }

    @Test
    public void test_rel2_filter10() {
        AsgQuery query = simpleQuery2("name", "ontName");
        Plan plan = new Plan(
                new RelationOp(AsgQueryUtil.<Rel>element(query, 2).get()),
                new RelationFilterOp(AsgQueryUtil.<RelPropGroup>element(query, 10).get())
        );

        Ontology ontology = Mockito.mock(Ontology.class);
        when(ontology.getRelationshipTypes()).thenAnswer(invocationOnMock ->
                {
                    ArrayList<RelationshipType> relTypes = new ArrayList<>();
                    relTypes.add(RelationshipType.Builder.get()
                            .withRType("1").withName("Fire").build());
                    return  relTypes;
                }
        );

        when(ontology.getProperties()).then(invocationOnMock -> {
            Property timestampProperty = new Property();
            timestampProperty.setpType("2");
            timestampProperty.setName("timestamp");
            timestampProperty.setType("int");

            return Collections.singletonList(timestampProperty);
        });

        TranslationContext context = Mockito.mock(TranslationContext.class);
        when(context.getOnt()).thenAnswer(invocationOnMock -> new Ontology.Accessor(ontology));

        RelationFilterOpTranslationStrategy strategy = new RelationFilterOpTranslationStrategy();
        GraphTraversal actualTraversal = strategy.translate(
                __.start().has("willBeDeleted", "doesnt matter"),
                new PlanWithCost<>(plan, null),
                plan.getOps().get(1),
                context);

        GraphTraversal expectedTraversal = __.start()
                .has(GlobalConstants.HasKeys.CONSTRAINT,
                        Constraint.by(__.start().and(
                                __.start().has(T.label, "Fire"),
                                __.start().has("timestamp", P.gt(10)))));

        Assert.assertEquals(expectedTraversal, actualTraversal);
    }


    @Test
    public void test_rel_or(){
        GraphTraversal expectedTraversal = __.start()
                .has(GlobalConstants.HasKeys.CONSTRAINT,
                        Constraint.by(__.start().and(
                                __.start().has(T.label, "Fire"),
                                __.start().or(__.start().has("timestamp", P.gt(10)), __.start().has("hour", P.gt(10)))
                        )));
        test_rel_group_inner(simpleQueryOr("name", "ontName"), expectedTraversal);
    }

    @Test
    public void test_rel_and(){
        GraphTraversal expectedTraversal = __.start()
                .has(GlobalConstants.HasKeys.CONSTRAINT,
                        Constraint.by(__.start().and(
                                __.start().has(T.label, "Fire"),
                                __.start().and(__.start().has("timestamp", P.gt(10)), __.start().has("hour", P.gt(10)))
                        )));
        test_rel_group_inner(simpleQueryAnd("name", "ontName"), expectedTraversal);
    }

    private void test_rel_group_inner(AsgQuery query, GraphTraversal expectedTraversal) {

        Plan plan = new Plan(
                new RelationOp(AsgQueryUtil.<Rel>element(query, 2).get()),
                new RelationFilterOp(AsgQueryUtil.<RelPropGroup>element(query, 3).get())
        );

        Ontology ontology = Mockito.mock(Ontology.class);
        when(ontology.getRelationshipTypes()).thenAnswer(invocationOnMock ->
                {
                    ArrayList<RelationshipType> relTypes = new ArrayList<>();
                    relTypes.add(RelationshipType.Builder.get()
                            .withRType("1").withName("Fire").build());
                    return  relTypes;
                }
        );

        when(ontology.getProperties()).then(invocationOnMock -> {
            Property timestampProperty = new Property();
            timestampProperty.setpType("4");
            timestampProperty.setName("timestamp");
            timestampProperty.setType("int");

            Property hourProperty = new Property();
            hourProperty.setpType("5");
            hourProperty.setName("hour");
            hourProperty.setType("int");


            return Arrays.asList(timestampProperty, hourProperty);
        });



        TranslationContext context = Mockito.mock(TranslationContext.class);
        when(context.getOnt()).thenAnswer(invocationOnMock -> new Ontology.Accessor(ontology));

        RelationFilterOpTranslationStrategy strategy = new RelationFilterOpTranslationStrategy();
        GraphTraversal actualTraversal = strategy.translate(
                __.start().has("willBeDeleted", "doesnt matter"),
                new PlanWithCost<>(plan, null),
                plan.getOps().get(1),
                context);



        Assert.assertEquals(expectedTraversal, actualTraversal);
    }

    @Test
    public void test_reversed_rel2_filter10() {
        AsgQuery query = simpleQuery2("name", "ontName");
        Plan plan = new Plan(
                new RelationOp(AsgQueryUtil.reverse(AsgQueryUtil.<Rel>element(query, 2).get())),
                new RelationFilterOp(AsgQueryUtil.<RelPropGroup>element(query, 10).get())
        );

        Ontology ontology = Mockito.mock(Ontology.class);
        when(ontology.getRelationshipTypes()).thenAnswer(invocationOnMock ->
                {
                    ArrayList<RelationshipType> relTypes = new ArrayList<>();
                    relTypes.add(RelationshipType.Builder.get()
                            .withRType("1").withName("Fire").build());
                    return  relTypes;
                }
        );

        when(ontology.getProperties()).then(invocationOnMock -> {
            Property timestampProperty = new Property();
            timestampProperty.setpType("2");
            timestampProperty.setName("timestamp");
            timestampProperty.setType("int");

            return Collections.singletonList(timestampProperty);
        });

        TranslationContext context = Mockito.mock(TranslationContext.class);
        when(context.getOnt()).thenAnswer(invocationOnMock -> new Ontology.Accessor(ontology));

        RelationFilterOpTranslationStrategy strategy = new RelationFilterOpTranslationStrategy();
        GraphTraversal actualTraversal = strategy.translate(
                __.start().has("willBeDeleted", "doesnt matter"),
                new PlanWithCost<>(plan, null),
                plan.getOps().get(1),
                context);

        GraphTraversal expectedTraversal = __.start()
                .has(GlobalConstants.HasKeys.CONSTRAINT,
                        Constraint.by(__.start().and(
                                __.start().has(T.label, "Fire"),
                                __.start().has("timestamp", P.gt(10)))));

        Assert.assertEquals(expectedTraversal, actualTraversal);
    }
}
