package com.kayhut.fuse.asg.strategy.type;

import com.kayhut.fuse.asg.strategy.AsgStrategyContext;
import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.model.OntologyTestUtils;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.entity.EUntyped;
import com.kayhut.fuse.model.query.properties.RelProp;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.Date;

import static com.kayhut.fuse.model.OntologyTestUtils.START_DATE;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.*;
import static com.kayhut.fuse.model.query.Constraint.of;
import static com.kayhut.fuse.model.query.ConstraintOp.eq;
import static com.kayhut.fuse.model.query.Rel.Direction.R;

/**
 * Created by benishue on 24-Apr-17.
 */
public class AsgUntypedInferTypeLeftSideRelationStrategyTest {
    //This Eprop is not under an AND quantifier and should be replaced by the EPropGroup Element -  e.g. Q3 on V1


    @Test
    public void testUntypedToTypedStrategy() throws Exception {
        Ontology ontology = OntologyTestUtils.createDragonsOntologyLong();
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragon")
                .next(unTyped(1))
                .next(rel(2, OntologyTestUtils.OWN.type, R).below(relProp(10,
                        RelProp.of(START_DATE.type, 10, of(eq, new Date())))))
                .next(typed(3, OntologyTestUtils.DRAGON.type))
                .build();

        AsgEBase<EUntyped> next = AsgQueryUtil
                .element$(query, EUntyped.class);
        Assert.assertEquals(0, next.geteBase().getvTypes().size());

        AsgUntypedInferTypeLeftSideRelationStrategy strategy = new AsgUntypedInferTypeLeftSideRelationStrategy();
        strategy.apply(query,new AsgStrategyContext(ontology));

        AsgEBase<EUntyped> after = AsgQueryUtil.<EUntyped>elements(query, EUntyped.class).iterator().next();
        Assert.assertEquals(after.geteBase().getvTypes(), Collections.singletonList(OntologyTestUtils.PERSON.type));



    }

    @Test
    public void testUntypedToConcreteStrategy() throws Exception {
        Ontology ontology = OntologyTestUtils.createDragonsOntologyLong();
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragon")
                .next(unTyped(1))
                .next(rel(2, OntologyTestUtils.OWN.type, R).below(relProp(10,
                        RelProp.of(START_DATE.type, 10, of(eq, new Date())))))
                .next(concrete(3, "123",OntologyTestUtils.DRAGON.type,"",""))
                .build();

        AsgEBase<EUntyped> next = AsgQueryUtil.element$(query, EUntyped.class);
        Assert.assertEquals(0, next.geteBase().getvTypes().size());

        AsgUntypedInferTypeLeftSideRelationStrategy strategy = new AsgUntypedInferTypeLeftSideRelationStrategy();
        strategy.apply(query,new AsgStrategyContext(ontology));

        AsgEBase<EUntyped> after = AsgQueryUtil.<EUntyped>elements(query, EUntyped.class).iterator().next();
        Assert.assertEquals(after.geteBase().getvTypes(), Collections.singletonList(OntologyTestUtils.PERSON.type));



    }

    @Test
    public void testUntypedToUntypedStrategy() throws Exception {
        Ontology ontology = OntologyTestUtils.createDragonsOntologyLong();
        AsgQuery query = AsgQuery.Builder.start("Q1", "Dragon")
                .next(unTyped(1))
                .next(rel(2, OntologyTestUtils.OWN.type, R).below(relProp(10,
                        RelProp.of(START_DATE.type, 10, of(eq, new Date())))))
                .next(unTyped(3))
                .build();

        AsgEBase<EUntyped> next = AsgQueryUtil.element$(query, EUntyped.class);
        Assert.assertEquals(0, next.geteBase().getvTypes().size());

        AsgUntypedInferTypeLeftSideRelationStrategy strategy = new AsgUntypedInferTypeLeftSideRelationStrategy();
        strategy.apply(query,new AsgStrategyContext(ontology));

        AsgEBase<EUntyped> after = AsgQueryUtil.<EUntyped>elements(query, EUntyped.class).iterator().next();
        Assert.assertEquals(after.geteBase().getvTypes(), Collections.singletonList(OntologyTestUtils.PERSON.type));



    }
}