package com.kayhut.fuse.assembly.knowledge.parser;

import com.kayhut.fuse.model.execution.plan.descriptors.QueryDescriptor;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.model.query.properties.RelPropGroup;
import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import com.kayhut.fuse.model.query.properties.constraint.ConstraintOp;
import com.kayhut.fuse.model.query.quant.Quant1;
import com.kayhut.fuse.model.query.quant.QuantType;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

import static com.kayhut.fuse.model.query.Rel.Direction.L;
import static com.kayhut.fuse.model.query.Rel.Direction.R;


public class JsonQueryTranslatorTest {


    @Test
    public void testSimpleStepQueryNoProperties() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("query/queryNoProps.json").getFile());
        String content = new String(Files.readAllBytes(file.toPath()));
        Query query = JsonQueryTranslator.jsonParser(new JSONObject(content));

        Query expected = Query.Builder.instance().withName("Query").withOnt("Knowledge")
                .withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "http://huha.com/minimal#person_1", "Entity", -1, 0)
        )).build();
        Assert.assertEquals(QueryDescriptor.print(expected),QueryDescriptor.print(query));
    }

    @Test
    public void testSimpleStepQueryWithSingleProperty() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("query/querySingleProps.json").getFile());
        String content = new String(Files.readAllBytes(file.toPath()));
        Query query = JsonQueryTranslator.jsonParser(new JSONObject(content));

        Query expected = Query.Builder.instance().withName("Query").withOnt("Knowledge")
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "http://huha.com/minimal#person_1", "Entity", 2, 0),
                        new Quant1(2,QuantType.all, Arrays.asList(3),0),
                        new Rel(3, "hasEvalue",R, null, 4, 0),
                        new ETyped(4, "http://huha.com/minimal#person_4", "Evalue", 5, 0),
                        new Quant1(5,QuantType.all, Arrays.asList(6,7),0),
                        new EProp(6,"fieldId",Constraint.of(ConstraintOp.eq,"title")),
                        new EProp(7,"stringValue",Constraint.of(ConstraintOp.like,"shuki"))
                )).build();
        Assert.assertEquals(QueryDescriptor.print(expected),QueryDescriptor.print(query));
    }

    @Test
    public void testSimpleStepQueryWithMultiProperty() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("query/queryMultiProps.json").getFile());
        String content = new String(Files.readAllBytes(file.toPath()));
        Query query = JsonQueryTranslator.jsonParser(new JSONObject(content));

        Query expected = Query.Builder.instance().withName("Query").withOnt("Knowledge")
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "http://huha.com/minimal#person_1", "Entity", 2, 0),
                        new Quant1(2,QuantType.all, Arrays.asList(3,8),0),
                        new Rel(3, "hasEvalue",R, null, 4, 0),
                        new ETyped(4, "http://huha.com/minimal#person_4", "Evalue", 5, 0),
                        new Quant1(5,QuantType.all, Arrays.asList(6,7),0),
                        new EProp(6,"fieldId",Constraint.of(ConstraintOp.eq,"birthday")),
                        new EProp(7,"dateValue",Constraint.of(ConstraintOp.gt,"2019-01-08 12:26")),

                        new Rel(8, "hasEvalue",R, null, 9, 0),
                        new ETyped(9, "http://huha.com/minimal#person_9", "Evalue", 10, 0),
                        new Quant1(10,QuantType.all, Arrays.asList(11,12),0),
                        new EProp(11,"fieldId",Constraint.of(ConstraintOp.eq,"title")),
                        new EProp(12,"stringValue",Constraint.of(ConstraintOp.contains,"shuki"))
                )).build();
        Assert.assertEquals(QueryDescriptor.print(expected),QueryDescriptor.print(query));
    }


    @Test
    public void testStepQueryProperties() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("query/singleStepQuery.json").getFile());
        String content = new String(Files.readAllBytes(file.toPath()));
        Query query = JsonQueryTranslator.jsonParser(new JSONObject(content));

        Query expected = Query.Builder.instance().withName("Query").withOnt("Knowledge")
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "http://huha.com/minimal#phoneNumber", "Entity", 2, 0),
                        new Quant1(2,QuantType.all, Arrays.asList(3,8),0),
                        new Rel(3, "hasEvalue",R, null, 4, 0),
                        new ETyped(4, "http://huha.com/minimal#person_4", "Evalue", 5, 0),
                        new Quant1(5,QuantType.all, Arrays.asList(6,7),0),
                        new EProp(6,"fieldId",Constraint.of(ConstraintOp.eq,"title")),
                        new EProp(7,"stringValue",Constraint.of(ConstraintOp.like,"11111111")),

                        new Rel(8, "relatedEntity",L, "http://huha.com/cdrs#caller", 11, 9),
                        new RelPropGroup(9,
                                RelProp.of(10,"category",Constraint.of(ConstraintOp.eq,10))),
                        new ETyped(11, "http://huha.com/minimal#person_9", "Entity", -1, 0))
                ).build();
        Assert.assertEquals(QueryDescriptor.print(expected),QueryDescriptor.print(query));
    }

    @Test
    public void testMultiStepQueryProperties() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("query/multiStepQuery.json").getFile());
        String content = new String(Files.readAllBytes(file.toPath()));
        Query query = JsonQueryTranslator.jsonParser(new JSONObject(content));

        Query expected = Query.Builder.instance().withName("Query").withOnt("Knowledge")
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "http://huha.com/minimal#phoneNumber", "Entity", 2, 0),
                        new Quant1(2,QuantType.all, Arrays.asList(3,8),0),
                        new Rel(3, "hasEvalue",R, null, 4, 0),
                        new ETyped(4, "http://huha.com/minimal#person_4", "Evalue", 5, 0),
                        new Quant1(5,QuantType.all, Arrays.asList(6,7),0),
                        new EProp(6,"fieldId",Constraint.of(ConstraintOp.eq,"title")),
                        new EProp(7,"stringValue",Constraint.of(ConstraintOp.like,"11111111")),

                        new Rel(8, "relatedEntity",R, "http://huha.com/cdrs#caller", 11, 9),
                        new RelPropGroup(9,
                                RelProp.of(10,"context",Constraint.of(ConstraintOp.eq,10))),
                        new ETyped(11, "http://huha.com/minimal#person_9", "Entity", 12, 0),
                        new Rel(12, "relatedEntity",R, "http://huha.com/cdrs#caller", 15, 13),
                        new RelPropGroup(13,
                                RelProp.of(14,"category",Constraint.of(ConstraintOp.eq,10))),
                        new ETyped(15, "http://huha.com/minimal#person_9", "Entity", -1, 0))
                ).build();
        Assert.assertEquals(QueryDescriptor.print(expected),QueryDescriptor.print(query));
    }
}
