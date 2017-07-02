package com.kayhut.fuse.asg.strategy.constraintTransformation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Supplier;
import com.kayhut.fuse.asg.AsgQueryStore;
import com.kayhut.fuse.dispatcher.asg.builder.BNextFactory;
import com.kayhut.fuse.dispatcher.asg.builder.NextEbaseFactory;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.asg.strategy.propertiesGrouping.AsgRelPropertiesGroupingStrategy;
import com.kayhut.fuse.dispatcher.asg.AsgQuerySupplier;
import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.*;
import com.kayhut.fuse.model.query.entity.ETyped;
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * Created by benishue on 09-May-17.
 */
public class AsgConstraintTypeTransformationStrategyTest {
    //region Setup
    @Before
    public void setUp() throws Exception {
        String ontologyExpectedJson = readJsonToString("src/test/resources/Dragons_Ontology.json");
        ont = new Ontology.Accessor(new ObjectMapper().readValue(ontologyExpectedJson, Ontology.class));

    }
    //endregion

    public static AsgQuery Q1() {
        //region Query Building
        Query query = new Query(); //Person owns Dragon with EProp - Name: 'dragonA'
        query.setOnt("Dragons");
        query.setName("Q1");
        List<EBase> elements = new ArrayList<EBase>();

       /*
        {
          "eNum": 0,
          "type": "Start",
          "next": 1
        }
         */

        Start start = new Start();
        start.seteNum(0);
        start.setNext(1);
        elements.add(start);

       /* Person
         {
          "eNum": 1,
          "type": "ETyped",
          "eTag": "A",
          "eType": 1,
          "next":2
        }
        */

        ETyped eTypedA = new ETyped();
        eTypedA.seteNum(1);
        eTypedA.seteTag("A");
        eTypedA.seteType("Person");
        eTypedA.setNext(2);
        elements.add(eTypedA);

       /* Owns
        {
          "eNum": 2,
          "type": "Rel",
          "rType": 1,
          "dir": "R",
          "next": 3
        }
         */
        Rel rel = new Rel();
        rel.seteNum(2);
        rel.setrType("Person");
        rel.setDir(Rel.Direction.R);
        rel.setNext(3);
        elements.add(rel);


       /* Dragon
        {
          "eNum": 3,
          "type": "ETyped",
          "eTag": "B",
          "eType": 2
        }
        */
        ETyped eTypedB = new ETyped();
        eTypedB.seteNum(3);
        eTypedB.seteTag("B");
        eTypedB.seteType("Dragon");
        eTypedB.setNext(4);
        elements.add(eTypedB);

       /* The dragon has the Name Entity Property = "dragonA"
            "type": "EProp",
            "eNum": 4,
            "pType": "1.1",
            "pTag": "1",
            "con": {
            "op": "eq",
            "expr": "dragonA"
            }
         */


        EProp eProp = new EProp();
        eProp.seteNum(4);
        eProp.setpType("1");
        eProp.setpTag("1");
        Constraint con = new Constraint();
        con.setOp(ConstraintOp.eq);
        con.setExpr("dragonA");
        eProp.setCon(con);
        elements.add(eProp);

        query.setElements(elements);

        //endregion

        Supplier<AsgQuery> asgSupplier = new AsgQuerySupplier(query,new NextEbaseFactory(), new BNextFactory());
        AsgQuery asgQuery = asgSupplier.get();
        return asgQuery;
    }

