package com.kayhut.fuse.asg.strategy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kayhut.fuse.asg.AsgQueryStore;
import com.kayhut.fuse.asg.strategy.ConstraintTransformation.AsgConstraintTransformationStrategy;
import com.kayhut.fuse.asg.strategy.PropertiesGrouping.AsgRelPropertiesGroupingStrategy;
import com.kayhut.fuse.asg.util.AsgQueryUtils;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.model.query.properties.RelPropGroup;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;

/**
 * Created by benishue on 09-May-17.
 */
public class AsgConstraintTransformationStrategyTest {

    Ontology ontology;
    @Test
    public void asgConstraintTransformationStrategyEPropsLongToDateTest() throws Exception {
        AsgQuery asgQueryWithEProps = AsgQueryStore.Q1();

        //Setting The EProp expression as a date represented by Long value
        EProp eProp = (EProp) AsgQueryUtils.getElement(asgQueryWithEProps, 4).get().geteBase();
        eProp.setpType("15"); //this is a date field - Input is long - epoch time
        eProp.getCon().setExpr(123456L); //Epoch time as Long

        assertThat(eProp.getCon().getExpr(), instanceOf(Long.class));

        AsgStrategyContext asgStrategyContext = new AsgStrategyContext(ontology);
        AsgConstraintTransformationStrategy asgConstraintTransformationStrategy = new AsgConstraintTransformationStrategy();

        //Applying the Strategy on the Eprop with the Epoch time
        asgConstraintTransformationStrategy.apply(asgQueryWithEProps, asgStrategyContext);
        Object expr = ((EProp) AsgQueryUtils.getElement(asgQueryWithEProps, 4).get().geteBase()).getCon().getExpr();
        assertThat(expr, instanceOf(Date.class));
    }

    @Test
    public void asgConstraintTransformationStrategyEPropsIntToDateTest() throws Exception {
        AsgQuery asgQueryWithEProps = AsgQueryStore.Q1();

        //Setting The EProp expression as a date represented by Long value
        EProp eProp = (EProp) AsgQueryUtils.getElement(asgQueryWithEProps, 4).get().geteBase();
        eProp.setpType("15"); //this is a date field - Input is long - epoch time
        eProp.getCon().setExpr(123456); //Epoch time as Long

        assertThat(eProp.getCon().getExpr(), instanceOf(int.class));

        AsgStrategyContext asgStrategyContext = new AsgStrategyContext(ontology);
        AsgConstraintTransformationStrategy asgConstraintTransformationStrategy = new AsgConstraintTransformationStrategy();

        //Applying the Strategy on the Eprop with the Epoch time
        asgConstraintTransformationStrategy.apply(asgQueryWithEProps, asgStrategyContext);
        Object expr = ((EProp) AsgQueryUtils.getElement(asgQueryWithEProps, 4).get().geteBase()).getCon().getExpr();
        assertThat(expr, instanceOf(Date.class));
    }

    @Test
    public void asgConstraintTransformationStrategyEPropsIntToLongTest() throws Exception {
        AsgQuery asgQueryWithEProps = AsgQueryStore.Q1();

        //Setting The EProp expression as an int
        EProp eProp = (EProp) AsgQueryUtils.getElement(asgQueryWithEProps, 4).get().geteBase();
        eProp.setpType("7"); //this is an int field
        eProp.getCon().setExpr(99);

        assertThat(eProp.getCon().getExpr(), instanceOf(Integer.class));


        AsgStrategyContext asgStrategyContext = new AsgStrategyContext(ontology);
        AsgConstraintTransformationStrategy asgConstraintTransformationStrategy = new AsgConstraintTransformationStrategy();

        //Applying the Strategy on the Eprop with the Epoch time
        asgConstraintTransformationStrategy.apply(asgQueryWithEProps, asgStrategyContext);
        Object expr = ((EProp) AsgQueryUtils.getElement(asgQueryWithEProps, 4).get().geteBase()).getCon().getExpr();
        assertThat(expr, instanceOf(long.class));
    }

