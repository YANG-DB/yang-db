package com.kayhut.fuse.assembly.knowledge.parser;

import com.kayhut.fuse.assembly.knowledge.parser.model.BusinessTypesProvider;
import com.kayhut.fuse.model.execution.plan.descriptors.QueryDescriptor;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import com.kayhut.fuse.model.query.properties.constraint.ConstraintOp;
import com.kayhut.fuse.model.query.quant.Quant1;
import com.kayhut.fuse.model.query.quant.QuantType;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.Arrays;

import static com.kayhut.fuse.model.query.Rel.Direction.L;
import static com.kayhut.fuse.model.query.Rel.Direction.R;


public class JsonQueryTranslatorTest {
    static JsonQueryTranslator parser;
    static BusinessTypesProvider typesProvider;

    
    @BeforeClass
    public static void setUp() throws URISyntaxException {
        typesProvider = new FolderBasedTypeProvider("ontology");
        parser = new JsonQueryTranslator();
    }

    @Test
    public void testSimpleStepQueryNoProperties() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("query/queryNoProps.json").getFile());
        String content = new String(Files.readAllBytes(file.toPath()));
        Query query = parser.translate(new JSONObject(content).getJSONObject("query"),typesProvider);

        Query expected = Query.Builder.instance().withName("Query").withOnt("Knowledge")
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "http://huha.com/minimal#person_1", "Entity", -1, 0)
                )).build();
        Assert.assertEquals(QueryDescriptor.print(expected), QueryDescriptor.print(query));

    }

    @Test
    public void testSingleStepQueryNoProperties() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("query/singleEntityQueryNoProps.json").getFile());
        String content = new String(Files.readAllBytes(file.toPath()));
        Query query = parser.translate(new JSONObject(content).getJSONObject("query"),typesProvider);

        Query expected = Query.Builder.instance().withName("Query").withOnt("Knowledge")
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "http://huha.com/minimal#person_1", "Entity", 2, 0),
                        new Rel(2, "hasRelation",R, null, 3),
                        new ETyped(3, "http://huha.com/cdrs#caller", "Relation", 4, 0),
                        new Quant1(4, QuantType.all, Arrays.asList(5), 0),
                        new EProp(5, "category", Constraint.of(ConstraintOp.eq, "http://huha.com/cdrs#caller"))

                )).build();
        Assert.assertEquals(QueryDescriptor.print(expected), QueryDescriptor.print(query));

    }
    @Test
    public void testSingleStepQueryWithProperties() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("query/singleEntityQueryProps.json").getFile());
        String content = new String(Files.readAllBytes(file.toPath()));
        Query query = parser.translate(new JSONObject(content).getJSONObject("query"),typesProvider);

        String expected = "[└── Start, \n" +
                "    ──Typ[Entity:1]──Q[2]:{3|8}, \n" +
                "                           └-> Rel(hasEvalue:3)──Typ[Evalue:4]──Q[5]:{6|7}, \n" +
                "                                                                      └─?[6]:[fieldId<eq,http://huha.com/gdelt#globalEventId>]──Typ[Relation:9]──Q[10]:{11|12}, \n" +
                "                                                                      └─?[7]:[stringValue<like,840100756>], \n" +
                "                           └-> Rel(hasRelation:8), \n" +
                "                                             └─?[11]:[category<eq,http://huha.com/cdrs#caller>], \n" +
                "                                             └-> Rel(hasRvalue:12)──Typ[Rvalue:13]──Q[14]:{15|16}, \n" +
                "                                                                                             └─?[15]:[fieldId<eq,http://huha.com/cdrs#location>], \n" +
                "                                                                                             └─?[16]:[stringValue<inSet,[geo_bounds, 25,-106.7, 24.5,-106.43]>]]";
        Assert.assertEquals(expected, QueryDescriptor.print(query));

    }

    @Test
    public void testSimpleStepQueryWithSingleProperty() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("query/querySingleProps.json").getFile());
        String content = new String(Files.readAllBytes(file.toPath()));
        Query query = parser.translate(new JSONObject(content).getJSONObject("query"),typesProvider);

        Query expected = Query.Builder.instance().withName("Query").withOnt("Knowledge")
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "http://huha.com/minimal#person_1", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3), 0),
                        new Rel(3, "hasEvalue", R, null, 4, 0),
                        new ETyped(4, "http://huha.com/minimal#person_4", "Evalue", 5, 0),
                        new Quant1(5, QuantType.all, Arrays.asList(6, 7), 0),
                        new EProp(6, "fieldId", Constraint.of(ConstraintOp.eq, "http://huha.com/minimal#title")),
                        new EProp(7, "stringValue", Constraint.of(ConstraintOp.like, "shuki"))
                )).build();
        Assert.assertEquals(QueryDescriptor.print(expected), QueryDescriptor.print(query));
    }

    @Test
    public void testSimpleStepQueryWithSingleRelProperty() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("query/querySingleRelProps.json").getFile());
        String content = new String(Files.readAllBytes(file.toPath()));
        Query query = parser.translate(new JSONObject(content).getJSONObject("query"),typesProvider);

        Query expected = Query.Builder.instance().withName("Query").withOnt("Knowledge")
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new Rel(1, "hasRelation", L, null, 2),
                        new ETyped(2, "http://huha.com/cdrs#caller_1", "Relation", 3),
                        new Quant1(3, QuantType.all, Arrays.asList(4, 5)),
                        new EProp(4, "category", Constraint.of(ConstraintOp.eq, "http://huha.com/cdrs#caller")),
                        new Rel(5, "hasRvalue", R, null, 6),
                        new ETyped(6, "http://huha.com/minimal#caller_4", "Rvalue", 7),
                        new Quant1(7, QuantType.all, Arrays.asList(8, 9)),
                        new EProp(8, "fieldId", Constraint.of(ConstraintOp.eq, "http://huha.com/cdrs#category")),
                        new EProp(9, "stringValue", Constraint.of(ConstraintOp.eq, "10"))
                )).build();
        Assert.assertEquals(QueryDescriptor.print(expected), QueryDescriptor.print(query));
    }

    @Test
    public void testSimpleStepQueryWithMultiProperty() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("query/queryMultiProps.json").getFile());
        String content = new String(Files.readAllBytes(file.toPath()));
        Query query = parser.translate(new JSONObject(content).getJSONObject("query"),typesProvider);

        Query expected = Query.Builder.instance().withName("Query").withOnt("Knowledge")
                .withElements(Arrays.asList(
                        new Start(0, 1),
                        new ETyped(1, "http://huha.com/minimal#person_1", "Entity", 2, 0),
                        new Quant1(2, QuantType.all, Arrays.asList(3, 8), 0),
                        new Rel(3, "hasEvalue", R, null, 4, 0),
                        new ETyped(4, "http://huha.com/minimal#person_4", "Evalue", 5, 0),
                        new Quant1(5, QuantType.all, Arrays.asList(6, 7), 0),
                        new EProp(6, "fieldId", Constraint.of(ConstraintOp.eq, "http://huha.com/minimal#birthday")),
                        new EProp(7, "dateValue", Constraint.of(ConstraintOp.gt, "2019-01-08 12:26")),

                        new Rel(8, "hasEvalue", R, null, 9, 0),
                        new ETyped(9, "http://huha.com/minimal#person_9", "Evalue", 10, 0),
                        new Quant1(10, QuantType.all, Arrays.asList(11, 12), 0),
                        new EProp(11, "fieldId", Constraint.of(ConstraintOp.eq, "http://huha.com/minimal#title")),
                        new EProp(12, "stringValue", Constraint.of(ConstraintOp.like, "shuki"))
                )).build();
        Assert.assertEquals(QueryDescriptor.print(expected), QueryDescriptor.print(query));
    }


    @Test
    public void testStepOutQueryProperties() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("query/singleOutStepQuery.json").getFile());
        String content = new String(Files.readAllBytes(file.toPath()));
        Query query = parser.translate(new JSONObject(content).getJSONObject("query"),typesProvider);

        String expected = "[└── Start, \n" +
                "    ──Typ[Entity:1]──Q[2]:{3|8}, \n" +
                "                           └-> Rel(hasEvalue:3)──Typ[Evalue:4]──Q[5]:{6|7}, \n" +
                "                                                                      └─?[6]:[fieldId<eq,http://huha.com/minimal#title>]──Typ[Relation:9]──Q[10]:{11|12|17}, \n" +
                "                                                                      └─?[7]:[stringValue<like,11111111>], \n" +
                "                           └<--Rel(hasRelation:8), \n" +
                "                                             └─?[11]:[category<eq,http://huha.com/cdrs#caller>], \n" +
                "                                             └-> Rel(hasRvalue:12)──Typ[Rvalue:13]──Q[14]:{15|16}, \n" +
                "                                                                                             └─?[15]:[fieldId<eq,http://huha.com#category>]──Typ[Entity:18], \n" +
                "                                                                                             └─?[16]:[stringValue<eq,10>], \n" +
                "                                             └-> Rel(hasRelation:17)]";
        Assert.assertEquals(expected, QueryDescriptor.print(query));
    }
    @Test
    public void testStepInQueryProperties() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("query/singleInStepQuery.json").getFile());
        String content = new String(Files.readAllBytes(file.toPath()));
        Query query = parser.translate(new JSONObject(content).getJSONObject("query"),typesProvider);

        String expected = "[└── Start, \n" +
                "    ──Typ[Entity:1]--> Rel(hasRelation:2)──Typ[Relation:3]──Q[4]:{5|6}, \n" +
                "                                                                  └─?[5]:[category<eq,http://huha.com/facebook#friends>], \n" +
                "                                                                  └<--Rel(hasRelation:6)──Typ[Entity:7]]";
        Assert.assertEquals(expected, QueryDescriptor.print(query));
    }

    @Test
    public void testMultiStepQueryProperties() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("query/multiStepQuery.json").getFile());
        String content = new String(Files.readAllBytes(file.toPath()));
        Query query = parser.translate(new JSONObject(content).getJSONObject("query"),typesProvider);

        String expected = "[└── Start, \n" +
                "    ──Typ[Entity:1]──Q[2]:{3|8}, \n" +
                "                           └-> Rel(hasEvalue:3)──Typ[Evalue:4]──Q[5]:{6|7}, \n" +
                "                                                                      └─?[6]:[fieldId<eq,http://huha.com/minimal#title>]──Typ[Relation:9]──Q[10]:{11|12|17}, \n" +
                "                                                                      └─?[7]:[stringValue<like,11111111>], \n" +
                "                           └-> Rel(hasRelation:8), \n" +
                "                                             └─?[11]:[category<eq,http://huha.com/cdrs#caller>], \n" +
                "                                             └-> Rel(hasRvalue:12)──Typ[Rvalue:13]──Q[14]:{15|16}, \n" +
                "                                                                                             └─?[15]:[fieldId<eq,http://huha.com#context>]──Typ[Entity:18]--> Rel(hasRelation:19)──Typ[Relation:20]──Q[21]:{22|23|28}, \n" +
                "                                                                                             └─?[16]:[stringValue<eq,10>], \n" +
                "                                             └<--Rel(hasRelation:17), \n" +
                "                                                                └─?[22]:[category<eq,http://huha.com/cdrs#receiver>], \n" +
                "                                                                └-> Rel(hasRvalue:23)──Typ[Rvalue:24]──Q[25]:{26|27}, \n" +
                "                                                                                                                └─?[26]:[fieldId<eq,http://huha.com#category>]──Typ[Entity:29], \n" +
                "                                                                                                                └─?[27]:[stringValue<eq,10>], \n" +
                "                                                                └<--Rel(hasRelation:28)]";
        Assert.assertEquals(expected, QueryDescriptor.print(query));
    }
}
