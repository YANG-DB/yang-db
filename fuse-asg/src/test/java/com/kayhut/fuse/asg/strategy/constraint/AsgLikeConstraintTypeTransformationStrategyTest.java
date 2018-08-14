package com.kayhut.fuse.asg.strategy.constraint;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kayhut.fuse.model.asgQuery.AsgQueryUtil;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import com.kayhut.fuse.model.query.properties.constraint.ConstraintOp;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.*;
import static com.kayhut.fuse.model.query.quant.QuantType.all;
import static org.junit.Assert.*;

/**
 * Created by benishue on 09-May-17.
 */
public class AsgLikeConstraintTypeTransformationStrategyTest {
    //region Setup
    @Before
    public void setUp() throws Exception {
        String ontologyExpectedJson = readJsonToString("src/test/resources/Dragons_Ontology.json");
        ont = new Ontology.Accessor(new ObjectMapper().readValue(ontologyExpectedJson, Ontology.class));

    }
    //endregion


    //region Test Methods
    @Test
    public void asgConstraintTransformationStrategyEPropsLongToDateTest() throws Exception {
        AsgStrategyContext asgStrategyContext = new AsgStrategyContext(ont);
        AsgQuery asgQueryWithEProps = Q1();
        RedundantLikeConstraintAsgStrategy asgConstraintTypeTransformationStrategy = new RedundantLikeConstraintAsgStrategy();

        //Applying the Strategy on the Eprop with the Epoch time
        asgConstraintTypeTransformationStrategy.apply(asgQueryWithEProps, asgStrategyContext);
        assertTrue(((EPropGroup) AsgQueryUtil.element(asgQueryWithEProps, 3).get().geteBase()).getProps().isEmpty());

        asgStrategyContext = new AsgStrategyContext(ont);
        asgQueryWithEProps = Q2();
        //Applying the Strategy on the Eprop with the Epoch time
        asgConstraintTypeTransformationStrategy.apply(asgQueryWithEProps, asgStrategyContext);
        assertEquals(((EPropGroup) AsgQueryUtil.element(asgQueryWithEProps, 3).get().geteBase()).getProps().get(0).getCon().getExpr().toString(), "a");

        asgStrategyContext = new AsgStrategyContext(ont);
        asgQueryWithEProps = Q3();
        //Applying the Strategy on the Eprop with the Epoch time
        asgConstraintTypeTransformationStrategy.apply(asgQueryWithEProps, asgStrategyContext);
        assertFalse(((EPropGroup) AsgQueryUtil.element(asgQueryWithEProps, 3).get().geteBase()).getProps().isEmpty());

        asgStrategyContext = new AsgStrategyContext(ont);
        asgQueryWithEProps = Q4();
        //Applying the Strategy on the Eprop with the Epoch time
        asgConstraintTypeTransformationStrategy.apply(asgQueryWithEProps, asgStrategyContext);
        assertEquals(((EPropGroup) AsgQueryUtil.element(asgQueryWithEProps, 3).get().geteBase()).getProps().get(0).getCon().getExpr().toString(), "b*");

        asgStrategyContext = new AsgStrategyContext(ont);
        asgQueryWithEProps = Q5();
        //Applying the Strategy on the Eprop with the Epoch time
        asgConstraintTypeTransformationStrategy.apply(asgQueryWithEProps, asgStrategyContext);
        assertTrue(((EPropGroup) AsgQueryUtil.element(asgQueryWithEProps, 3).get().geteBase()).getProps().isEmpty());

    }

    private AsgQuery Q1() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "ont")
                .next(typed(1, "Person", "A"))
                .next(quant1(2, all))
                .in(ePropGroup(3, EProp.of(3, "name", Constraint.of(ConstraintOp.like, "*"))))
                .build();
        return asgQuery;
    }

    private AsgQuery Q2() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "ont")
                .next(typed(1, "Person", "A"))
                .next(quant1(2, all))
                .in(ePropGroup(3, EProp.of(3, "name", Constraint.of(ConstraintOp.like, "a")), EProp.of(3, "name", Constraint.of(ConstraintOp.like, "*"))))
                .build();
        return asgQuery;
    }

    private AsgQuery Q3() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "ont")
                .next(typed(1, "Person", "A"))
                .next(quant1(2, all))
                .in(ePropGroup(3, EProp.of(3, "name", Constraint.of(ConstraintOp.like, "a")), EProp.of(3, "name", Constraint.of(ConstraintOp.like, "b*"))))
                .build();
        return asgQuery;
    }

    private AsgQuery Q4() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "ont")
                .next(typed(1, "Person", "A"))
                .next(quant1(2, all))
                .in(ePropGroup(3, EProp.of(3, "name", Constraint.of(ConstraintOp.likeAny, Arrays.asList("a", "*", "b"))), EProp.of(3, "name", Constraint.of(ConstraintOp.like, "b*"))))
                .build();
        return asgQuery;
    }

    private AsgQuery Q5() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "ont")
                .next(typed(1, "Person", "A"))
                .next(quant1(2, all))
                .in(ePropGroup(3, EProp.of(3, "name", Constraint.of(ConstraintOp.likeAny, Arrays.asList("a", "*", "b"))), EProp.of(3, "name", Constraint.of(ConstraintOp.like, "***"))))
                .build();
        return asgQuery;
    }


    //endregion

    //region Private Methods
    private static String readJsonToString(String jsonRelativePath) throws Exception {
        String contents = "";
        try {
            contents = new String(Files.readAllBytes(Paths.get(jsonRelativePath)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contents;
    }
    //endregion

    //region Fields
    private Ontology.Accessor ont;
    //endregion

}