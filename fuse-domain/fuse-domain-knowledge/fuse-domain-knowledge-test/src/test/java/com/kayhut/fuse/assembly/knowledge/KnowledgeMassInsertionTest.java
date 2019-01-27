package com.kayhut.fuse.assembly.knowledge;

import com.kayhut.fuse.assembly.knowledge.domain.EntityBuilder;
import com.kayhut.fuse.assembly.knowledge.domain.KnowledgeWriterContext;
import com.kayhut.fuse.assembly.knowledge.domain.RelationBuilder;
import com.kayhut.fuse.assembly.knowledge.domain.ValueBuilder;
import com.kayhut.fuse.model.resourceInfo.CursorResourceInfo;
import com.kayhut.fuse.model.resourceInfo.FuseResourceInfo;
import com.kayhut.fuse.model.resourceInfo.QueryResourceInfo;
import com.kayhut.fuse.model.results.Assignment;
import com.kayhut.fuse.model.results.AssignmentsQueryResult;
import com.kayhut.fuse.model.results.QueryResultBase;
import com.kayhut.fuse.model.transport.CreatePageRequest;
import com.kayhut.fuse.model.transport.cursor.CreateGraphCursorRequest;
import javaslang.Tuple3;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.kayhut.fuse.assembly.knowledge.Setup.*;
import static com.kayhut.fuse.assembly.knowledge.domain.EntityBuilder.INDEX;
import static com.kayhut.fuse.assembly.knowledge.domain.EntityBuilder._e;
import static com.kayhut.fuse.assembly.knowledge.domain.KnowledgeReaderContext.KNOWLEDGE;
import static com.kayhut.fuse.assembly.knowledge.domain.KnowledgeReaderContext.nextPage;
import static com.kayhut.fuse.assembly.knowledge.domain.KnowledgeWriterContext.commit;
import static com.kayhut.fuse.assembly.knowledge.domain.RelationBuilder.REL_INDEX;
import static com.kayhut.fuse.assembly.knowledge.domain.RelationBuilder._rel;
import static com.kayhut.fuse.assembly.knowledge.domain.ValueBuilder._v;
import static java.time.temporal.ChronoField.EPOCH_DAY;

public class KnowledgeMassInsertionTest {
    public static final String LOREM_IPSUM = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";
    public static final String[] words = LOREM_IPSUM.split(" ");

    static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    static KnowledgeWriterContext ctx;
    static Random rand;

    @BeforeClass
    public static void setup() throws Exception {
        Setup.setup(false);
        rand = new Random();
        ctx = KnowledgeWriterContext.init(client, manager.getSchema());
    }

    @After
    public void after() {
//        ctx.removeCreated();
//        ctx.clearCreated();
    }

    private Tuple3<List<EntityBuilder>, List<ValueBuilder>, List<RelationBuilder>> populateEntityWithRelation(int index) {
        final EntityBuilder e1 = _e(ctx.nextLogicalId()).cat(getCategory(index)).ctx("context1")
                .creationTime(getCreationTime(index)).creationUser(getCreationUser(index));

        final EntityBuilder e2 = _e(ctx.nextLogicalId()).cat(getCategory(index)).ctx("context1")
                .creationTime(getCreationTime(index)).creationUser(getCreationUser(index));

        final List<ValueBuilder> valueBuilders = IntStream.rangeClosed(1, rand.nextInt(5) + 1)
                .mapToObj(i -> e1.withValue(_v(ctx.nextValueId()).field(getFieldName(index)).value(getFieldValue(index)).bdt("???")
                        .creationTime(getCreationTime(index)).creationUser(getCreationUser(index)))).collect(Collectors.toList());

        final RelationBuilder rel = _rel(ctx.nextRelId()).ctx("context1").cat(getCategory(index)).creationUser(getCreationUser(index))
                .creationTime(getCreationTime(index)).creationUser(getCreationUser(index));

        rel.sideA(e1).sideB(e2);
        e1.rel(rel, "out");
        e2.rel(rel, "in");

        return new Tuple3<>(Arrays.asList(e1, e2), valueBuilders, Collections.singletonList(rel));
    }

    private String getFieldName(int index) {
        return words[(words.length - 1) / (rand.nextInt(index) + 1)];
    }

    private Object getFieldValue(int index) {
        return words[(words.length - 1) / (rand.nextInt(index) + 1)];
    }

    private String getCreationUser(int index) {
        return words[(words.length - 1) / (rand.nextInt(index) + 1)];
    }

    private String getCategory(int index) {
        return words[(words.length - 1) / (rand.nextInt(index) + 1)];
    }

    private Date getCreationTime(int index) {
        int minDay = (int) LocalDate.of(1900, 1, 1).toEpochDay();
        int maxDay = (int) LocalDate.of(2019, 1, 1).toEpochDay();
        long randomDay = minDay + rand.nextInt(maxDay - minDay);

        return new Date(LocalDate.ofEpochDay(randomDay).getLong(EPOCH_DAY));
    }

    @Test
    public void testInsertMassEntityWithValuesAndOneRelation() throws IOException, InterruptedException {
        List<EntityBuilder> entities = new ArrayList<>();
        List<ValueBuilder> values = new ArrayList<>();
        List<RelationBuilder> relations = new ArrayList<>();

        IntStream.rangeClosed(1, 1000).forEach(index -> {
            final Tuple3<List<EntityBuilder>, List<ValueBuilder>, List<RelationBuilder>> tuple3 = populateEntityWithRelation(index);
            entities.addAll(tuple3._1);
            values.addAll(tuple3._2);
            relations.addAll(tuple3._3);
        });

        System.out.println("completed writing " + commit(ctx, INDEX, entities) + " entities");
        System.out.println("completed writing " + commit(ctx, INDEX, values) + " e.values");
        System.out.println("completed writing " + commit(ctx, REL_INDEX, relations) + " relations");

        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        String query = "Match (e:Entity)-[r:hasEvalue]->(ev:Evalue) Return e,r,ev";


        // get Query URL
        QueryResourceInfo queryResourceInfo = fuseClient.postQuery(fuseResourceInfo.getQueryStoreUrl(), query, KNOWLEDGE);
        // Press on Cursor
        CursorResourceInfo cursorResourceInfo = fuseClient.postCursor(queryResourceInfo.getCursorStoreUrl(), new CreateGraphCursorRequest(new CreatePageRequest(100)));

//        QueryResultBase pageData = query(fuseClient, fuseResourceInfo,100, query, KNOWLEDGE);
        QueryResultBase pageData = nextPage(fuseClient, cursorResourceInfo, 100);
        while (pageData.getSize() > 0) {
            List<Assignment> assignments = ((AssignmentsQueryResult) pageData).getAssignments();

            // Check Entity Response
            Assert.assertEquals(1, pageData.getSize());
            Assert.assertEquals(1, assignments.size());
            Assert.assertEquals(false, assignments.get(0).getRelationships().isEmpty());
            Assert.assertEquals(false, assignments.get(0).getEntities().isEmpty());

            pageData = nextPage(fuseClient, cursorResourceInfo, 100);
        }
    }


}
