package com.kayhut.fuse.assembly.knowledge;

import com.kayhut.fuse.assembly.knowledge.domain.KnowledgeWriterContext;
import com.kayhut.fuse.assembly.knowledge.domain.ValueBuilder;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import static com.kayhut.fuse.assembly.knowledge.KnowledgeAutomationFunctions.INDEX;
import static com.kayhut.fuse.assembly.knowledge.Setup.client;
import static com.kayhut.fuse.assembly.knowledge.Setup.manager;
import static com.kayhut.fuse.assembly.knowledge.domain.KnowledgeWriterContext.commit;
import static com.kayhut.fuse.assembly.knowledge.domain.RelationBuilder.REL_INDEX;


public class KnowledgeSimpleRvalueWithFilterE2ETests {

    static KnowledgeWriterContext ctx;
    static ValueBuilder rv1, rv2, rv3, rv4, rv5, rv6, rv7, rv8;
    static private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    @BeforeClass
    public static void setup() throws Exception {
        Setup.setup();
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        ctx = KnowledgeWriterContext.init(client, manager.getSchema());
        // Rvalue entities for tests

        // Insert Relation entities to ES
        Assert.assertEquals(8, commit(ctx, INDEX, rv1, rv2, rv3, rv4, rv5, rv6, rv7, rv8));
    }

    @AfterClass
    public static void after() {
        ctx.removeCreated();
    }

    // STRING_VALUE, CONTENT, TITLE, DISPLAY_NAME, DESCRIPTION => Find lower and Upper

    // Start Tests:
    //@Test

}
