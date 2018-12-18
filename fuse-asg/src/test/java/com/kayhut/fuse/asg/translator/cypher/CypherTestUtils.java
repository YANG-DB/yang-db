package com.kayhut.fuse.asg.translator.cypher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kayhut.fuse.asg.translator.cypher.strategies.CypherElementTranslatorStrategy;
import com.kayhut.fuse.asg.translator.cypher.strategies.MatchCypherTranslatorStrategy;
import com.kayhut.fuse.asg.translator.cypher.strategies.NodePatternCypherTranslatorStrategy;
import com.kayhut.fuse.asg.translator.cypher.strategies.StepPatternCypherTranslatorStrategy;
import com.kayhut.fuse.asg.translator.cypher.strategies.expressions.*;
import com.kayhut.fuse.model.ontology.Ontology;
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
                new NodePatternCypherTranslatorStrategy(),
                new StepPatternCypherTranslatorStrategy(
                        new NodePatternCypherTranslatorStrategy()
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
        whereExpressionStrategies.add(new InExpression());

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
