package com.yangdb.fuse.assembly.knowledge.asg;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yangdb.fuse.dispatcher.ontology.OntologyProvider;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.asgQuery.AsgStrategyContext;
import com.yangdb.fuse.model.execution.plan.descriptors.AsgQueryDescriptor;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.entity.EEntityBase;
import com.yangdb.fuse.model.query.properties.EProp;
import com.yangdb.fuse.model.query.properties.constraint.Constraint;
import com.yangdb.fuse.model.query.properties.constraint.ConstraintOp;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static com.yangdb.fuse.model.asgQuery.AsgQuery.Builder.*;
import static com.yangdb.fuse.model.query.quant.QuantType.all;
import static org.junit.Assert.*;

/**
 * Created by benishue on 09-May-17.
 */
public class KnowledgeLogicalGraphTranslatorStrategyTest {
    //region Setup
    @Before
    public void setUp() throws Exception {
        URL resource = Thread.currentThread().getContextClassLoader().getResource("ontology/Knowledge.json");
        ont = new Ontology.Accessor(new ObjectMapper().readValue(resource, Ontology.class));
        provider = new OntologyProvider() {
            @Override
            public Optional<Ontology> get(String id) {
                return Optional.of(ont.get());
            }

            @Override
            public Collection<Ontology> getAll() {
                return Collections.singleton(ont.get());
            }
        };

    }
    //endregion


    //region Test Methods
    @Test
    public void asgLogicalGraphQueryTransformationTest() throws Exception {
        AsgStrategyContext asgStrategyContext = new AsgStrategyContext(ont);
        KnowledgeLogicalEntityGraphTranslatorStrategy translatorStrategy = new KnowledgeLogicalEntityGraphTranslatorStrategy(provider, "Evalue", EEntityBase.class);

        String before = AsgQueryDescriptor.print(QIgnore());
        AsgQuery query = QIgnore();
        translatorStrategy.apply(query, asgStrategyContext);
        //assume no change when evalue is present in query
        assertEquals(before, AsgQueryDescriptor.print(query));

        query = QE0();
        String expected = "[└── Start, \n" +
                "    ──Typ[:Entity A#1]──Q[3:all]:{4}, \n" +
                "                                └-> Rel(:hasEvalue null#4)──Typ[:Evalue V.2#5]──?[2]:[fName<like,*>]]";

        //Applying the Strategy on the Eprop with the Epoch time
        translatorStrategy.apply(query, asgStrategyContext);
        assertEquals(expected, AsgQueryDescriptor.print(query));

        expected = "[└── Start, \n" +
                "    ──Typ[:Entity A#1]──Q[2:all]:{4}, \n" +
                "                                └-> Rel(:hasEvalue null#4)──Typ[:Evalue V.3#5]──?[3]:[fName<like,*>]]";
        asgStrategyContext = new AsgStrategyContext(ont);

        query = QE1();
        //Applying the Strategy on the Eprop with the Epoch time
        translatorStrategy.apply(query, asgStrategyContext);
        assertEquals(expected, AsgQueryDescriptor.print(query));

        expected = "[└── Start, \n" +
                "    ──Typ[:Entity A#1]──Q[2:all]:{5|6}, \n" +
                "                                  └─?[5]:[creationTime<like,*>], \n" +
                "                                  └-> Rel(:hasEvalue null#6)──Typ[:Evalue V.4#7]──?[4]:[fName<like,a>]]";
        asgStrategyContext = new AsgStrategyContext(ont);
        query = QE2();
        //Applying the Strategy on the Eprop with the Epoch time
        translatorStrategy.apply(query, asgStrategyContext);
        assertEquals(expected, AsgQueryDescriptor.print(query));


        expected = "[└── Start, \n" +
                "    ──Typ[:Entity A#1]──Q[3:all]:{5|6}, \n" +
                "                                  └─?[5]:[creationTime<like,*>], \n" +
                "                                  └-> Rel(:hasEvalue null#6)──Typ[:Evalue V.4#7]──?[4]:[fName<like,a>]]";
        asgStrategyContext = new AsgStrategyContext(ont);

        query = QE3();

        //Applying the Strategy on the Eprop with the Epoch time
        translatorStrategy.apply(query, asgStrategyContext);
        assertEquals(expected, AsgQueryDescriptor.print(query));


        expected = "[└── Start, \n" +
                "    ──Typ[:Entity A#1]--> Rel(:hasEntity null#4)──Typ[:Entity B#5]──Q[6:all]:{9|10|12}, \n" +
                "                                                                                  └─?[9]:[creationTime<like,b*>], \n" +
                "                                                                                  └-> Rel(:hasEvalue null#10)──Typ[:Evalue V.7#11]──?[7]:[fName<like,a>], \n" +
                "                                                                                  └-> Rel(:hasEvalue null#12)──Typ[:Evalue V.8#13]──?[8]:[lName<ne,alice>]]";
        asgStrategyContext = new AsgStrategyContext(ont);

        query = QE4();
        //Applying the Strategy on the Eprop with the Epoch time
        translatorStrategy.apply(query, asgStrategyContext);
        assertEquals(expected, AsgQueryDescriptor.print(query));

        expected = "[└── Start, \n" +
                "    ──Typ[:Entity A#1]──Q[2:all]:{4|8|9|11}, \n" +
                "                                       └-> Rel(:hasEntity null#4)──Typ[:Entity B#5], \n" +
                "                                       └─?[8]:[creationTime<like,b*>], \n" +
                "                                       └-> Rel(:hasEvalue null#9)──Typ[:Evalue V.6#10]──?[6]:[fName<like,a>], \n" +
                "                                       └-> Rel(:hasEvalue null#11)──Typ[:Evalue V.7#12]──?[7]:[lName<ne,alice>]]";
        asgStrategyContext = new AsgStrategyContext(ont);

        query = QE5();
        //Applying the Strategy on the Eprop with the Epoch time
        translatorStrategy.apply(query, asgStrategyContext);
        assertEquals(expected, AsgQueryDescriptor.print(query));

        expected = "[└── Start, \n" +
                "    ──Typ[:Entity A#1]──Q[2:all]:{3|12|15|17}, \n" +
                "                                         └-> Rel(:hasEntity null#3)──Typ[:Entity B#4]──Q[7:all]:{8|13}, \n" +
                "                                                                                                  └─?[8]:[creationUser<like,b*>], \n" +
                "                                                                                                  └-> Rel(:hasEvalue null#13)──Typ[:Evalue V.9#14]──?[9]:[fName<like,a>]──Typ[:Evalue V.10#16]──?[10]:[fName<like,a>], \n" +
                "                                         └─?[12]:[creationTime<like,b*>]──Typ[:Evalue V.11#18]──?[11]:[lName<ne,alice>], \n" +
                "                                         └-> Rel(:hasEvalue null#15), \n" +
                "                                         └-> Rel(:hasEvalue null#17)]";
        asgStrategyContext = new AsgStrategyContext(ont);

        query = QE6();
        //Applying the Strategy on the Eprop with the Epoch time
        translatorStrategy.apply(query, asgStrategyContext);
        assertEquals(expected, AsgQueryDescriptor.print(query));

    }

