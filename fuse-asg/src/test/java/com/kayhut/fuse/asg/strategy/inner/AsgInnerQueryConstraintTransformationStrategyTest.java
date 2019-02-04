package com.kayhut.fuse.asg.strategy.inner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Supplier;
import com.kayhut.fuse.asg.strategy.constraint.LikeToEqTransformationAsgStrategy;
import com.kayhut.fuse.asg.strategy.constraint.RedundantLikeConstraintAsgStrategy;
import com.kayhut.fuse.dispatcher.asg.AsgQuerySupplier;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgQueryUtil;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.constraint.*;
import org.junit.Assert;
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
public class AsgInnerQueryConstraintTransformationStrategyTest {
    //region Setup
    @Before
    public void setUp() throws Exception {
        String ontologyExpectedJson = readJsonToString("src/test/resources/Dragons_Ontology.json");
        ont = new Ontology.Accessor(new ObjectMapper().readValue(ontologyExpectedJson, Ontology.class));

    }
    //endregion


    //region Test Methods
    @Test
    public void testTransformLike(){
        Supplier<AsgQuery> asgSupplier = new AsgQuerySupplier(Q1());
        AsgQuery asgQuery = asgSupplier.get();

    }

    private Query Q1() {
        Query query = Query.Builder.instance().withName("q1").withOnt("Dragons")
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "People", "Person", 2, 0),
                        new EPropGroup(2,
                                new EProp(3, "id",Constraint.of(ConstraintOp.inSet,
                                        new InnerQueryConstraint(Q2(),"P.id"))))
                )).build();
        return query;
    }

    private Query Q2() {
        Query query = Query.Builder.instance().withName("q2").withOnt("Dragons")
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "P", "Person", 2, 0),
                        new EPropGroup(2,
                                new EProp(3, "name",Constraint.of(ConstraintOp.like,"jhon*")))
                )).build();
        return query;
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