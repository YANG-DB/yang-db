package com.kayhut.fuse.assembly.knowledge.parser;

import com.kayhut.fuse.model.execution.plan.descriptors.QueryDescriptor;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.optional.OptionalComp;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import com.kayhut.fuse.model.query.properties.constraint.ConstraintOp;
import com.kayhut.fuse.model.query.quant.Quant1;
import com.kayhut.fuse.model.query.quant.QuantType;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

import static com.kayhut.fuse.model.query.Rel.Direction.L;
import static com.kayhut.fuse.model.query.Rel.Direction.R;


@Ignore
public class Json2QueryMapperTest {

    @Test
    public void testSimpleStepQueryNoProperties() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("queryNoProps.json").getFile());
        String content = new String(Files.readAllBytes(file.toPath()));
        Query query = Json2QueryMapper.jsonParser(new JSONObject(content));

        Query expected = Query.Builder.instance().withName("Query").withOnt("Knowledge")
                .withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "http://huha.com/minimal#person_1", "Entity", 0, 0)
        )).build();
        Assert.assertEquals(QueryDescriptor.print(expected),QueryDescriptor.print(query));
    }

    @Test
    public void testSimpleStepQueryWithSingleProperty() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("querySingleProps.json").getFile());
        String content = new String(Files.readAllBytes(file.toPath()));
        Query query = Json2QueryMapper.jsonParser(new JSONObject(content));

        Query expected = Query.Builder.instance().withName("Query").withOnt("Knowledge")
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "http://huha.com/minimal#person_1", "Entity", 2, 0),
                        new Quant1(2,QuantType.all, Arrays.asList(3),0),
                        new Rel(3, "hasEvalue",R, null, 4, 0),
                        new ETyped(4, "http://huha.com/minimal#person_4", "EValue", 5, 0),
                        new Quant1(5,QuantType.all, Arrays.asList(6,7),0),
                        new EProp(6,"fieldId",Constraint.of(ConstraintOp.eq,"http://huha.com#title")),
                        new EProp(7,"stringValue",Constraint.of(ConstraintOp.contains,"shuki"))
                )).build();
        Assert.assertEquals(QueryDescriptor.print(expected),QueryDescriptor.print(query));
    }

    @Test
    public void testSimpleStepQueryWithMultiProperty() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("queryMultiProps.json").getFile());
        String content = new String(Files.readAllBytes(file.toPath()));
        Query query = Json2QueryMapper.jsonParser(new JSONObject(content));

        Query expected = Query.Builder.instance().withName("Query").withOnt("Knowledge")
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "http://huha.com/minimal#person_1", "Entity", 2, 0),
                        new Quant1(2,QuantType.all, Arrays.asList(3,8),0),
                        new Rel(3, "hasEvalue",R, null, 4, 0),
                        new ETyped(4, "http://huha.com/minimal#person_4", "EValue", 5, 0),
                        new Quant1(5,QuantType.all, Arrays.asList(6,7),0),
                        new EProp(6,"fieldId",Constraint.of(ConstraintOp.eq,"http://huha.com/minimal#birthday")),
                        new EProp(7,"dateValue",Constraint.of(ConstraintOp.gt,"2019-01-08 12:26")),

                        new Rel(8, "hasEvalue",R, null, 9, 0),
                        new ETyped(9, "http://huha.com/minimal#person_9", "EValue", 10, 0),
                        new Quant1(10,QuantType.all, Arrays.asList(11,12),0),
                        new EProp(11,"fieldId",Constraint.of(ConstraintOp.eq,"http://huha.com#title")),
                        new EProp(12,"stringValue",Constraint.of(ConstraintOp.contains,"shuki"))
                )).build();
        Assert.assertEquals(QueryDescriptor.print(expected),QueryDescriptor.print(query));
    }


    @Test
    public void testStepQueryNoProperties() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("singleStepQuery.json").getFile());
        String content = new String(Files.readAllBytes(file.toPath()));
        Query query = Json2QueryMapper.jsonParser(new JSONObject(content));

        Query expected = Query.Builder.instance().withName("Query").withOnt("Knowledge")
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "http://huha.com/minimal#person_1", "Entity", 0, 0)
                )).build();
        Assert.assertEquals(QueryDescriptor.print(expected),QueryDescriptor.print(query));
    }
}