    //region Test Methods
    @Test
    public void asgConstraintTransformationStrategyEPropsLongToDateTest() throws Exception {
        AsgQuery asgQueryWithEProps = Q1();

        //Setting The EProp expression as a date represented by Long value
        EProp eProp = (EProp) AsgQueryUtil.element(asgQueryWithEProps, 4).get().geteBase();
        eProp.setpType("dateSinceTheBigBang"); //this is a date field - Input is long - epoch time
        eProp.getCon().setExpr(123456L); //Epoch time as Long

        assertThat(eProp.getCon().getExpr(), instanceOf(Long.class));

        AsgStrategyContext asgStrategyContext = new AsgStrategyContext(ont);
        AsgConstraintTypeTransformationStrategy asgConstraintTypeTransformationStrategy = new AsgConstraintTypeTransformationStrategy();

        //Applying the Strategy on the Eprop with the Epoch time
        asgConstraintTypeTransformationStrategy.apply(asgQueryWithEProps, asgStrategyContext);
        Object expr = ((EProp) AsgQueryUtil.element(asgQueryWithEProps, 4).get().geteBase()).getCon().getExpr();
        assertThat(expr, instanceOf(Date.class));
    }

    @Test
    public void asgConstraintTransformationStrategyEPropsIntToDateTest() throws Exception {
        AsgQuery asgQueryWithEProps = Q1();

        //Setting The EProp expression as a date represented by Long value
        EProp eProp = (EProp) AsgQueryUtil.element(asgQueryWithEProps, 4).get().geteBase();
        eProp.setpType("dateSinceTheBigBang"); //this is a date field - Input is long - epoch time
        eProp.getCon().setExpr(123456); //Epoch time as Long

        assertThat(eProp.getCon().getExpr(), instanceOf(int.class));

        AsgStrategyContext asgStrategyContext = new AsgStrategyContext(ont);
        AsgConstraintTypeTransformationStrategy asgConstraintTypeTransformationStrategy = new AsgConstraintTypeTransformationStrategy();

        //Applying the Strategy on the Eprop with the Epoch time
        asgConstraintTypeTransformationStrategy.apply(asgQueryWithEProps, asgStrategyContext);
        Object expr = ((EProp) AsgQueryUtil.element(asgQueryWithEProps, 4).get().geteBase()).getCon().getExpr();
        assertThat(expr, instanceOf(Date.class));
    }

    @Test
    public void asgConstraintTransformationStrategyEPropsIntToLongTest() throws Exception {
        AsgQuery asgQueryWithEProps = Q1();

        //Setting The EProp expression as an int
        EProp eProp = (EProp) AsgQueryUtil.element(asgQueryWithEProps, 4).get().geteBase();
        eProp.setpType("height"); //this is an int field
        eProp.getCon().setExpr(99);

        assertThat(eProp.getCon().getExpr(), instanceOf(Integer.class));


        AsgStrategyContext asgStrategyContext = new AsgStrategyContext(ont);
        AsgConstraintTypeTransformationStrategy asgConstraintTypeTransformationStrategy = new AsgConstraintTypeTransformationStrategy();

        //Applying the Strategy on the Eprop with the Epoch time
        asgConstraintTypeTransformationStrategy.apply(asgQueryWithEProps, asgStrategyContext);
        Object expr = ((EProp) AsgQueryUtil.element(asgQueryWithEProps, 4).get().geteBase()).getCon().getExpr();
        assertThat(expr, instanceOf(long.class));
    }