    @Test
    public void asgConstraintTransformationStrategyRelPropsLongToDateTest() throws Exception {
        AsgQuery asgQueryWithRelProps = AsgQueryStore.Q188_V1();
        AsgQuery asgQueryWithRelPropsOriginal = AsgQueryStore.Q188_V1();

        //region Preparing the Properties for the AsgQuery
        //Setting The RelProp (enum #4) expression as a date represented by Long value
        RelProp rProp1 = (RelProp) AsgQueryUtils.getElement(asgQueryWithRelProps, 4).get().geteBase();
        rProp1.setpType("15"); //this is a date field - Input is long type - epoch time
        rProp1.getCon().setExpr(123456L); //Epoch time as Long
        assertThat(rProp1.getCon().getExpr(), instanceOf(Long.class));

        //Setting The RelProp (enum #5) expression as a date represented by Long value
        RelProp rProp2 = (RelProp) AsgQueryUtils.getElement(asgQueryWithRelProps, 5).get().geteBase();
        rProp2.setpType("15"); //this is a date field - Input is long type - epoch time
        rProp2.getCon().setExpr(5555L); //Epoch time as Long
        assertThat(rProp2.getCon().getExpr(), instanceOf(Long.class));



        RelProp rProp3 = (RelProp) AsgQueryUtils.getElement(asgQueryWithRelPropsOriginal, 5).get().geteBase();
        rProp3.setpType("15"); //this is a date field - Input is long type - epoch time
        rProp3.getCon().setExpr(5555L); //Epoch time as Long
        assertThat(rProp3.getCon().getExpr(), instanceOf(Long.class));

        RelProp rProp4 = (RelProp) AsgQueryUtils.getElement(asgQueryWithRelPropsOriginal, 4).get().geteBase();
        rProp4.setpType("15"); //this is a date field - Input is long type - epoch time
        rProp4.getCon().setExpr(123456L); //Epoch time as Long
        assertThat(rProp4.getCon().getExpr(), instanceOf(Long.class));
        //endregion

        AsgStrategyContext asgStrategyContext = new AsgStrategyContext(ontology);
        AsgConstraintTransformationStrategy asgConstraintTransformationStrategy = new AsgConstraintTransformationStrategy();

        //Applying the Strategy on the RelProp #1 with the Epoch time
        asgConstraintTransformationStrategy.apply(asgQueryWithRelProps, asgStrategyContext);
        Object expr1 = ((RelProp) AsgQueryUtils.getElement(asgQueryWithRelProps, 4).get().geteBase()).getCon().getExpr();
        Object expr2 = ((RelProp) AsgQueryUtils.getElement(asgQueryWithRelProps, 5).get().geteBase()).getCon().getExpr();
        assertThat(expr1, instanceOf(Date.class));
        assertThat(expr2, instanceOf(Date.class));

        //Appling First the Properties Grouping Startegy and then applying the constraint transformation strategy
        //We want to be sure that the order of strategies is not affecting the final result
        AsgRelPropertiesGroupingStrategy asgRelPropertiesGroupingStrategy = new AsgRelPropertiesGroupingStrategy();
        asgRelPropertiesGroupingStrategy.apply(asgQueryWithRelPropsOriginal, new AsgStrategyContext());
        expr1 = ((RelPropGroup) AsgQueryUtils.getElement(asgQueryWithRelPropsOriginal, 4).get().geteBase()).getrProps().get(0).getCon().getExpr();
        expr2 = ((RelPropGroup) AsgQueryUtils.getElement(asgQueryWithRelPropsOriginal, 4).get().geteBase()).getrProps().get(1).getCon().getExpr();
        assertThat(expr1, instanceOf(Long.class));
        assertThat(expr2, instanceOf(Long.class));

        //Checking the RelProps grouping mechanism
        AsgEBase<EBase> newRelPropGroupAsgEbase = AsgQueryUtils.getElement(asgQueryWithRelPropsOriginal, 4).get();
        assertNotNull(newRelPropGroupAsgEbase);

        //Applying again the Constraint Transformation Strategy
        asgConstraintTransformationStrategy.apply(asgQueryWithRelPropsOriginal, asgStrategyContext);
        expr1 = ((RelPropGroup) AsgQueryUtils.getElement(asgQueryWithRelPropsOriginal, 4).get().geteBase()).getrProps().get(0).getCon().getExpr();
        expr2 = ((RelPropGroup) AsgQueryUtils.getElement(asgQueryWithRelPropsOriginal, 4).get().geteBase()).getrProps().get(1).getCon().getExpr();
        assertThat(expr1, instanceOf(Date.class));
        assertThat(expr2, instanceOf(Date.class));

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