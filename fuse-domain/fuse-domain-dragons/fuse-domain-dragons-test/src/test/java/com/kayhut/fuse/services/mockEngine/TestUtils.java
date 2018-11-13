package com.kayhut.fuse.services.mockEngine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.results.AssignmentsQueryResult;
import org.apache.commons.io.IOUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import java.io.IOException;
import java.util.function.Predicate;

/**
 * Created by lior.perry on 3/12/2017.
 */
public abstract class TestUtils {

    public static Ontology loadOntology(String name) throws IOException {
        String query = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream("ontology/" + name));
        return new ObjectMapper().readValue(query, Ontology.class);
    }

    public static Query loadQuery(String name) throws IOException {
        String query = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream("queries/" + name));
        return new ObjectMapper().readValue(query, Query.class);
    }

    public static AssignmentsQueryResult loadResult(String name) throws IOException {
        String query = IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream("results/" + name));
        return new ObjectMapper().readValue(query, AssignmentsQueryResult.class);
    }

    public static class ContentMatcher extends BaseMatcher {
        private Predicate predicate;

        public ContentMatcher(Predicate predicate) {
            this.predicate = predicate;
        }

        @Override
        public boolean matches(Object item) {
            return predicate.test(item);
        }

        @Override
        public void describeTo(Description description) {}
    }
}
