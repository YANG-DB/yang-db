package com.kayhut.fuse.asg.translator.cypher;

import com.kayhut.fuse.asg.translator.AsgTranslator;
import com.kayhut.fuse.asg.translator.cypher.strategies.MatchCypherTranslatorStrategy;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.model.query.quant.Quant1;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;

import static com.kayhut.fuse.model.asgQuery.AsgQuery.Builder.*;
import static com.kayhut.fuse.model.execution.plan.descriptors.AsgQueryDescriptor.print;
import static com.kayhut.fuse.model.query.properties.EProp.of;
import static com.kayhut.fuse.model.query.properties.constraint.Constraint.of;
import static com.kayhut.fuse.model.query.properties.constraint.ConstraintOp.*;
import static com.kayhut.fuse.model.query.quant.QuantType.all;
import static org.junit.Assert.assertEquals;

/**
 * Created by lior.perry
 */
public class CypherMatchMultiHopsWithWhereTest {
    //region Setup
    @Before
    public void setUp() throws Exception {
        match = new CypherTestUtils().setUp(readJsonToString("src/test/resources/Dragons_Ontology.json")).match;
    }
    //endregion

    @Test
    public void testMatch_4_hops_with_contains_and() {
        AsgTranslator<String, AsgQuery> translator = new CypherTranslator("Dragons", Collections.singleton(match));
        final AsgQuery query = translator.translate("MATCH (a)-[b]->(c)-[d]-(e)-[f]->(g)-[h]-(i) where (a.name CONTAINS 'jh') AND a:Horse RETURN a");
        String expected = "[└── Start, \n" +
                            "    ──UnTyp[:[] a#1]──Q[100:all]:{2|101}, \n" +
                            "                                    └-> Rel(:null b#2)──UnTyp[:[] c#3]──Q[300:all]:{4}, \n" +
                            "                                                                                  └<--Rel(:null d#4)──UnTyp[:[] e#5]──Q[500:all]:{6}, \n" +
                            "                                                                                                                                └-> Rel(:null f#6)──UnTyp[:[] g#7]──Q[700:all]:{8}, \n" +
                            "                                                                                                                                                                              └<--Rel(:null h#8)──UnTyp[:[] i#9], \n" +
                            "                                    └─?[..][101], \n" +
                            "                                            └─?[101]:[name<contains,jh>], \n" +
                            "                                            └─?[102]:[type<inSet,[Horse]>]]";
        assertEquals(expected, print(query));
    }

    @Test
    public void testMatch_4_hops_with_contains_or() {
        AsgTranslator<String, AsgQuery> translator = new CypherTranslator("Dragons", Collections.singleton(match));
        final AsgQuery query = translator.translate("MATCH (a)-[b]->(c)-[d]-(e)-[f]->(g)-[h]-(i) where (a.name CONTAINS 'jh') OR a:Horse RETURN a");
        String expected = "[└── Start, \n" +
                "    ──Q[900:some]:{10|23}, \n" +
                "                     └─UnTyp[:[] a#10]──Q[1000:all]:{12|1001}, \n" +
                "                                                         └-> Rel(:null b#12)──UnTyp[:[] c#13]──Q[1300:all]:{15}──Q[2300:all]:{25|2301}, \n" +
                "                                                                                                           └<--Rel(:null d#15)──UnTyp[:[] e#16]──Q[1600:all]:{18}, \n" +
                "                                                                                                                                                             └-> Rel(:null f#18)──UnTyp[:[] g#19]──Q[1900:all]:{21}, \n" +
                "                                                                                                                                                                                                               └<--Rel(:null h#21)──UnTyp[:[] i#22], \n" +
                "                                                         └─?[..][1001], \n" +
                "                                                                  └─?[1001]:[name<contains,jh>], \n" +
                "                     └─UnTyp[:[] a#23], \n" +
                "                                  └-> Rel(:null b#25)──UnTyp[:[] c#26]──Q[2600:all]:{28}, \n" +
                "                                                                                    └<--Rel(:null d#28)──UnTyp[:[] e#29]──Q[2900:all]:{31}, \n" +
                "                                                                                                                                      └-> Rel(:null f#31)──UnTyp[:[] g#32]──Q[3200:all]:{34}, \n" +
                "                                                                                                                                                                                        └<--Rel(:null h#34)──UnTyp[:[] i#35], \n" +
                "                                  └─?[..][2301], \n" +
                "                                           └─?[2301]:[type<inSet,[Horse]>]]";
        assertEquals(expected, print(query));
    }

    @Test
    public void testMatch_4_hops_with_contains_or_and() {
        AsgTranslator<String, AsgQuery> translator = new CypherTranslator("Dragons", Collections.singleton(match));
        final AsgQuery query = translator.translate("MATCH (a)-[b]->(c)-[d]-(e)-[f]->(g)-[h]-(i) where (a.name CONTAINS 'jh' and c.age > 100) OR (e:Horse and h:Fire) RETURN a");
        String expected = "[└── Start, \n" +
                            "    ──Q[900:some]:{10|23}, \n" +
                            "                     └─UnTyp[:[] a#10]──Q[1000:all]:{12}, \n" +
                            "                                                    └-> Rel(:null b#12)──UnTyp[:[] c#13]──Q[1300:all]:{15}──Q[2300:all]:{25|2301}, \n" +
                            "                                                                                                      └<--Rel(:null d#15)──UnTyp[:[] e#16]──Q[1600:all]:{18|1601}, \n" +
                            "                                                                                                                                                             └-> Rel(:null f#18)──UnTyp[:[] g#19]──Q[1900:all]:{21}, \n" +
                            "                                                                                                                                                                                                               └<--Rel(:null h#21)──UnTyp[:[] i#22], \n" +
                            "                                                                                                                                                                                                                              └─?[..][2100], \n" +
                            "                                                                                                                                                                                                                                       └─?[2101]:[type<inSet,[Fire]>], \n" +
                            "                                                                                                                                                             └─?[..][1601], \n" +
                            "                                                                                                                                                                      └─?[1601]:[type<inSet,[Horse]>], \n" +
                            "                     └─UnTyp[:[] a#23], \n" +
                            "                                  └-> Rel(:null b#25)──UnTyp[:[] c#26]──Q[2600:all]:{28|2601}, \n" +
                            "                                                                                         └<--Rel(:null d#28)──UnTyp[:[] e#29]──Q[2900:all]:{31}, \n" +
                            "                                                                                                                                           └-> Rel(:null f#31)──UnTyp[:[] g#32]──Q[3200:all]:{34}, \n" +
                            "                                                                                                                                                                                             └<--Rel(:null h#34)──UnTyp[:[] i#35], \n" +
                            "                                                                                         └─?[..][2601], \n" +
                            "                                                                                                  └─?[2601]:[age<gt,100>], \n" +
                            "                                  └─?[..][2301], \n" +
                            "                                           └─?[2301]:[name<contains,jh>]]";
        assertEquals(expected, print(query));
    }



    //region Private Methods
    private static String readJsonToString(String jsonRelativePath) {
        String contents = "";
        try {
            contents = new String(Files.readAllBytes(Paths.get(jsonRelativePath)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contents;
    }
    //endregion

    //region Fields
    private MatchCypherTranslatorStrategy match;
    //endregion

}