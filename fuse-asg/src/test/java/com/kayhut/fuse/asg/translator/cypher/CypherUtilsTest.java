package com.kayhut.fuse.asg.translator.cypher;

import com.kayhut.fuse.asg.translator.cypher.strategies.CypherUtils;
import com.kayhut.fuse.asg.translator.cypher.strategies.MatchCypherTranslatorStrategy;
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
    public void test_A_OR_B_StripDown() {

        final Statement statement = new CypherParser().parse("MATCH (a)--(b) where a:Dragon Or b:Person RETURN a",Option.empty());
        final QueryPart part = ((Query) statement).part();
        final Seq<Clause> clauses = ((SingleQuery) part).clauses();
        final Optional<Clause> matchClause = asJavaCollectionConverter(clauses).asJavaCollection()
                .stream().filter(c -> c.getClass().isAssignableFrom(Match.class)).findAny();

        final Match match = (Match) matchClause.get();
        Where where = match.where().get();

        CypherUtils.reWrite(where.expression());

    }

    @Test
    public void test_A_OR_B_AND_C_StripDown() {

        final Statement statement = new CypherParser().parse("MATCH (a)-[c]-(b) where (a:Dragon Or b:Person) And c RETURN a",Option.empty());
        final QueryPart part = ((Query) statement).part();
        final Seq<Clause> clauses = ((SingleQuery) part).clauses();
        final Optional<Clause> matchClause = asJavaCollectionConverter(clauses).asJavaCollection()
                .stream().filter(c -> c.getClass().isAssignableFrom(Match.class)).findAny();

        final Match match = (Match) matchClause.get();
        Where where = match.where().get();

        CypherUtils.reWrite(where.expression());

    }
    //endregion

    private MatchCypherTranslatorStrategy match;

}