package com.kayhut.fuse.assembly.knowledge.cdr;

import com.kayhut.fuse.assembly.knowledge.Setup;
import com.kayhut.fuse.assembly.knowledge.domain.KnowledgeWriterContext;
import com.kayhut.fuse.model.resourceInfo.FuseResourceInfo;
import com.kayhut.fuse.model.results.AssignmentsQueryResult;
import com.kayhut.fuse.model.results.QueryResultBase;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static com.kayhut.fuse.assembly.knowledge.Setup.*;
import static com.kayhut.fuse.assembly.knowledge.domain.KnowledgeReaderContext.KNOWLEDGE;
import static com.kayhut.fuse.assembly.knowledge.domain.KnowledgeReaderContext.query;

public class KnowledgeSimpleEntityTests {
    static KnowledgeWriterContext ctx;

    @BeforeClass
    public static void setup() throws Exception {
        Setup.setup();
        ctx = KnowledgeWriterContext.init(client, manager.getSchema());
        DataLoader.load(client,ctx,"./data/cdr-sample.csv");
    }

    @After
    public void after() {
        ctx.removeCreated();
        ctx.clearCreated();
    }

    @Test
    public void testInsertOneSimpleEntityWithBuilder() throws IOException, InterruptedException {
        // Create v1 query to fetch newly created entity
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        final String query = "Match (phone:Entity)-[rel:hasRelation]->(any:Entity) Return phone,rel,any";
        QueryResultBase pageData = query(fuseClient, fuseResourceInfo, query, KNOWLEDGE);

        // Check Entity Response
        Assert.assertEquals(1, pageData.getSize());
        Assert.assertEquals(1, ((AssignmentsQueryResult) pageData).getAssignments().size());
    }
}
