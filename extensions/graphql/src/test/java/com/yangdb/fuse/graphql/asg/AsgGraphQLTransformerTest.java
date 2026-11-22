package com.yangdb.fuse.graphql.asg;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import com.yangdb.fuse.dispatcher.ontology.OntologyProvider;
import com.yangdb.fuse.graphql.translator.GraphQLSchemaUtils;
import com.yangdb.fuse.graphql.translator.GraphQLToOntologyTransformer;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.execution.plan.descriptors.AsgQueryDescriptor;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.query.QueryInfo;
import org.junit.Assert;
import org.junit.Test;

import static com.yangdb.fuse.graphql.translator.GraphQLDragonsQueryTest.provider;
import static com.yangdb.fuse.graphql.translator.GraphQLDragonsQueryTest.resource;
import static com.yangdb.fuse.model.transport.CreateQueryRequestMetadata.TYPE_GRAPHQL;

/** The graphQL language (as the service creates it, once) translates query after query independently. */
public class AsgGraphQLTransformerTest {
    @Test
    public void eachQueryIsTranslatedOnItsOwn() throws Exception {
        Ontology dragons = new ObjectMapper().readValue(resource("ontology/Dragons.json"), Ontology.class);
        AsgGraphQLTransformer transformer = Guice.createInjector(binder -> {
            binder.bind(OntologyProvider.class).toInstance(provider(dragons));
            binder.bind(GraphQLSchemaUtils.class).to(GraphQLToOntologyTransformer.class);
        }).getInstance(AsgGraphQLTransformer.class);

        AsgQuery first = transformer.transform(new QueryInfo<>("{ person { name Own { name color } } }", "q1", TYPE_GRAPHQL, "Dragons"));
        AsgQuery second = transformer.transform(new QueryInfo<>("{ horse { name } }", "q2", TYPE_GRAPHQL, "Dragons"));

        Assert.assertEquals("Dragons", second.getOnt());
        Assert.assertEquals("q2", second.getName());
        String print = AsgQueryDescriptor.print(second);
        Assert.assertTrue(print, print.contains("Horse"));
        Assert.assertFalse("the first query's elements leaked into the second: " + print,
                print.contains("Person") || print.contains("Own") || print.contains("Dragon"));
        Assert.assertTrue(first.getElements().size() > second.getElements().size());
    }
}
