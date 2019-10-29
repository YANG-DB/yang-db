package com.yangdb.fuse.assembly.knowledge.asg;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yangdb.fuse.asg.validation.AsgQueryValidator;
import com.yangdb.fuse.dispatcher.ontology.OntologyProvider;
import com.yangdb.fuse.model.asgQuery.AsgEBase;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.asgQuery.AsgQueryUtil;
import com.yangdb.fuse.model.asgQuery.AsgStrategyContext;
import com.yangdb.fuse.model.execution.plan.descriptors.AsgQueryDescriptor;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.entity.EEntityBase;
import com.yangdb.fuse.model.query.properties.EProp;
import com.yangdb.fuse.model.query.properties.RelProp;
import com.yangdb.fuse.model.query.properties.constraint.Constraint;
import com.yangdb.fuse.model.query.properties.constraint.ConstraintOp;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.net.URL;
import java.util.*;

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
        URL knowledge = Thread.currentThread().getContextClassLoader().getResource("ontology/Knowledge.json");
        URL dragons = Thread.currentThread().getContextClassLoader().getResource("ontology/Dragons.json");
        knowledgeOnt = new Ontology.Accessor(new ObjectMapper().readValue(knowledge, Ontology.class));
        dragonsOnt = new Ontology.Accessor(new ObjectMapper().readValue(dragons, Ontology.class));
        provider = new OntologyProvider() {
            @Override
            public Ontology add(Ontology ontology) {
                return ontology;
            }
            @Override
            public Optional<Ontology> get(String id) {
                switch (id) {
                    case "Knowledge":
                        return Optional.of(knowledgeOnt.get());
                    case "Dragons":
                        return Optional.of(dragonsOnt.get());
                    default:
                        return Optional.of(knowledgeOnt.get());
                }
            }

            @Override
            public Collection<Ontology> getAll() {
                return Collections.singleton(knowledgeOnt.get());
            }

            @Override
            public Ontology add(Ontology ontology) {
                return ontology;
             }
        };

        validator = new AsgQueryValidator(new AsgKnowledgeValidatorStrategyRegistrar(), provider);
    }
    //endregion


    //region Test Methods
    @Test
    @Ignore("Fails currently during code changes")
    public void asgLogicalGraphQueryTransformationTest() throws Exception {
        AsgStrategyContext asgStrategyContext = new AsgStrategyContext(knowledgeOnt);
        KnowledgeLogicalEntityGraphTranslatorStrategy translatorStrategy = new KnowledgeLogicalEntityGraphTranslatorStrategy(provider, EEntityBase.class);

        String before = AsgQueryDescriptor.print(QIgnore());
        AsgQuery query = QIgnore();
        translatorStrategy.apply(query, asgStrategyContext);
        //assume no change when evalue is present in query
        assertEquals(before, AsgQueryDescriptor.print(query));

        query = QE0();
        String expected = "[└── Start, \n" +
                "    ──Typ[:Entity A#1]──Q[3:all]:{4}, \n" +
                "                                └-> Rel(:hasEvalue null#4)──Typ[:Evalue V.2#5]──?[..][2], \n" +
                "                                                                                    └─?[5]:[fieldId<eq,fName>], \n" +
                "                                                                                    └─?[2]:[stringValue<like,*>]]";

        //Applying the Strategy on the Eprop with the Epoch time
        translatorStrategy.apply(query, asgStrategyContext);
        assertEquals(expected, AsgQueryDescriptor.print(query));

        expected = "[└── Start, \n" +
                "    ──Typ[:Entity A#1]──Q[2:all]:{4}, \n" +
                "                                └-> Rel(:hasEvalue null#4)──Typ[:Evalue V.3#5]──?[..][3], \n" +
                "                                                                                    └─?[5]:[fieldId<eq,fName>], \n" +
                "                                                                                    └─?[3]:[stringValue<like,*>]]";
        asgStrategyContext = new AsgStrategyContext(knowledgeOnt);

        query = QE1();
        //Applying the Strategy on the Eprop with the Epoch time
        translatorStrategy.apply(query, asgStrategyContext);
        assertEquals(expected, AsgQueryDescriptor.print(query));

        expected = "[└── Start, \n" +
                "    ──Typ[:Entity A#1]──Q[2:all]:{5|6}, \n" +
                "                                  └─?[5]:[creationTime<like,*>], \n" +
                "                                  └-> Rel(:hasEvalue null#6)──Typ[:Evalue V.4#7]──?[..][4], \n" +
                "                                                                                      └─?[7]:[fieldId<eq,fName>], \n" +
                "                                                                                      └─?[4]:[stringValue<like,a>]]";
        asgStrategyContext = new AsgStrategyContext(knowledgeOnt);
        query = QE2();
        //Applying the Strategy on the Eprop with the Epoch time
        translatorStrategy.apply(query, asgStrategyContext);
        assertEquals(expected, AsgQueryDescriptor.print(query));


        expected = "[└── Start, \n" +
                "    ──Typ[:Entity A#1]──Q[3:all]:{5|6}, \n" +
                "                                  └─?[5]:[creationTime<like,*>], \n" +
                "                                  └-> Rel(:hasEvalue null#6)──Typ[:Evalue V.4#7]──?[..][4], \n" +
                "                                                                                      └─?[7]:[fieldId<eq,age>], \n" +
                "                                                                                      └─?[4]:[intValue<gt,10>]]";
        asgStrategyContext = new AsgStrategyContext(knowledgeOnt);

        query = QE3();

        //Applying the Strategy on the Eprop with the Epoch time
        translatorStrategy.apply(query, asgStrategyContext);
        assertEquals(expected, AsgQueryDescriptor.print(query));


        expected = "[└── Start, \n" +
                "    ──Typ[:Entity A#1]--> Rel(:hasEntity null#4)──Typ[:Entity B#5]──Q[6:all]:{9|10|12}, \n" +
                "                                                                                  └─?[9]:[creationTime<like,b*>], \n" +
                "                                                                                  └-> Rel(:hasEvalue null#10)──Typ[:Evalue V.7#11]──?[..][7], \n" +
                "                                                                                                                                        └─?[11]:[fieldId<eq,fName>]──Typ[:Evalue V.8#13]──?[..][8], \n" +
                "                                                                                                                                        └─?[7]:[stringValue<like,a>], \n" +
                "                                                                                  └-> Rel(:hasEvalue null#12), \n" +
                "                                                                                                         └─?[13]:[fieldId<eq,birth>], \n" +
                "                                                                                                         └─?[8]:[dateValue<lt,12/10/2000>]]";
        asgStrategyContext = new AsgStrategyContext(knowledgeOnt);

        query = QE4();
        //Applying the Strategy on the Eprop with the Epoch time
        translatorStrategy.apply(query, asgStrategyContext);
        assertEquals(expected, AsgQueryDescriptor.print(query));

        expected = "[└── Start, \n" +
                "    ──Typ[:Entity A#1]──Q[2:all]:{4|9|10|12|14}, \n" +
                "                                           └-> Rel(:hasEntity null#4)──Typ[:Entity B#5], \n" +
                "                                           └─?[9]:[creationTime<like,b*>], \n" +
                "                                           └-> Rel(:hasEvalue null#10)──Typ[:Evalue V.6#11]──?[..][6], \n" +
                "                                                                                                 └─?[11]:[fieldId<eq,fName>]──Typ[:Evalue V.7#13]──?[..][7], \n" +
                "                                                                                                 └─?[6]:[stringValue<like,a>]──Typ[:Evalue V.8#15]──?[..][8], \n" +
                "                                           └-> Rel(:hasEvalue null#12), \n" +
                "                                                                  └─?[13]:[fieldId<eq,birth>], \n" +
                "                                                                  └─?[7]:[dateValue<lt,12/10/2000>], \n" +
                "                                           └-> Rel(:hasEvalue null#14), \n" +
                "                                                                  └─?[15]:[fieldId<eq,fName>], \n" +
                "                                                                  └─?[8]:[creationTime<lt,12/10/2000>]]";
        asgStrategyContext = new AsgStrategyContext(knowledgeOnt);

        query = QE5();
        //Applying the Strategy on the Eprop with the Epoch time
        translatorStrategy.apply(query, asgStrategyContext);
        assertEquals(expected, AsgQueryDescriptor.print(query));

        expected = "[└── Start, \n" +
                "    ──Typ[:Entity A#1]──Q[2:all]:{3|13|16|18|20}, \n" +
                "                                            └-> Rel(:hasEntity null#3)──Typ[:Entity B#4]──Q[7:all]:{8|14}, \n" +
                "                                                                                                     └─?[8]:[creationUser<like,b*>], \n" +
                "                                                                                                     └-> Rel(:hasEvalue null#14)──Typ[:Evalue V.9#15]──?[..][9]──Typ[:Evalue V.10#17]──?[..][10], \n" +
                "                                                                                                                                                           └─?[15]:[fieldId<eq,fName>]──Typ[:Evalue V.11#19]──?[..][11], \n" +
                "                                                                                                                                                           └─?[9]:[stringValue<like,a>]──Typ[:Evalue V.12#21]──?[..][12], \n" +
                "                                            └─?[13]:[creationTime<like,b*>], \n" +
                "                                            └-> Rel(:hasEvalue null#16), \n" +
                "                                                                   └─?[17]:[fieldId<eq,fName>], \n" +
                "                                                                   └─?[10]:[stringValue<like,a>], \n" +
                "                                            └-> Rel(:hasEvalue null#18), \n" +
                "                                                                   └─?[19]:[fieldId<eq,birth>], \n" +
                "                                                                   └─?[11]:[dateValue<lt,12/10/2000>], \n" +
                "                                            └-> Rel(:hasEvalue null#20), \n" +
                "                                                                   └─?[21]:[fieldId<eq,fName>], \n" +
                "                                                                   └─?[12]:[creationTime<lt,12/10/2000>]]";
        asgStrategyContext = new AsgStrategyContext(knowledgeOnt);

        query = QE6();
        //Applying the Strategy on the Eprop with the Epoch time
        translatorStrategy.apply(query, asgStrategyContext);
        assertEquals(expected, AsgQueryDescriptor.print(query));

        expected = "[└── Start, \n" +
                "    ──Typ[:Entity A#1]──Q[3:all]:{4}, \n" +
                "                                └-> Rel(:hasEvalue null#4)──Typ[:Evalue V.2#5]──?[2]:[stringValue<like,*>]]";
        asgStrategyContext = new AsgStrategyContext(knowledgeOnt);

        query = QE7();
        //Applying the Strategy on the Eprop with the Epoch time
        translatorStrategy.apply(query, asgStrategyContext);
        assertEquals(expected, AsgQueryDescriptor.print(query));


        expected = "[└── Start, \n" +
                "    ──Typ[:Entity A#1]──Q[3:all]:{5}, \n" +
                "                                └-> Rel(:hasEvalue null#5)──Typ[:Evalue V.4#6]──?[4]:[stringValue<like,*>]]";
        asgStrategyContext = new AsgStrategyContext(knowledgeOnt);

        query = QE8();
        //Applying the Strategy on the Eprop with the Epoch time
        translatorStrategy.apply(query, asgStrategyContext);
        assertEquals(expected, AsgQueryDescriptor.print(query));

    }

    @Test
    public void asgLogicalDragonGraphQueryTransformationTest() throws Exception {
        AsgStrategyContext asgStrategyContext = new AsgStrategyContext(knowledgeOnt);
        KnowledgeLogicalEntityGraphTranslatorStrategy translatorStrategy = new KnowledgeLogicalEntityGraphTranslatorStrategy(provider, EEntityBase.class);

        String before = AsgQueryDescriptor.print(QIgnore());
        AsgQuery query = QIgnore();
        translatorStrategy.apply(query, asgStrategyContext);
        //assume no change when evalue is present in query
        assertEquals(before, AsgQueryDescriptor.print(query));

        String expected = "[└── Start, \n" +
                "    ──Typ[:Entity A#1]──Q[3:all]:{2|4}, \n" +
                "                                  └-> Rel(:relatedEntity null#2), \n" +
                "                                  └─?[4]:[category<eq,Person>]]";
        asgStrategyContext = new AsgStrategyContext(knowledgeOnt);

        //Applying the Strategy on the Eprop with the Epoch time
        query = QFail();
        translatorStrategy.apply(query, asgStrategyContext);
        assertEquals(expected, AsgQueryDescriptor.print(query));
        assertFalse(validator.validate(query).valid());
        assertEquals(validator.validate(query).errors().iterator().next(),"Ontology doesn't Allow Relation with No entity Attached to :Asg(Rel(2))");


        expected = "[└── Start, \n" +
                "    ──Typ[:Entity A#1]──Q[4:all]:{2|5}, \n" +
                "                                  └-> Rel(:relatedEntity null#2)──Typ[:Entity B#3]──Q[6:all]:{7}, \n" +
                "                                                                                            └─?[7]:[category<eq,Horse>], \n" +
                "                                  └─?[5]:[category<eq,Person>]]";
        asgStrategyContext = new AsgStrategyContext(knowledgeOnt);

        //Applying the Strategy on the Eprop with the Epoch time
        query = QE0L();
        translatorStrategy.apply(query, asgStrategyContext);
        assertTrue(validator.validate(query).valid());
        assertEquals(expected, AsgQueryDescriptor.print(query));

        //validate rel type was included in the logical to knowledge translation as below child
        AsgEBase<Rel> relAsgEBase = AsgQueryUtil.element(query, Rel.class).get();
        Assert.assertEquals(1,relAsgEBase.getB().size());
        Assert.assertEquals("category",((RelProp)AsgQueryUtil.element(query, Rel.class).get().getB().get(0).geteBase()).getpType());
        assertTrue(validator.validate(query).valid());


        expected = "[└── Start, \n" +
                "    ──Typ[:Entity A#1]──Q[2:all]:{4|6}, \n" +
                "                                  └-> Rel(:hasEvalue null#4)──Typ[:Evalue V.3#5]──?[..][3], \n" +
                "                                                                                      └─?[5]:[fieldId<eq,deathDate>], \n" +
                "                                                                                      └─?[3]:[dateValue<gt,Sat Jan 01 00:00:00 IST 2000>], \n" +
                "                                  └─?[6]:[category<eq,Person>]]";
        asgStrategyContext = new AsgStrategyContext(knowledgeOnt);

        query = QE1L();
        //Applying the Strategy on the Eprop with the Epoch time
        translatorStrategy.apply(query, asgStrategyContext);
        assertTrue(validator.validate(query).valid());
        assertEquals(expected, AsgQueryDescriptor.print(query));
        //validate rel type was included in the logical to knowledge translation as below child
        Assert.assertFalse(AsgQueryUtil.elements(query, Rel.class).stream().anyMatch(r->!r.getB().isEmpty()));

        expected = "[└── Start, \n" +
                "    ──Typ[:Entity A#1]──Q[2:all]:{5|6|8}, \n" +
                "                                    └─?[5]:[creationTime<like,*>], \n" +
                "                                    └-> Rel(:hasEvalue null#6)──Typ[:Evalue V.4#7]──?[..][4], \n" +
                "                                                                                        └─?[7]:[fieldId<eq,name>], \n" +
                "                                                                                        └─?[4]:[stringValue<like,a>], \n" +
                "                                    └─?[8]:[category<eq,Person>]]";
        asgStrategyContext = new AsgStrategyContext(knowledgeOnt);
        query = QE2L();
        //Applying the Strategy on the Eprop with the Epoch time
        translatorStrategy.apply(query, asgStrategyContext);
        assertTrue(validator.validate(query).valid());
        assertEquals(expected, AsgQueryDescriptor.print(query));


        expected = "[└── Start, \n" +
                "    ──Typ[:Entity A#1]──Q[3:all]:{5|6|8}, \n" +
                "                                    └─?[5]:[creationTime<like,*>], \n" +
                "                                    └-> Rel(:hasEvalue null#6)──Typ[:Evalue V.4#7]──?[..][4], \n" +
                "                                                                                        └─?[7]:[fieldId<eq,height>], \n" +
                "                                                                                        └─?[4]:[intValue<gt,150>], \n" +
                "                                    └─?[8]:[category<eq,Person>]]";
        asgStrategyContext = new AsgStrategyContext(knowledgeOnt);

        query = QE3L();

        //Applying the Strategy on the Eprop with the Epoch time
        translatorStrategy.apply(query, asgStrategyContext);
        assertTrue(validator.validate(query).valid());
        assertEquals(expected, AsgQueryDescriptor.print(query));


        expected = "[└── Start, \n" +
                "    ──Typ[:Entity A#1]──Q[14:all]:{4|15}, \n" +
                "                                    └-> Rel(:relatedEntity null#4)──Typ[:Entity B#5]──Q[6:all]:{9|10|12|16}, \n" +
                "                                                                                                       └─?[9]:[creationTime<gt,Sat Jan 01 00:00:00 IST 2000>], \n" +
                "                                                                                                       └-> Rel(:hasEvalue null#10)──Typ[:Evalue V.7#11]──?[..][7], \n" +
                "                                                                                                                                                             └─?[11]:[fieldId<eq,color>]──Typ[:Evalue V.8#13]──?[..][8], \n" +
                "                                                                                                                                                             └─?[7]:[stringValue<like,a>], \n" +
                "                                                                                                       └-> Rel(:hasEvalue null#12), \n" +
                "                                                                                                                              └─?[13]:[fieldId<eq,birthDate>], \n" +
                "                                                                                                                              └─?[8]:[dateValue<lt,12/10/2000>], \n" +
                "                                                                                                       └─?[16]:[category<eq,Dragon>], \n" +
                "                                    └─?[15]:[category<eq,Dragon>]]";
        asgStrategyContext = new AsgStrategyContext(knowledgeOnt);

        query = QE4L();
        //Applying the Strategy on the Eprop with the Epoch time
        translatorStrategy.apply(query, asgStrategyContext);
        assertTrue(validator.validate(query).valid());
        assertEquals(expected, AsgQueryDescriptor.print(query));
        //validate rel type was included in the logical to knowledge translation as below child
        List<AsgEBase<Rel>> elements = AsgQueryUtil.elements(query, Rel.class);
        elements.stream()
                .filter(r->r.geteBase().getrType().equals("relatedEntity"))
                .forEach(r-> {
                    Assert.assertEquals(1, r.getB().size());
                    Assert.assertEquals("category", ((RelProp) r.getB().get(0).geteBase()).getpType());
                });

        expected = "[└── Start, \n" +
                "    ──Typ[:Entity A#1]──Q[2:all]:{3|12|15|17|19}, \n" +
                "                                            └-> Rel(:relatedEntity null#3)──Typ[:Entity B#4]──Q[7:all]:{8|13|20}, \n" +
                "                                                                                                            └─?[8]:[creationUser<like,b*>], \n" +
                "                                                                                                            └-> Rel(:hasEvalue null#13)──Typ[:Evalue V.9#14]──?[..][9]──Typ[:Evalue V.10#16]──?[..][10], \n" +
                "                                                                                                                                                                  └─?[14]:[fieldId<eq,name>]──Typ[:Evalue V.11#18]──?[..][11], \n" +
                "                                                                                                                                                                  └─?[9]:[stringValue<like,a>], \n" +
                "                                                                                                            └─?[20]:[category<eq,Dragon>], \n" +
                "                                            └─?[12]:[creationTime<like,b*>], \n" +
                "                                            └-> Rel(:hasEvalue null#15), \n" +
                "                                                                   └─?[16]:[fieldId<eq,firstName>], \n" +
                "                                                                   └─?[10]:[stringValue<like,a>], \n" +
                "                                            └-> Rel(:hasEvalue null#17), \n" +
                "                                                                   └─?[18]:[fieldId<eq,birthDate>], \n" +
                "                                                                   └─?[11]:[dateValue<lt,12/10/2000>], \n" +
                "                                            └─?[19]:[category<eq,Person>]]";
        asgStrategyContext = new AsgStrategyContext(knowledgeOnt);

        query = QE6L();
        //Applying the Strategy on the Eprop with the Epoch time
        translatorStrategy.apply(query, asgStrategyContext);
        assertTrue(validator.validate(query).valid());
        assertEquals(expected, AsgQueryDescriptor.print(query));

        //validate rel type was included in the logical to knowledge translation as below child
        elements = AsgQueryUtil.elements(query, Rel.class);
        elements.stream()
                .filter(r->r.geteBase().getrType().equals("relatedEntity"))
                .forEach(r-> {
                    Assert.assertEquals(1, r.getB().size());
                    Assert.assertEquals("category", ((RelProp) r.getB().get(0).geteBase()).getpType());
                });


    }

    private AsgQuery QIgnore() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "Knowledge")
                .next(typed(1, "Evalue", "A"))
                .next(eProp(2, "fieldId", Constraint.of(ConstraintOp.like, "*")))
                .build();
        return asgQuery;
    }
    private AsgQuery QFail() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "Dragons")
                .next(typed(1, "Person", "A"))
                .next(rel(2, "own", Rel.Direction.R))
                .build();
        return asgQuery;
    }

    private AsgQuery QE0() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "Knowledge")
                .next(typed(1, "Entity", "A"))
                .next(eProp(2, "fName.stringValue", Constraint.of(ConstraintOp.like, "*")))
                .build();
        return asgQuery;
    }

    private AsgQuery QE0L() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "Dragons")
                .next(typed(1, "Person", "A"))
                .next(rel(2, "own", Rel.Direction.R))
                .next(typed(3, "Horse", "B"))
                .build();
        return asgQuery;
    }

    private AsgQuery QE1() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "Knowledge")
                .next(typed(1, "Entity", "A"))
                .next(quant1(2, all))
                .in(eProp(3, "fName.stringValue", Constraint.of(ConstraintOp.like, "*")))
                .build();
        return asgQuery;
    }
    private AsgQuery QE1L() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "Dragons")
                .next(typed(1, "Person", "A"))
                .next(quant1(2, all))
                .in(eProp(3, "deathDate", Constraint.of(ConstraintOp.gt, new Date("1/1/2000"))))
                .build();
        return asgQuery;
    }

    private AsgQuery QE2() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "Knowledge")
                .next(typed(1, "Entity", "A"))
                .next(quant1(2, all))
                .in(ePropGroup(3, EProp.of(3, "fName.stringValue", Constraint.of(ConstraintOp.like, "a")),
                        EProp.of(3, "creationTime", Constraint.of(ConstraintOp.like, "*"))))
                .build();
        return asgQuery;
    }

    private AsgQuery QE2L() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "Dragons")
                .next(typed(1, "Person", "A"))
                .next(quant1(2, all))
                .in(ePropGroup(3, EProp.of(3, "name", Constraint.of(ConstraintOp.like, "a")),
                        EProp.of(3, "creationTime", Constraint.of(ConstraintOp.like, "*"))))
                .build();
        return asgQuery;
    }

    private AsgQuery QE3() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "Knowledge")
                .next(typed(1, "Entity", "A"))
                .next(ePropGroup(2,
                        EProp.of(2, "age.intValue", Constraint.of(ConstraintOp.gt, 10)),
                        EProp.of(2, "creationTime", Constraint.of(ConstraintOp.like, "*"))))
                .build();
        return asgQuery;
    }
    private AsgQuery QE3L() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "Dragons")
                .next(typed(1, "Person", "A"))
                .next(ePropGroup(2,
                        EProp.of(2, "height", Constraint.of(ConstraintOp.gt, 150)),
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
                        EProp.of(3, "fName.stringValue", Constraint.of(ConstraintOp.like, "a")),
                        EProp.of(3, "birth.dateValue", Constraint.of(ConstraintOp.lt, "12/10/2000")),
                        EProp.of(3, "creationTime", Constraint.of(ConstraintOp.like, "b*"))))
                .build();
        return asgQuery;
    }

    private AsgQuery QE4L() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "Dragons")
                .next(typed(1, "Dragon", "A"))
                .next(rel(4, "fire", Rel.Direction.R))
                .next(typed(5, "Dragon", "B"))
                .next(ePropGroup(3,
                        EProp.of(3, "color", Constraint.of(ConstraintOp.like, "a")),
                        EProp.of(3, "birthDate", Constraint.of(ConstraintOp.lt, "12/10/2000")),
                        EProp.of(3, "creationTime", Constraint.of(ConstraintOp.gt, new Date("1/1/2000")))))
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
                                EProp.of(3, "fName.stringValue", Constraint.of(ConstraintOp.like, "a")),
                                EProp.of(3, "birth.dateValue", Constraint.of(ConstraintOp.lt, "12/10/2000")),
                                EProp.of(3, "fName.creationTime", Constraint.of(ConstraintOp.lt, "12/10/2000")),
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
                                        EProp.of(5, "fName.stringValue", Constraint.of(ConstraintOp.like, "a"))))),
                        ePropGroup(6,
                                EProp.of(6, "fName.stringValue", Constraint.of(ConstraintOp.like, "a")),
                                EProp.of(6, "birth.dateValue", Constraint.of(ConstraintOp.lt, "12/10/2000")),
                                EProp.of(6, "fName.creationTime", Constraint.of(ConstraintOp.lt, "12/10/2000")),
                                EProp.of(6, "creationTime", Constraint.of(ConstraintOp.like, "b*"))))
                .build();
        return asgQuery;
    }

    private AsgQuery QE6L() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "Dragons")
                .next(typed(1, "Person", "A"))
                .next(quant1(2, all))
                .in(
                        rel(3, "own", Rel.Direction.R)
                                .next(typed(4, "Dragon", "B")
                                .next(ePropGroup(5,
                                        EProp.of(5, "creationUser", Constraint.of(ConstraintOp.like, "b*")),
                                        EProp.of(5, "name", Constraint.of(ConstraintOp.like, "a"))))),
                        ePropGroup(6,
                                EProp.of(6, "firstName", Constraint.of(ConstraintOp.like, "a")),
                                EProp.of(6, "birthDate", Constraint.of(ConstraintOp.lt, "12/10/2000")),
                                EProp.of(6, "creationTime", Constraint.of(ConstraintOp.like, "b*"))))
                .build();
        return asgQuery;
    }



    private AsgQuery QE7() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "Knowledge")
                .next(typed(1, "Entity", "A"))
                .next(eProp(2, "stringValue", Constraint.of(ConstraintOp.like, "*")))
                .build();
        return asgQuery;
    }
    private AsgQuery QE8() {
        AsgQuery asgQuery = AsgQuery.Builder.start("query1", "Knowledge")
                .next(typed(1, "Entity", "A"))
                .next(ePropGroup(2,EProp.of(3, "stringValue", Constraint.of(ConstraintOp.like, "*"))))
                .build();
        return asgQuery;
    }

    //endregion

    //region Fields
    private Ontology.Accessor knowledgeOnt;
    private Ontology.Accessor dragonsOnt;
    private OntologyProvider provider;
    private AsgQueryValidator validator;
    //endregion

}