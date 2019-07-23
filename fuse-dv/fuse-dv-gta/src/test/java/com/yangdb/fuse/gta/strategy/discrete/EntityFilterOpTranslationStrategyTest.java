package com.yangdb.fuse.gta.strategy.discrete;

import com.yangdb.fuse.model.asgQuery.AsgQueryUtil;
import com.yangdb.fuse.gta.strategy.common.EntityTranslationOptions;
import com.yangdb.fuse.dispatcher.gta.TranslationContext;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.execution.plan.*;
import com.yangdb.fuse.model.execution.plan.composite.Plan;
import com.yangdb.fuse.model.execution.plan.entity.EntityFilterOp;
import com.yangdb.fuse.model.execution.plan.entity.EntityOp;
import com.yangdb.fuse.model.execution.plan.relation.RelationOp;
import com.yangdb.fuse.model.ontology.EntityType;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.ontology.Property;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.entity.EEntityBase;
import com.yangdb.fuse.model.query.properties.EProp;
import com.yangdb.fuse.model.query.properties.EPropGroup;
import com.yangdb.fuse.model.query.properties.RelProp;
import com.yangdb.fuse.unipop.controller.promise.GlobalConstants;
import com.yangdb.fuse.unipop.promise.Constraint;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import com.yangdb.fuse.unipop.process.traversal.dsl.graph.__;

import org.apache.tinkerpop.gremlin.structure.T;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;

import static com.yangdb.fuse.model.asgQuery.AsgQuery.Builder.*;
import static com.yangdb.fuse.model.query.properties.constraint.Constraint.of;
import static com.yangdb.fuse.model.query.properties.constraint.ConstraintOp.eq;
import static com.yangdb.fuse.model.query.Rel.Direction.R;
import static com.yangdb.fuse.model.query.quant.QuantType.all;
import static org.mockito.Mockito.when;

/**
 * Created by Roman on 10/05/2017.
 */
public class EntityFilterOpTranslationStrategyTest {

    static AsgQuery simpleQuery2(String queryName, String ontologyName) {
        return AsgQuery.Builder.start(queryName, ontologyName)
                .next(typed(1, "1", "A"))
                .next(rel(2, "1", R).below(relProp(10, RelProp.of(10, "2", of(eq, "value2")))))
                .next(typed(3, "2", "B"))
                .next(quant1(4, all))
                .in(ePropGroup(9, EProp.of(9, "1", of(eq, "value1")), EProp.of(9, "2", of(eq, 30)))
                        , rel(5, "4", R)
                                .next(unTyped(6, "C"))
                        , rel(7, "5", R)
                                .below(relProp(11, RelProp.of(11, "5", of(eq, "value5")), RelProp.of(11, "4", of(eq, "value4"))))
                                .next(concrete(8, "concrete1", "3", "Concrete1", "D"))
                )
                .build();
    }


    @Test
    public void test_entity3_filter9() throws Exception {
        AsgQuery query = simpleQuery2("name", "ontName");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(query, 3).get()),
                new EntityFilterOp(AsgQueryUtil.<EPropGroup>element(query, 9).get())
        );

        Ontology ontology = Mockito.mock(Ontology.class);
        when(ontology.getEntityTypes()).thenAnswer(invocationOnMock ->
                {
                    ArrayList<EntityType> entityTypes = new ArrayList<>();
                    entityTypes.add(EntityType.Builder.get()
                            .withEType("2").withName("Person").build());
                    return  entityTypes;
                }
        );
        when(ontology.getProperties()).then(invocationOnMock -> {
            Property nameProperty = new Property();
            nameProperty.setpType("1");
            nameProperty.setName("name");
            nameProperty.setType("string");

            Property ageProperty = new Property();
            ageProperty.setpType("2");
            ageProperty.setName("age");
            ageProperty.setType("int");

            return Arrays.asList(nameProperty, ageProperty);
        });

        TranslationContext context = Mockito.mock(TranslationContext.class);
        when(context.getOnt()).thenAnswer(invocationOnMock -> new Ontology.Accessor(ontology));

        EntityFilterOpTranslationStrategy strategy = new EntityFilterOpTranslationStrategy(EntityTranslationOptions.none);
        GraphTraversal actualTraversal = strategy.translate(
                __.start().has("willBeDeleted", "doesnt matter"),
                new PlanWithCost<>(plan, null),
                plan.getOps().get(1),
                context);

        GraphTraversal expectedTraversal = __.start()
                .has(GlobalConstants.HasKeys.CONSTRAINT,
                        Constraint.by(__.start().and(
                                __.start().has(T.label, "Person"),
                                __.start().and(__.start().has("name", "value1"),
                                       __.start().has("age", 30)))));

        Assert.assertEquals(expectedTraversal, actualTraversal);
    }

    @Test
    public void test_entity1_rel2_entity3_filter9() {
        AsgQuery query = simpleQuery2("name", "ontName");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>element(query, 1).get()),
                new RelationOp(AsgQueryUtil.<Rel>element(query, 2).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>element(query, 3).get()),
                new EntityFilterOp(AsgQueryUtil.<EPropGroup>element(query, 9).get())
        );

        Ontology ontology = Mockito.mock(Ontology.class);
        when(ontology.getEntityTypes()).thenAnswer(invocationOnMock ->
                {
                    ArrayList<EntityType> entityTypes = new ArrayList<>();
                    entityTypes.add(EntityType.Builder.get()
                            .withEType("2").withName("Person").build());
                    return  entityTypes;
                }
        );
        when(ontology.getProperties()).then(invocationOnMock -> {
            Property nameProperty = new Property();
            nameProperty.setpType("1");
            nameProperty.setName("name");
            nameProperty.setType("string");

            Property ageProperty = new Property();
            ageProperty.setpType("2");
            ageProperty.setName("age");
            ageProperty.setType("int");

            return Arrays.asList(nameProperty, ageProperty);
        });

        TranslationContext context = Mockito.mock(TranslationContext.class);
        when(context.getOnt()).thenAnswer(invocationOnMock -> new Ontology.Accessor(ontology));

        EntityFilterOpTranslationStrategy strategy = new EntityFilterOpTranslationStrategy(EntityTranslationOptions.none);

        GraphTraversal actualTraversal = strategy.translate(__.start(), new PlanWithCost<>(plan, null), plan.getOps().get(3), context);
        GraphTraversal expectedTraversal = __.start()
                .outE(GlobalConstants.Labels.PROMISE_FILTER)
                .has(GlobalConstants.HasKeys.CONSTRAINT,
                        Constraint.by(__.start().and(
                                __.start().has("name", "value1"),
                                __.start().has("age", 30))))
                .otherV().as("B");

        Assert.assertEquals(expectedTraversal, actualTraversal);
    }
}
