package com.yangdb.test;

import com.yangdb.fuse.model.OntologyTestUtils;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.query.Query;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.Start;
import com.yangdb.fuse.model.query.entity.EConcrete;
import com.yangdb.fuse.model.query.entity.ETyped;
import com.yangdb.fuse.model.resourceInfo.FuseResourceInfo;
import com.yangdb.fuse.client.FuseClient;
import com.yangdb.fuse.services.test.TestCase;

import java.util.Arrays;

import static com.yangdb.fuse.model.OntologyTestUtils.*;

public class DragonKingdomQuery4Test extends TestCase {

    public void run(FuseClient fuseClient) throws Exception {
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Ontology.Accessor ont = new Ontology.Accessor(fuseClient.getOntology(fuseResourceInfo.getCatalogStoreUrl() + "/Dragons"));
        Query query = Query.Builder.instance().withName(NAME.name).withOnt(ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new EConcrete(1, "A", ont.eType$(OntologyTestUtils.DRAGON.name), "Dragon_100", "D0", 2, 0),
                new Rel(2, ont.rType$(FIRE.getName()), Rel.Direction.R, null, 3, 0),
                new ETyped(3, "B", ont.eType$(OntologyTestUtils.DRAGON.name), 4, 0),
                new Rel(4, ont.rType$(ORIGINATED_IN.getName()), Rel.Direction.R, null, 5, 0),
                new ETyped(5, "C", ont.eType$(OntologyTestUtils.KINGDOM.name), 6, 0),
                new Rel(6, ont.rType$(ORIGINATED_IN.getName()), Rel.Direction.L, null, 7, 0),
                new ETyped(7, "D", ont.eType$(OntologyTestUtils.DRAGON.name), 8, 0),
                new Rel(8, ont.rType$(FIRE.getName()), Rel.Direction.L, null, 9, 0),
                new EConcrete(9, "E", ont.eType$(OntologyTestUtils.DRAGON.name), "Dragon_200", "D0", 0, 0)
        )).build();
        /*Query query = Query.Builder.instance().withName(NAME.name).withOnt(ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new EConcrete(1, "A", ont.eType$(OntologyTestUtils.DRAGON.name), "Dragon_100", "D0", singletonList(NAME.type), 2, 0),
                new Rel(2, ont.rType$(FIRE.getType()), Rel.Direction.R, null, 3, 0),
                new ETyped(3, "B", ont.eType$(OntologyTestUtils.DRAGON.name), singletonList(NAME.type), 0, 0)
        )).build();*/


        testAndAssertQuery(query, fuseClient);
    }


}
