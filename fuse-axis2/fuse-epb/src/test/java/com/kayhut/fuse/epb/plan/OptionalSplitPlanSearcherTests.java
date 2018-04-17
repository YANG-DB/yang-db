package com.kayhut.fuse.epb.plan;

import com.kayhut.fuse.model.OntologyTestUtils;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.ontology.Value;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import com.kayhut.fuse.model.query.properties.constraint.ConstraintOp;
import org.junit.Test;

import java.util.Date;

import static com.kayhut.fuse.model.OntologyTestUtils.*;
import static com.kayhut.fuse.model.OntologyTestUtils.Gender.MALE;
import static com.kayhut.fuse.model.OntologyTestUtils.NAME;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.*;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.concrete;
import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.eProp;
import static com.kayhut.fuse.model.query.Rel.Direction.R;
import static com.kayhut.fuse.model.query.properties.RelProp.of;
import static com.kayhut.fuse.model.query.properties.constraint.ConstraintOp.*;
import static com.kayhut.fuse.model.query.quant.QuantType.all;

public class OptionalSplitPlanSearcherTests {

    private AsgQuery query(){
        return AsgQuery.Builder.start("q", "O")
                .next(typed(1, OntologyTestUtils.PERSON.type)
                        .next(eProp(2,EProp.of(3, HEIGHT.type, Constraint.of(ConstraintOp.gt, 189)))))
                .next(rel(4, OWN.getrType(), R)
                        .below(relProp(5, of(6, START_DATE.type, Constraint.of(eq, new Date())))))
                .next(typed(7, OntologyTestUtils.DRAGON.type))
                .next(quant1(8, all))
                .in(eProp(9, EProp.of(10, NAME.type, Constraint.of(eq, "smith")), EProp.of(11, GENDER.type, Constraint.of(gt, new Value(MALE.ordinal(),MALE.name()))))
                        , optional(50).next(rel(12, FREEZE.getrType(), R)
                                .next(unTyped(13)
                                        .next(eProp(14,EProp.of(15, NAME.type, Constraint.of(ConstraintOp.notContains, "bob"))))
                                ))
                        , rel(16, FIRE.getrType(), R)
                        .next(concrete(20, "smoge", DRAGON.type, "Display:smoge", "D")
                            .next(eProp(21,EProp.of(22, NAME.type, Constraint.of(ConstraintOp.eq, "smoge"))))
                        )
                )
                .build();
    }

    @Test
    public void test(){

        OptionalSplitPlanSearcher planSearcher = new OptionalSplitPlanSearcher(null, null);
        AsgQuery query = query();
        planSearcher.search(query);


    }
}
