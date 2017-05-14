package com.kayhut.fuse.asg.strategy.ConstraintTransformation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kayhut.fuse.asg.AsgQueryStore;
import com.kayhut.fuse.asg.strategy.AsgStrategyContext;
import com.kayhut.fuse.asg.strategy.PropertiesGrouping.AsgRelPropertiesGroupingStrategy;
import com.kayhut.fuse.asg.util.AsgQueryUtils;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.ConstraintOp;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.model.query.properties.RelPropGroup;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;

/**
 * Created by benishue on 11-May-17.
 */
public class AsgConstraintArrayTransformationStrategyTest {

    Ontology ontology;


    @Test
    public void asgConstraintTransformationStrategyEPropsLongToDateTest() throws Exception {
        AsgQuery asgQueryWithEProps = AsgQueryStore.Q1();

        //Setting The EProp expression as a date represented by Long value
        EProp eProp = (EProp) AsgQueryUtils.getElement(asgQueryWithEProps, 4).get().geteBase();
        eProp.setpType("15"); //this is a date field - Input is long - epoch time
        eProp.getCon().setOp(ConstraintOp.inSet);
        eProp.getCon().setExpr(new long[] {1000,205555,355540,445450,587870,604564,787481,8879680,9798770,99879891}); //Epoch time as Long

        assertTrue(eProp.getCon().getExpr().getClass().isArray());

        AsgStrategyContext asgStrategyContext = new AsgStrategyContext(ontology);
        AsgConstraintArrayTransformationStrategy asgConstraintArrayTransformationStrategy = new AsgConstraintArrayTransformationStrategy();

        //Applying the Strategy on the Eprop with the Epoch time
        asgConstraintArrayTransformationStrategy.apply(asgQueryWithEProps, asgStrategyContext);
        Object expr = ((EProp) AsgQueryUtils.getElement(asgQueryWithEProps, 4).get().geteBase()).getCon().getExpr();
        assertThat(expr, instanceOf(List.class));
    }

