package com.yangdb.fuse.asg.translator;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;
import com.yangdb.fuse.asg.translator.cypher.AsgCypherModule;
import com.yangdb.fuse.asg.translator.cypher.AsgCypherTransformer;
import com.yangdb.fuse.asg.translator.sparql.AsgSparQLTransformer;
import com.yangdb.fuse.asg.translator.sparql.AsgSparqlModule;
import com.yangdb.fuse.dispatcher.ontology.OntologyProvider;
import com.yangdb.fuse.dispatcher.ontology.OntologyTransformerProvider;
import com.yangdb.fuse.dispatcher.query.JsonQueryTransformerFactory;
import com.yangdb.fuse.dispatcher.query.QueryTransformer;
import com.yangdb.fuse.extension.QueryLanguages;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.query.QueryInfo;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import com.yangdb.fuse.model.transport.CreateQueryRequestMetadata;
import org.jooby.Env;
import org.jooby.Jooby;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

/** The query-language registry: a MapBinder keyed by query type, filled by the language modules and by extensions. */
public class BasicJsonQueryTransformerFactoryTest {

    private static final QueryTransformer<QueryInfo<String>, AsgQuery> ECHO =
            query -> AsgQuery.Builder.start(query.getQuery(), query.getOntology()).build();

    private static JsonQueryTransformerFactory factory(Module... modules) {
        Module[] all = Arrays.copyOf(modules, modules.length + 1);
        all[modules.length] = binder -> binder.bind(JsonQueryTransformerFactory.class).to(BasicJsonQueryTransformerFactory.class);
        return Guice.createInjector(all).getInstance(JsonQueryTransformerFactory.class);
    }

    @Test
    public void aRegisteredLanguageResolvesToItsTransformer() {
        JsonQueryTransformerFactory factory = factory(binder -> QueryLanguages.register(binder, "echo").toInstance(ECHO));

        AsgQuery asg = factory.transform("echo").transform(new QueryInfo<>("hello", "q1", "echo", "Dragons"));

        Assert.assertSame(ECHO, factory.transform("echo"));
        Assert.assertEquals("hello", asg.getName());
        Assert.assertEquals(Collections.singleton("echo"), factory.languages());
    }

    @Test
    public void anUnknownLanguageFailsWithTheAvailableLanguages() {
        JsonQueryTransformerFactory factory = factory(binder -> QueryLanguages.register(binder, "echo").toInstance(ECHO));
        try {
            factory.transform("klingon");
            Assert.fail("expected an error for an unknown language");
        } catch (FuseError.FuseErrorException e) {
            Assert.assertEquals("No Query translator found", e.getError().getErrorCode());
            Assert.assertTrue(e.getError().getErrorDescription(), e.getError().getErrorDescription().contains("klingon"));
            Assert.assertTrue(e.getError().getErrorDescription(), e.getError().getErrorDescription().contains("[echo]"));
        }
    }

    @Test
    public void noLanguagesStillBuildsTheFactory() {
        // the factory's own module declares the map, so a config without any language module still starts
        JsonQueryTransformerFactory factory = factory(QueryLanguages::mapBinder);
        Assert.assertTrue(factory.languages().isEmpty());
    }

    @Test
    public void lookupFallsBackToACaseInsensitiveMatch() {
        JsonQueryTransformerFactory factory = factory(binder -> QueryLanguages.register(binder, "graphQL").toInstance(ECHO));
        Assert.assertSame(ECHO, factory.transform("graphQL"));
        Assert.assertSame(ECHO, factory.transform("graphql"));
    }

    @Test
    public void theBuiltInLanguageModulesRegisterUnderTheirQueryTypes() throws Throwable {
        Config config = ConfigFactory.empty()
                .withValue("application.profile", ConfigValueFactory.fromAnyRef("test"))
                .withValue("modules.test", ConfigValueFactory.fromIterable(Arrays.asList(
                        AsgCypherModule.class.getName(), AsgSparqlModule.class.getName())));
        AsgCypherTransformer cypher = new AsgCypherTransformer(null);
        AsgSparQLTransformer sparql = new AsgSparQLTransformer(null);

        Injector injector = Guice.createInjector(
                jooby(new AsgCypherModule(), config), jooby(new AsgSparqlModule(), config),
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        // the real transformers need a whole ontology stack: these instances stand in for them
                        bind(AsgCypherTransformer.class).toInstance(cypher);
                        bind(AsgSparQLTransformer.class).toInstance(sparql);
                        bind(OntologyProvider.class).toInstance(stub(OntologyProvider.class));
                        bind(OntologyTransformerProvider.class).toInstance(stub(OntologyTransformerProvider.class));
                        bind(JsonQueryTransformerFactory.class).to(BasicJsonQueryTransformerFactory.class);
                    }
                });

        Map<String, QueryTransformer<QueryInfo<String>, AsgQuery>> languages = injector.getInstance(
                com.google.inject.Key.get(new TypeLiteral<Map<String, QueryTransformer<QueryInfo<String>, AsgQuery>>>() {}));
        // graphQL is registered by the GraphQL extension (extensions/graphql), not by core
        Assert.assertEquals(new HashSet<>(Arrays.asList(CreateQueryRequestMetadata.TYPE_CYPHERQL,
                CreateQueryRequestMetadata.TYPE_SPARQL)), languages.keySet());

        JsonQueryTransformerFactory factory = injector.getInstance(JsonQueryTransformerFactory.class);
        Assert.assertSame(cypher, factory.transform(CreateQueryRequestMetadata.TYPE_CYPHERQL));
        Assert.assertSame(sparql, factory.transform(CreateQueryRequestMetadata.TYPE_SPARQL));
    }

    private static Module jooby(Jooby.Module module, Config config) {
        return binder -> {
            try {
                module.configure(Env.DEFAULT.build(config), config, binder);
            } catch (Throwable e) {
                throw new IllegalStateException(e);
            }
        };
    }

    @SuppressWarnings("unchecked")
    private static <T> T stub(Class<T> type) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, (proxy, method, args) -> null);
    }
}
