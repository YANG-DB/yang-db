package com.yangdb.fuse.dispatcher.query.rdf;

import com.google.common.collect.Sets;
import com.yangdb.fuse.dispatcher.query.graphql.GraphQL2OntologyTransformer;
import com.yangdb.fuse.model.ontology.EnumeratedType;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.ontology.Property;
import com.yangdb.fuse.model.ontology.Value;
import graphql.schema.GraphQLSchema;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.yangdb.fuse.model.ontology.Property.equal;

public class OWLOntologyTranslatorTest {
    public static Ontology ontology;

    @BeforeClass
    public static void setUp() throws Exception {
        URL workspace = Thread.currentThread().getContextClassLoader().getResource("rdf/workspace.owl");
        URL user = Thread.currentThread().getContextClassLoader().getResource("rdf/user.owl");
        OWL2OntologyTransformer transformer = new OWL2OntologyTransformer();
        //load owl ontologies - the order of the ontologies is important in regards with the owl dependencies
        assert user != null;
        assert workspace != null;

        ontology = transformer.transform(Arrays.asList(
                new String(Files.readAllBytes(new File(user.toURI()).toPath())),
                new String(Files.readAllBytes(new File(workspace.toURI()).toPath()))));
        Assert.assertNotNull(ontology);
    }

    @Test
    public void testEnumTranslation() {
        Assert.assertEquals(ontology.getEnumeratedTypes().size(), 1);
        Ontology.Accessor accessor = new Ontology.Accessor(ontology);

        Assert.assertEquals(accessor.enumeratedType$("http://yangdb.org/user#countries"),
                new EnumeratedType("http://yangdb.org/user#countries",
                        Arrays.asList(new Value(0, "http://yangdb.org/user#Eurasia"),
                                new Value(1, "http://yangdb.org/user#NorthAmerica"),
                                new Value(2, "http://yangdb.org/user#Antarctica"),
                                new Value(3, "http://yangdb.org/user#Africa"),
                                new Value(4, "http://yangdb.org/user#SouthAmerica"),
                                new Value(5, "http://yangdb.org/user#Australia"))));
    }

    @Test
    public void testPropertiesTranslation() {
        Assert.assertEquals(ontology.getProperties().size(), 18);
        Ontology.Accessor accessor = new Ontology.Accessor(ontology);
        List<String> expected = Arrays.asList(
                "http://yangdb.org/user#passwordHash", "http://yangdb.org/user#authorizations", "http://yangdb.org/user#currentLoginRemoteAddr",
                "http://yangdb.org/user#uiPreferences", "http://yangdb.org/user#username", "http://yangdb.org/user#createDate", "http://yangdb.org/user#privileges", "http://yangdb.org/user#currentLoginDate",
                "http://yangdb.org/user#previousLoginDate", "http://yangdb.org/user#emailAddress", "http://yangdb.org/user#passwordSalt", "http://yangdb.org/user#loginCount",
                "http://yangdb.org/user#passwordResetToken", "http://yangdb.org/user#previousLoginRemoteAddr", "http://yangdb.org/user#currentWorkspace", "http://yangdb.org/user#displayName",
                "http://yangdb.org/user#passwordResetTokenExpirationDate", "http://yangdb.org/user#status");

        Assert.assertEquals(ontology.getProperties().stream().map(Property::getpType).collect(Collectors.toList()), expected);
    }

    @Test
    public void testEntitiesTranslation() {
        Assert.assertEquals(ontology.getEntityTypes().size(), 5);
        Ontology.Accessor accessor = new Ontology.Accessor(ontology);

        Assert.assertEquals(accessor.entity$("http://yangdb.org/user#Person").geteType(), "http://yangdb.org/user#Person");
        Assert.assertEquals(accessor.entity$("http://www.w3.org/2002/07/owl#Thing").geteType(), "http://www.w3.org/2002/07/owl#Thing");
        Assert.assertEquals(accessor.entity$("http://yangdb.org/user#Corporation").geteType(), "http://yangdb.org/user#Corporation");
        Assert.assertEquals(accessor.entity$("http://xmlns.com/foaf/0.1/img").geteType(), "http://xmlns.com/foaf/0.1/img");

        Assert.assertEquals(accessor.entity$("http://yangdb.org/user#user").geteType(), "http://yangdb.org/user#user");
        Assert.assertEquals(accessor.entity$("http://yangdb.org/user#user").getProperties().size(), 18);
        Assert.assertEquals(accessor.entity$("http://yangdb.org/user#user").getMandatory().size(), 0);

    }

    @Test
    public void testRelationsTranslation() {
        Assert.assertEquals(ontology.getRelationshipTypes().size(), 1);
        Ontology.Accessor accessor = new Ontology.Accessor(ontology);

        Assert.assertEquals(accessor.relation$("http://yangdb.org/user#hasImage").getrType(), "http://yangdb.org/user#hasImage");
        Assert.assertEquals(accessor.relation$("http://yangdb.org/user#hasImage").getePairs().size(), 2);
        Assert.assertEquals(accessor.relation$("http://yangdb.org/user#hasImage").getePairs().get(1).geteTypeA(), "http://yangdb.org/user#Corporation");
        Assert.assertEquals(accessor.relation$("http://yangdb.org/user#hasImage").getePairs().get(0).geteTypeA(), "http://yangdb.org/user#Person");

    }

}