    private AsgQuery QIgnore() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "Knowledge")
                .next(typed(1, "Evalue", "A"))
                .next(eProp(2, "fName", Constraint.of(ConstraintOp.like, "*")))
                .build();
        return asgQuery;
    }

    private AsgQuery QR0() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "Knowledge")
                .next(typed(1, "Rel", "A"))
                .next(eProp(2, "fName", Constraint.of(ConstraintOp.like, "*")))
                .build();
        return asgQuery;
    }

    private AsgQuery QE0() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "Knowledge")
                .next(typed(1, "Entity", "A"))
                .next(eProp(2, "fName", Constraint.of(ConstraintOp.like, "*")))
                .build();
        return asgQuery;
    }

    private AsgQuery QE1() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "Knowledge")
                .next(typed(1, "Entity", "A"))
                .next(quant1(2, all))
                .in(eProp(3, "fName", Constraint.of(ConstraintOp.like, "*")))
                .build();
        return asgQuery;
    }

    private AsgQuery QE2() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "Knowledge")
                .next(typed(1, "Entity", "A"))
                .next(quant1(2, all))
                .in(ePropGroup(3, EProp.of(3, "fName", Constraint.of(ConstraintOp.like, "a")),
                        EProp.of(3, "creationTime", Constraint.of(ConstraintOp.like, "*"))))
                .build();
        return asgQuery;
    }

    private AsgQuery QE3() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "Knowledge")
                .next(typed(1, "Entity", "A"))
                .next(ePropGroup(2,
                        EProp.of(2, "fName", Constraint.of(ConstraintOp.like, "a")),
                        EProp.of(2, "creationTime", Constraint.of(ConstraintOp.like, "*"))))
                .build();
        return asgQuery;
    }

    private AsgQuery QE4() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "Knowledge")
                .next(typed(1, "Entity", "A"))
                .next(rel(4, "hasEntity", Rel.Direction.R))
                .next(typed(5, "Entity", "B"))
                .next(ePropGroup(3,
                        EProp.of(3, "fName", Constraint.of(ConstraintOp.like, "a")),
                        EProp.of(3, "lName", Constraint.of(ConstraintOp.ne, "alice")),
                        EProp.of(3, "creationTime", Constraint.of(ConstraintOp.like, "b*"))))
                .build();
        return asgQuery;
    }

    private AsgQuery QE5() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "Knowledge")
                .next(typed(1, "Entity", "A"))
                .next(quant1(2, all))
                .in(
                        rel(4, "hasEntity", Rel.Direction.R).next(typed(5, "Entity", "B")),
                        ePropGroup(3,
                                EProp.of(3, "fName", Constraint.of(ConstraintOp.like, "a")),
                                EProp.of(3, "lName", Constraint.of(ConstraintOp.ne, "alice")),
                                EProp.of(3, "creationTime", Constraint.of(ConstraintOp.like, "b*"))))
                .build();
        return asgQuery;
    }

    private AsgQuery QE6() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "Knowledge")
                .next(typed(1, "Entity", "A"))
                .next(quant1(2, all))
                .in(
                        rel(3, "hasEntity", Rel.Direction.R).next(typed(4, "Entity", "B")
                                .next(ePropGroup(5,
                                        EProp.of(5, "creationUser", Constraint.of(ConstraintOp.like, "b*")),
                                        EProp.of(5, "fName", Constraint.of(ConstraintOp.like, "a"))))),
                        ePropGroup(6,
                                EProp.of(6, "fName", Constraint.of(ConstraintOp.like, "a")),
                                EProp.of(6, "lName", Constraint.of(ConstraintOp.ne, "alice")),
                                EProp.of(6, "creationTime", Constraint.of(ConstraintOp.like, "b*"))))
                .build();
        return asgQuery;
    }


    //endregion

    //region Fields
    private Ontology.Accessor ont;
    private OntologyProvider provider;
    //endregion

}