package com.kayhut.fuse.test;

import com.kayhut.fuse.model.OntologyTestUtils;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.*;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import com.kayhut.fuse.model.query.properties.constraint.ConstraintOp;
import com.kayhut.fuse.model.query.quant.Quant1;
import com.kayhut.fuse.model.query.quant.QuantType;
import com.kayhut.fuse.model.resourceInfo.FuseResourceInfo;
import com.kayhut.fuse.test.util.FuseClient;

import java.util.Arrays;
import java.util.Collections;

import static com.kayhut.fuse.model.OntologyTestUtils.FIRE;
import static com.kayhut.fuse.model.OntologyTestUtils.NAME;
import static com.kayhut.fuse.model.OntologyTestUtils.ORIGINATED_IN;
import static java.util.Collections.singletonList;


public class DragonKingdomQuery7Test extends TestCase {

    /**
     *
     [└── Start,
             ──Typ[Dragon:1]--> Rel(originatedIn:2)──Typ[Kingdom:3]-<--Rel(originatedIn:4)──Typ[Dragon:5]──Q[6]:{7|8|13},
                                                                                                             └─?[7]:[name<eq,BB>],
                                                                                                             └-> Rel(fire:8)──Typ[Dragon:9]--> Rel(originatedIn:10)──Typ[Kingdom:11]──?[12]:[name<eq,AB>],
                                                                                                             └-> Rel(fire:13)──Conc[Dragon:14]-<--Rel(fire:15)──Typ[Dragon:16]]
     */

    public void run(FuseClient fuseClient) throws Exception {
        FuseResourceInfo fuseResourceInfo = fuseClient.getFuseInfo();
        Ontology.Accessor ont = new Ontology.Accessor(fuseClient.getOntology(fuseResourceInfo.getCatalogStoreUrl() + "/Dragons"));
        Query query = Query.Builder.instance().withName(NAME.name).withOnt(ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new ETyped(1, "A", ont.eType$(OntologyTestUtils.DRAGON.name), 2, 0),
                new Rel(2, ont.rType$(ORIGINATED_IN.getName()), Rel.Direction.R, null, 3, 0),
                new ETyped(3, "B", ont.eType$(OntologyTestUtils.KINGDOM.name), 4, 0),
                new Rel(4, ont.rType$(ORIGINATED_IN.getName()), Rel.Direction.L, null, 5, 0),
                new ETyped(5, "C", ont.eType$(OntologyTestUtils.DRAGON.name), 6, 0),
                new Quant1(6, QuantType.all,Arrays.asList(7,8,13),0),
                new EProp(7, NAME.type, Constraint.of(ConstraintOp.eq, "BB")),
                new Rel(8, ont.rType$(FIRE.getName()), Rel.Direction.R, null, 9, 0),
                new ETyped(9, "D", ont.eType$(OntologyTestUtils.DRAGON.name), 10, 0),
                new Rel(10, ont.rType$(ORIGINATED_IN.getName()), Rel.Direction.R, null, 11, 0),
                new ETyped(11, "E", ont.eType$(OntologyTestUtils.KINGDOM.name), 12, 0),
                new EProp(12, NAME.type, Constraint.of(ConstraintOp.eq, "AB")),
                new Rel(13, ont.rType$(FIRE.getName()), Rel.Direction.R, null, 14, 0),
                new EConcrete(14, "F", ont.eType$(OntologyTestUtils.DRAGON.name), "Dragon_1", "D0", 15, 0),
                new Rel(15, ont.rType$(FIRE.getName()), Rel.Direction.L, null, 16, 0),
                new ETyped(16, "B", ont.eType$(OntologyTestUtils.DRAGON.name), -1, 0)
        )).build();


        testAndAssertQuery(query, fuseClient);
    }


}
