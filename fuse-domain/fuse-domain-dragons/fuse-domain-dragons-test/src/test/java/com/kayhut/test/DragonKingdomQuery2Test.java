package com.kayhut.test;

import com.kayhut.fuse.model.OntologyTestUtils;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import com.kayhut.fuse.model.query.properties.constraint.ConstraintOp;
import com.kayhut.fuse.model.query.quant.Quant1;
import com.kayhut.fuse.model.query.quant.QuantType;
import com.kayhut.fuse.model.resourceInfo.FuseResourceInfo;
import com.kayhut.fuse.utils.FuseClient;
import com.kayhut.fuse.services.test.TestCase;

import java.util.Arrays;

import static com.kayhut.fuse.model.OntologyTestUtils.NAME;
import static com.kayhut.fuse.model.OntologyTestUtils.ORIGINATED_IN;

public class DragonKingdomQuery2Test extends TestCase {

    public void run(FuseClient fuseClient) throws Exception {
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Ontology.Accessor ont = new Ontology.Accessor(fuseClient.getOntology(fuseResourceInfo.getCatalogStoreUrl() + "/Dragons"));
        Query query = Query.Builder.instance().withName(NAME.name).withOnt(ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new EConcrete(1, "A", ont.eType$(OntologyTestUtils.DRAGON.name), "Dragon_1", "D0", 2, 0),
                new Rel(2, ont.rType$(ORIGINATED_IN.getName()), Rel.Direction.R, null, 3, 0),
                new ETyped(3, "B", ont.eType$(OntologyTestUtils.KINGDOM.name), 4, 0),
                new Rel(4, ont.rType$(ORIGINATED_IN.getName()), Rel.Direction.L, null, 5, 0),
                new ETyped(5, "C", ont.eType$(OntologyTestUtils.DRAGON.name), 6, 0),
                new Quant1(6, QuantType.all,Arrays.asList(7,8),0),
                new EProp(7, NAME.type, Constraint.of(ConstraintOp.eq, "BB")),
                new Rel(8, ont.rType$(ORIGINATED_IN.getName()), Rel.Direction.R, null, 9, 0),
                new ETyped(9, "D", ont.eType$(OntologyTestUtils.KINGDOM.name), 10, 0),
                new Rel(10, ont.rType$(ORIGINATED_IN.getName()), Rel.Direction.L, null, 11, 0),
                new ETyped(11, "E", ont.eType$(OntologyTestUtils.DRAGON.name), 12, 0),
                new EProp(12, NAME.type, Constraint.of(ConstraintOp.eq, "AB"))
        )).build();


        testAndAssertQuery(query, fuseClient);
    }


}