    @Test
    public void asgConstraintTransformationStrategyRelPropsArrayToListTest() throws Exception {
        AsgQuery asgQueryWithRelProps = AsgQueryStore.Q188_V1();
        AsgQuery asgQueryWithRelPropsOriginal = AsgQueryStore.Q188_V1();

        //region Preparing the Properties for the AsgQuery
        //Setting The RelProp (enum #4) expression as a date represented by Long value
        RelProp rProp1 = (RelProp) AsgQueryUtils.getElement(asgQueryWithRelProps, 4).get().geteBase();
        rProp1.setpType("15"); //this is a date field - Input is long type - epoch time
        rProp1.getCon().setExpr(new long[] {10,20,30,40,50,60,71,80,90,91}); //Epoch time as Long
        rProp1.getCon().setOp(ConstraintOp.inRange);
        assertTrue(rProp1.getCon().getExpr().getClass().isArray());

        //Setting The RelProp (enum #5) expression
        RelProp rProp2 = (RelProp) AsgQueryUtils.getElement(asgQueryWithRelProps, 5).get().geteBase();
        rProp2.setpType("3");
        rProp2.getCon().setExpr(new String[] {"a", "b", "c"});
        rProp2.getCon().setOp(ConstraintOp.inSet);
        assertTrue(rProp2.getCon().getExpr().getClass().isArray());

        RelProp rProp3 = (RelProp) AsgQueryUtils.getElement(asgQueryWithRelPropsOriginal, 5).get().geteBase();
        rProp3.setpType("15"); //this is a date field - Input is long type - epoch time
        rProp3.getCon().setExpr(new long[] {10,20,30,40,50,60,71,80,90,91}); //Epoch time as Long
        rProp3.getCon().setOp(ConstraintOp.inRange);
        assertTrue(rProp3.getCon().getExpr().getClass().isArray());

        RelProp rProp4 = (RelProp) AsgQueryUtils.getElement(asgQueryWithRelPropsOriginal, 4).get().geteBase();
        rProp4.setpType("3");
        rProp4.getCon().setExpr(new String[] {"a", "b", "c"});
        rProp4.getCon().setOp(ConstraintOp.notInSet);
        assertTrue(rProp4.getCon().getExpr().getClass().isArray());

        //endregion

        AsgStrategyContext asgStrategyContext = new AsgStrategyContext(ontology);
        AsgConstraintArrayTransformationStrategy asgConstraintArrayTransformationStrategy = new AsgConstraintArrayTransformationStrategy();

        //Applying the Strategy on the RelProp #1 with the Epoch time
        asgConstraintArrayTransformationStrategy.apply(asgQueryWithRelProps, asgStrategyContext);
        Object expr1 = ((RelProp) AsgQueryUtils.getElement(asgQueryWithRelProps, 4).get().geteBase()).getCon().getExpr();
        Object expr2 = ((RelProp) AsgQueryUtils.getElement(asgQueryWithRelProps, 5).get().geteBase()).getCon().getExpr();
        assertThat(expr1, instanceOf(List.class));
        assertThat(expr2, instanceOf(List.class));
        assertThat(((ArrayList)expr1).get(0), instanceOf(Date.class));
        assertThat(((ArrayList)expr2).get(0), instanceOf(String.class));

        //Appling First the Properties Grouping Startegy and then applying the constraint transformation strategy
        //We want to be sure that the order of strategies is not affecting the final result
        AsgRelPropertiesGroupingStrategy asgRelPropertiesGroupingStrategy = new AsgRelPropertiesGroupingStrategy();
        asgRelPropertiesGroupingStrategy.apply(asgQueryWithRelPropsOriginal, new AsgStrategyContext());
        expr1 = ((RelPropGroup) AsgQueryUtils.getElement(asgQueryWithRelPropsOriginal, 4).get().geteBase()).getrProps().get(0).getCon().getExpr();
        expr2 = ((RelPropGroup) AsgQueryUtils.getElement(asgQueryWithRelPropsOriginal, 4).get().geteBase()).getrProps().get(1).getCon().getExpr();
        assertTrue(expr1.getClass().isArray());
        assertTrue(expr2.getClass().isArray());

        //Checking the RelProps grouping mechanism
        AsgEBase<EBase> newRelPropGroupAsgEbase = AsgQueryUtils.getElement(asgQueryWithRelPropsOriginal, 4).get();
        assertNotNull(newRelPropGroupAsgEbase);

        //Applying again the Constraint Transformation Strategy
        asgConstraintArrayTransformationStrategy.apply(asgQueryWithRelPropsOriginal, asgStrategyContext);
        expr1 = ((RelPropGroup) AsgQueryUtils.getElement(asgQueryWithRelPropsOriginal, 4).get().geteBase()).getrProps().get(0).getCon().getExpr();
        expr2 = ((RelPropGroup) AsgQueryUtils.getElement(asgQueryWithRelPropsOriginal, 4).get().geteBase()).getrProps().get(1).getCon().getExpr();
        assertThat(expr1, instanceOf(List.class));
        assertThat(expr2, instanceOf(List.class));
        assertThat(((ArrayList)expr1).get(0), instanceOf(String.class));
        assertThat(((ArrayList)expr2).get(0), instanceOf(Date.class));

        //Checking first the Constraint Type Transformation and then the Constraint Array Transformation
        AsgQuery asgQueryWithRelProps2 = AsgQueryStore.Q188_V1();
        RelProp rProp5 = (RelProp) AsgQueryUtils.getElement(asgQueryWithRelProps2, 4).get().geteBase();
        rProp5.setpType("15"); //this is a date field - Input is long type - epoch time
        rProp5.getCon().setExpr(100L); //Epoch time as Long
        rProp5.getCon().setOp(ConstraintOp.ge);
        assertThat(rProp5.getCon().getExpr(), instanceOf(Long.class));

        //Setting The RelProp (enum #5) expression
        RelProp rProp6 = (RelProp) AsgQueryUtils.getElement(asgQueryWithRelProps2, 5).get().geteBase();
        rProp6.setpType("3");
        rProp6.getCon().setExpr(new String[] {"a", "b", "c"});
        rProp6.getCon().setOp(ConstraintOp.inRange);
        assertTrue(rProp6.getCon().getExpr().getClass().isArray());

        AsgConstraintTypeTransformationStrategy asgConstraintTypeTransformationStrategy = new AsgConstraintTypeTransformationStrategy();
        asgConstraintTypeTransformationStrategy.apply(asgQueryWithRelProps2, asgStrategyContext);
        asgConstraintTypeTransformationStrategy.apply(asgQueryWithRelProps2, asgStrategyContext); //dsecond call to the same strategy

        Object expr5 = ((RelProp) AsgQueryUtils.getElement(asgQueryWithRelProps2, 4).get().geteBase()).getCon().getExpr();
        assertThat(expr5, instanceOf(Date.class));


        //Lets call now to the Constraint Array Transformation
        AsgConstraintArrayTransformationStrategy asgConstraintArrayTransformationStrategy1 = new AsgConstraintArrayTransformationStrategy();
        asgConstraintArrayTransformationStrategy1.apply(asgQueryWithRelProps2, asgStrategyContext);

        Object expr3 = ((RelProp) AsgQueryUtils.getElement(asgQueryWithRelProps2, 4).get().geteBase()).getCon().getExpr();
        Object expr4 = ((RelProp) AsgQueryUtils.getElement(asgQueryWithRelProps2, 5).get().geteBase()).getCon().getExpr();
        assertThat(expr3, instanceOf(Date.class));
        assertThat(expr4, instanceOf(List.class));


        RelProp rProp7 = (RelProp) AsgQueryUtils.getElement(asgQueryWithRelProps2, 5).get().geteBase();
        rProp6.setpType("15");
        rProp6.getCon().setExpr(new long[] {212121, 555557, 987654321});
        rProp6.getCon().setOp(ConstraintOp.inRange);
        assertTrue(rProp6.getCon().getExpr().getClass().isArray());
        asgConstraintArrayTransformationStrategy1.apply(asgQueryWithRelProps2, asgStrategyContext);

        Object expr8 = ((RelProp) AsgQueryUtils.getElement(asgQueryWithRelProps2, 5).get().geteBase()).getCon().getExpr();
        assertThat(expr8, instanceOf(List.class));
        assertThat(((ArrayList)expr8).get(0), instanceOf(Date.class));
    }

    @Before
    public void setUp() throws Exception {
        String ontologyExpectedJson = readJsonToString("src/test/resources/Dragons_Ontology.json");
        ontology = new ObjectMapper().readValue(ontologyExpectedJson, Ontology.class);

    }

    private static String readJsonToString(String jsonRelativePath) throws Exception {
        String contents = "";
        try {
            contents = new String(Files.readAllBytes(Paths.get(jsonRelativePath)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contents;
    }
}