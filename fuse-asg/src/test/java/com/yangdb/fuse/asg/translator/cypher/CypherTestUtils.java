package com.yangdb.fuse.asg.translator.cypher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yangdb.fuse.asg.translator.cypher.strategies.CypherElementTranslatorStrategy;
import com.yangdb.fuse.asg.translator.cypher.strategies.MatchCypherTranslatorStrategy;
import com.yangdb.fuse.asg.translator.cypher.strategies.NodePatternCypherTranslatorStrategy;
import com.yangdb.fuse.asg.translator.cypher.strategies.StepPatternCypherTranslatorStrategy;
import com.yangdb.fuse.asg.translator.cypher.strategies.expressions.*;
import com.yangdb.fuse.model.ontology.Ontology;
import org.junit.Before;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class CypherTestUtils {

    @Before
    public CypherTestUtils setUp(String ontologyExpectedJson) throws Exception {
        ont = new Ontology.Accessor(new ObjectMapper().readValue(ontologyExpectedJson, Ontology.class));
        //translators
        translatorStrategies = Arrays.asList(
                new NodePatternCypherTranslatorStrategy(new EqualityExpression()),
                new StepPatternCypherTranslatorStrategy(
                        new NodePatternCypherTranslatorStrategy(new EqualityExpression()),
                        new EqualityExpression()
                ));

        //expressions
        whereExpressionStrategies = new ArrayList<>();
        whereExpressionStrategies.add(new OrExpression(whereExpressionStrategies));
        whereExpressionStrategies.add(new AndExpression(whereExpressionStrategies));
        whereExpressionStrategies.add(new HasLabelExpression());
        whereExpressionStrategies.add(new HasRelationLabelExpression());
        whereExpressionStrategies.add(new InequalityExpression());
        whereExpressionStrategies.add(new EqualityExpression());
        whereExpressionStrategies.add(new NotEqualExpression());
        whereExpressionStrategies.add(new StartsWithExpression());
        whereExpressionStrategies.add(new EndsWithExpression());
        whereExpressionStrategies.add(new InExpression());
        whereExpressionStrategies.add(new ContainsExpression());
        whereExpressionStrategies.add(new CountExpression());
        whereExpressionStrategies.add(new LikeExpression());

        whereClause = new WhereClauseNodeCypherTranslator(whereExpressionStrategies);
        match = new MatchCypherTranslatorStrategy(translatorStrategies, whereClause);
        return this;
    }


    //region Fields
    private Ontology.Accessor ont;
    private List<CypherElementTranslatorStrategy> translatorStrategies;
    private List<ExpressionStrategies> whereExpressionStrategies;

    public MatchCypherTranslatorStrategy match;
    private WhereClauseNodeCypherTranslator whereClause;

    //endregion

}
