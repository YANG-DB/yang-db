package com.kayhut.fuse.gta.strategy;

import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.gta.translation.TranslationContext;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.execution.plan.EntityFilterOp;
import com.kayhut.fuse.model.execution.plan.EntityOp;
import com.kayhut.fuse.model.execution.plan.Plan;
import com.kayhut.fuse.model.execution.plan.RelationOp;
import com.kayhut.fuse.model.ontology.EntityType;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.ontology.Property;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.unipop.controller.GlobalConstants;
import com.kayhut.fuse.unipop.promise.Constraint;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.T;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.*;
import static com.kayhut.fuse.model.query.Constraint.of;
import static com.kayhut.fuse.model.query.ConstraintOp.eq;
import static com.kayhut.fuse.model.query.Rel.Direction.R;
import static com.kayhut.fuse.model.query.quant.QuantType.all;
import static org.mockito.Mockito.when;

/**
 * Created by Roman on 10/05/2017.
 */
public class EntityFilterOpTranslationStrategyTest {

    static AsgQuery simpleQuery2(String queryName, String ontologyName) {
        return AsgQuery.Builder.start(queryName, ontologyName)
                .next(typed(1, 1))
                .next(rel(R, 2, 1).below(relProp(10, RelProp.of("2", 10, of(eq, "value2")))))
                .next(typed(2, 3))
                .next(quant1(4, all))
                .in(eProp(9, EProp.of("1", 9, of(eq, "value1")), EProp.of("2", 9, of(eq, 30)))
                        , rel(R, 5, 4)
                                .next(unTyped("C", 6))
                        , rel(R, 7, 5)
                                .below(relProp(11, RelProp.of("5", 11, of(eq, "value5")), RelProp.of("4", 11, of(eq, "value4"))))
                                .next(concrete("concrete1", 3, "Concrete1", "D", 8))
                )
                .build();
    }


    @Test
    public void test_entity3_filter9() throws Exception {
        AsgQuery query = simpleQuery2("name", "ontName");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>getElement(query, 3).get()),
                new EntityFilterOp(AsgQueryUtil.<EPropGroup>getElement(query, 9).get())
        );

        Ontology ontology = Mockito.mock(Ontology.class);
        when(ontology.getEntityTypes()).thenAnswer(invocationOnMock ->
                {
                    ArrayList<EntityType> entityTypes = new ArrayList<>();
                    entityTypes.add(EntityType.EntityTypeBuilder.anEntityType()
                            .withEType(2).withName("Person").build());
                    return  entityTypes;
                }
        );
        when(ontology.getProperties()).then(invocationOnMock -> {
            Property nameProperty = new Property();
            nameProperty.setpType(1);
            nameProperty.setName("name");
            nameProperty.setType("string");

            Property ageProperty = new Property();
            ageProperty.setpType(2);
            ageProperty.setName("age");
            ageProperty.setType("int");

            return Arrays.asList(nameProperty, ageProperty);
        });

        TranslationContext context = Mockito.mock(TranslationContext.class);
        when(context.getOntology()).thenAnswer( invocationOnMock -> ontology);

        EntityFilterOpTranslationStrategy strategy = new EntityFilterOpTranslationStrategy();
        GraphTraversal actualTraversal = strategy.translate(
                __.start().has("willBeDeleted", "doesnt matter"),
                plan,
                plan.getOps().get(1),
                context);

        GraphTraversal expectedTraversal = __.start()
                .has(GlobalConstants.HasKeys.CONSTRAINT,
                        Constraint.by(__.and(
                                __.has(T.label, "Person"),
                                __.has("name", "value1"),
                                __.has("age", 30))));

        Assert.assertEquals(expectedTraversal, actualTraversal);
    }

    @Test
    public void test_entity1_rel2_entity3_filter9() {
        AsgQuery query = simpleQuery2("name", "ontName");
        Plan plan = new Plan(
                new EntityOp(AsgQueryUtil.<EEntityBase>getElement(query, 1).get()),
                new RelationOp(AsgQueryUtil.<Rel>getElement(query, 2).get()),
                new EntityOp(AsgQueryUtil.<EEntityBase>getElement(query, 3).get()),
                new EntityFilterOp(AsgQueryUtil.<EPropGroup>getElement(query, 9).get())
        );

        Ontology ontology = Mockito.mock(Ontology.class);
        when(ontology.getEntityTypes()).thenAnswer(invocationOnMock ->
                {
                    ArrayList<EntityType> entityTypes = new ArrayList<>();
                    entityTypes.add(EntityType.EntityTypeBuilder.anEntityType()
                            .withEType(2).withName("Person").build());
                    return  entityTypes;
                }
        );
        when(ontology.getProperties()).then(invocationOnMock -> {
            Property nameProperty = new Property();
            nameProperty.setpType(1);
            nameProperty.setName("name");
            nameProperty.setType("string");

            Property ageProperty = new Property();
            ageProperty.setpType(2);
            ageProperty.setName("age");
            ageProperty.setType("int");

            return Arrays.asList(nameProperty, ageProperty);
        });

        TranslationContext context = Mockito.mock(TranslationContext.class);
        when(context.getOntology()).thenAnswer( invocationOnMock -> ontology);

        EntityFilterOpTranslationStrategy strategy = new EntityFilterOpTranslationStrategy();
        GraphTraversal actualTraversal = strategy.translate(__.start(), plan, plan.getOps().get(3), context);
        GraphTraversal expectedTraversal = __.start()
                .outE(GlobalConstants.Labels.PROMISE_FILTER)
                .has(GlobalConstants.HasKeys.CONSTRAINT,
                        Constraint.by(__.and(
                                __.has("name", "value1"),
                                __.has("age", 30))))
                .otherV();

        Assert.assertEquals(expectedTraversal, actualTraversal);
    }
}
