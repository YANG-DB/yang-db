package com.yangdb.fuse.asg.translator.cypher;

import com.yangdb.fuse.asg.translator.cypher.strategies.CypherUtils;
import com.yangdb.fuse.asg.translator.cypher.strategies.MatchCypherTranslatorStrategy;
import org.junit.Assert;
import org.junit.Test;
import org.opencypher.v9_0.ast.*;
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
    public void test_A_AND_B_OR_A_AND_B_StripDown() {
        final String originalWhere = "(a:Dragon AND b:Person) OR (b:Person AND a:Dragon) ";
        final String expectedWhere = "(a:Dragon & b:Person)";

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
    public void test_A_OR_B_AND_A_OR_B_StripDown() {
        final String originalWhere = "(a:Dragon Or b:Person) And (a:Dragon Or b:Person) ";
        final String expectedWhere = "(a:Dragon | b:Person)";

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
    public void test_A_OR_B_AND_C_StripDown() {
        final String originalWhere = "(a:Dragon Or b:Person) And c ";
        final String expectedWhere = "((a:Dragon & c) | (b:Person & c))";

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
        final String originalWhere = "(a:Dragon And b:Person) And c ";
        final String expectedWhere = "(a:Dragon & b:Person & c)";

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
//    @Ignore
    public void test_A_OR_B_AND_C_OR_D_StripDown() {
        final String originalWhere = "(a:Dragon Or b:Person) And (c:Fire Or d:Ice)";
        final String expectedWhere = "((a:Dragon & c:Fire) | (a:Dragon & d:Ice) | (b:Person & c:Fire) | (b:Person & d:Ice))";

        final Statement statement = new CypherParser().parse("MATCH (a)-[c]-(b)-[]-(d) where "+originalWhere+" RETURN a",Option.empty());
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
//    @Ignore
    public void test_A_AND_B_OR_C_AND_D_StripDown() {
        final String originalWhere = "(a:Dragon And b:Person) OR (c:Fire AND d:Ice)";
        final String expectedWhere = "((a:Dragon & b:Person) | (c:Fire & d:Ice))";

        final Statement statement = new CypherParser().parse("MATCH (a)-[c]-(b)-[]-(d) where "+originalWhere+" RETURN a",Option.empty());
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
    public void test_A_OR_B_AND_D_OR_C_OR_D_AND_B_StripDown() {
        final String originalWhere = "( ((a:Dragon Or b:Person) And d:Ice) Or ((c:Fire Or d:Ice) And b:Person) )";
        final String expectedWhere = "((a:Dragon & d:Ice) | (b:Person & c:Fire) | (b:Person & d:Ice))";

        final Statement statement = new CypherParser().parse("MATCH (a)-[c]-(b)-[]-(d) where "+originalWhere+" RETURN a",Option.empty());
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
    public void test_A_OR_A_AND_A_OR_A_OR_A_AND_A_StripDown() {
        final String originalWhere = "( ((a:Dragon Or a:Person) And a:Ice) Or ((a:Fire Or a:Ice) And a:Person) )";
        final String expectedWhere = "((a:Dragon & a:Ice) | (a:Fire & a:Person) | (a:Ice & a:Person))";

        final Statement statement = new CypherParser().parse("MATCH (a)-[c]-(b)-[]-(d) where "+originalWhere+" RETURN a",Option.empty());
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
    public void test_A_OR_A_OR_A_OR_A_StripDown() {
        final String originalWhere = "( (a:Dragon Or a:Person)  Or (a:Fire Or a:Ice) )";
        final String expectedWhere = "(a:Dragon | a:Fire | a:Ice | a:Person)";

        final Statement statement = new CypherParser().parse("MATCH (a)-[c]-(b)-[]-(d) where "+originalWhere+" RETURN a",Option.empty());
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