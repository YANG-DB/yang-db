package com.kayhut.fuse.asg.translator.cypher;

import com.kayhut.fuse.asg.translator.cypher.strategies.CypherUtils;
import com.kayhut.fuse.asg.translator.cypher.strategies.MatchCypherTranslatorStrategy;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.opencypher.v9_0.ast.*;
import org.opencypher.v9_0.expressions.Expression;
import org.opencypher.v9_0.parser.CypherParser;
import scala.Option;
import scala.collection.Seq;

import java.util.Optional;

import static scala.collection.JavaConverters.asJavaCollectionConverter;

/**
 * Created by lior.perry
 */
public class CypherUtilsTest {


    //region Test Methods

    @Test
    public void test_A_OR_B_AND_C_StripDown() {
        final String originalWhere = "(a:Dragon Or b:Person) And c ";
        final String expectedWhere = "a:Dragon AND c OR b:Person AND c";

        final Statement statement = new CypherParser().parse("MATCH (a)-[c]-(b) where "+originalWhere+" RETURN a",Option.empty());
        final QueryPart part = ((Query) statement).part();
        final Seq<Clause> clauses = ((SingleQuery) part).clauses();
        final Optional<Clause> matchClause = asJavaCollectionConverter(clauses).asJavaCollection()
                .stream().filter(c -> c.getClass().isAssignableFrom(Match.class)).findAny();

        final Match match = (Match) matchClause.get();
        Where where = match.where().get();

        final com.bpodgursky.jbool_expressions.Expression reWriteWhere = CypherUtils.reWrite(where.expression());

        System.out.println("Origin:"+where.expression().asCanonicalStringVal());
        System.out.println("Simplified:"+reWriteWhere.toLexicographicString());
        Assert.assertEquals(expectedWhere,reWriteWhere.toLexicographicString());

    }

    @Test
    public void test_A_AND_B_AND_C_StripDown() {
        final String originalWhere = "(a:Dragon AND b:Person) And c ";
        final String expectedWhere = "a:Dragon AND c AND b:Person AND c";

        final Statement statement = new CypherParser().parse("MATCH (a)-[c]-(b) where "+originalWhere+" RETURN a",Option.empty());
        final QueryPart part = ((Query) statement).part();
        final Seq<Clause> clauses = ((SingleQuery) part).clauses();
        final Optional<Clause> matchClause = asJavaCollectionConverter(clauses).asJavaCollection()
                .stream().filter(c -> c.getClass().isAssignableFrom(Match.class)).findAny();

        final Match match = (Match) matchClause.get();
        Where where = match.where().get();

        final com.bpodgursky.jbool_expressions.Expression reWriteWhere = CypherUtils.reWrite(where.expression());

        System.out.println("Origin:"+where.expression().asCanonicalStringVal());
        System.out.println("Simplified:"+reWriteWhere.toLexicographicString());
        Assert.assertEquals(expectedWhere,reWriteWhere.toLexicographicString());

    }

    @Test
    @Ignore
    public void test_A_OR_B_AND_C_OR_D_StripDown() {
        final String originalWhere = "(a:Dragon Or b:Person) And (c:Fire Or d:Ice)";
        final String expectedWhere = "a:Dragon AND c:Fire OR a:Dragon AND d:Ice OR b:Person AND c:Fire OR b:Person AND d:Ice";

        final Statement statement = new CypherParser().parse("MATCH (a)-[c]-(b)-[]-(d) where "+expectedWhere+" RETURN a",Option.empty());
        final QueryPart part = ((Query) statement).part();
        final Seq<Clause> clauses = ((SingleQuery) part).clauses();
        final Optional<Clause> matchClause = asJavaCollectionConverter(clauses).asJavaCollection()
                .stream().filter(c -> c.getClass().isAssignableFrom(Match.class)).findAny();

        final Match match = (Match) matchClause.get();
        Where where = match.where().get();

        final com.bpodgursky.jbool_expressions.Expression reWriteWhere = CypherUtils.reWrite(where.expression());

        System.out.println("Origin:"+where.expression().asCanonicalStringVal());
        System.out.println("Simplified:"+reWriteWhere.toLexicographicString());
        Assert.assertEquals(expectedWhere,reWriteWhere.toLexicographicString());

    }
    //endregion

    private MatchCypherTranslatorStrategy match;

}