    @Test
    public void asgConstraintTransformationStrategyRelPropsLongToDateTest() throws Exception {
        AsgQuery asgQueryWithRelProps = AsgQueryStore.Q188_V1();
        AsgQuery asgQueryWithRelPropsOriginal = AsgQueryStore.Q188_V1();

        //region Preparing the Properties for the AsgQuery
        //Setting The RelProp (enum #4) expression as a date represented by Long value
        RelProp rProp1 = (RelProp) AsgQueryUtil.element(asgQueryWithRelProps, 4).get().geteBase();
        rProp1.setpType("dateSinceTheBigBang"); //this is a date field - Input is long type - epoch time
        rProp1.getCon().setExpr(123456L); //Epoch time as Long
        assertThat(rProp1.getCon().getExpr(), instanceOf(Long.class));

        //Setting The RelProp (enum #5) expression as a date represented by Long value
        RelProp rProp2 = (RelProp) AsgQueryUtil.element(asgQueryWithRelProps, 5).get().geteBase();
        rProp2.setpType("dateSinceTheBigBang"); //this is a date field - Input is long type - epoch time
        rProp2.getCon().setExpr(5555L); //Epoch time as Long
        assertThat(rProp2.getCon().getExpr(), instanceOf(Long.class));



        RelProp rProp3 = (RelProp) AsgQueryUtil.element(asgQueryWithRelPropsOriginal, 5).get().geteBase();
        rProp3.setpType("dateSinceTheBigBang"); //this is a date field - Input is long type - epoch time
        rProp3.getCon().setExpr(5555L); //Epoch time as Long
        assertThat(rProp3.getCon().getExpr(), instanceOf(Long.class));

        RelProp rProp4 = (RelProp) AsgQueryUtil.element(asgQueryWithRelPropsOriginal, 4).get().geteBase();
        rProp4.setpType("dateSinceTheBigBang"); //this is a date field - Input is long type - epoch time
        rProp4.getCon().setExpr(123456L); //Epoch time as Long
        assertThat(rProp4.getCon().getExpr(), instanceOf(Long.class));
        //endregion

        AsgStrategyContext asgStrategyContext = new AsgStrategyContext(ont);
        AsgConstraintTypeTransformationStrategy asgConstraintTypeTransformationStrategy = new AsgConstraintTypeTransformationStrategy();

        //Applying the Strategy on the RelProp #1 with the Epoch time
        asgConstraintTypeTransformationStrategy.apply(asgQueryWithRelProps, asgStrategyContext);
        Object expr1 = ((RelProp) AsgQueryUtil.element(asgQueryWithRelProps, 4).get().geteBase()).getCon().getExpr();
        Object expr2 = ((RelProp) AsgQueryUtil.element(asgQueryWithRelProps, 5).get().geteBase()).getCon().getExpr();
        assertThat(expr1, instanceOf(Date.class));
        assertThat(expr2, instanceOf(Date.class));

        //Appling First the Properties Grouping Startegy and then applying the constraint transformation strategy
        //We want to be sure that the order of strategies is not affecting the final result
        AsgRelPropertiesGroupingStrategy asgRelPropertiesGroupingStrategy = new AsgRelPropertiesGroupingStrategy();
        asgRelPropertiesGroupingStrategy.apply(asgQueryWithRelPropsOriginal, new AsgStrategyContext(ont));

        expr1 = ((RelPropGroup) AsgQueryUtil.element(asgQueryWithRelPropsOriginal, 4).get().geteBase()).getProps().get(0).getCon().getExpr();
        expr2 = ((RelPropGroup) AsgQueryUtil.element(asgQueryWithRelPropsOriginal, 4).get().geteBase()).getProps().get(1).getCon().getExpr();

        assertThat(expr1, instanceOf(Long.class));
        assertThat(expr2, instanceOf(Long.class));

        //Checking the RelProps grouping mechanism
        AsgEBase<EBase> newRelPropGroupAsgEbase = AsgQueryUtil.element(asgQueryWithRelPropsOriginal, 4).get();
        assertNotNull(newRelPropGroupAsgEbase);

        //Applying again the Constraint Transformation Strategy
        asgConstraintTypeTransformationStrategy.apply(asgQueryWithRelPropsOriginal, asgStrategyContext);

        expr1 = ((RelPropGroup) AsgQueryUtil.element(asgQueryWithRelPropsOriginal, 4).get().geteBase()).getProps().get(0).getCon().getExpr();
        expr2 = ((RelPropGroup) AsgQueryUtil.element(asgQueryWithRelPropsOriginal, 4).get().geteBase()).getProps().get(1).getCon().getExpr();

        assertThat(expr1, instanceOf(Date.class));
        assertThat(expr2, instanceOf(Date.class));

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