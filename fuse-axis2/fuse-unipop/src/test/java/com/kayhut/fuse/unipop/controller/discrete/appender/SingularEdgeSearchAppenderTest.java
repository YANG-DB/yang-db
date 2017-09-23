package com.kayhut.fuse.unipop.controller.discrete.appender;

import com.kayhut.fuse.unipop.controller.common.context.VertexControllerContext;
import com.kayhut.fuse.unipop.controller.search.QueryBuilder;
import com.kayhut.fuse.unipop.controller.search.SearchBuilder;
import com.kayhut.fuse.unipop.promise.TraversalConstraint;
import com.kayhut.fuse.unipop.schemaProviders.*;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import com.kayhut.fuse.unipop.structure.ElementType;
import com.kayhut.fuse.unipop.structure.discrete.DiscreteVertex;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.T;
import org.junit.Assert;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

/**
 * Created by roman.margolis on 14/09/2017.
 */
public class SingularEdgeSearchAppenderTest {
    //region Tests
    @Test
    public void testOut() {
        SingularEdgeSearchAppender appender = new SingularEdgeSearchAppender();
        SearchBuilder searchBuilder = new SearchBuilder();
        appender.append(searchBuilder, new VertexControllerContext.Default(
                null,
                ElementType.edge,
                getSchemaProvider(),
                Optional.empty(),
                Collections.emptyList(),
                0,
                Direction.OUT,
                Arrays.asList(
                        new DiscreteVertex("1", "vertexType1", null, null),
                        new DiscreteVertex("2", "vertexType1", null, null)
                )));

        QueryBuilder expectedQueryBuilder = new QueryBuilder();
        expectedQueryBuilder.query().filtered().filter().bool().must().terms("vertexType1Id", Arrays.asList("1", "2"));
        JSONAssert.assertEquals(expectedQueryBuilder.getQuery().toString(), searchBuilder.getQueryBuilder().getQuery().toString(), false);
    }

    @Test
    public void testOutEdgeType1() {
        SingularEdgeSearchAppender appender = new SingularEdgeSearchAppender();
        SearchBuilder searchBuilder = new SearchBuilder();
        appender.append(searchBuilder, new VertexControllerContext.Default(
                null,
                ElementType.edge,
                getSchemaProvider(),
                Optional.of(new TraversalConstraint(__.has(T.label, "edgeType1"))),
                Collections.emptyList(),
                0,
                Direction.OUT,
                Arrays.asList(
                        new DiscreteVertex("1", "vertexType1", null, null),
                        new DiscreteVertex("2", "vertexType1", null, null)
                )));

        QueryBuilder expectedQueryBuilder = new QueryBuilder();
        expectedQueryBuilder.query().filtered().filter().bool().must().terms("vertexType1Id", Arrays.asList("1", "2"));
        JSONAssert.assertEquals(expectedQueryBuilder.getQuery().toString(), searchBuilder.getQueryBuilder().getQuery().toString(), false);
    }

    @Test
    public void testIn() {
        SingularEdgeSearchAppender appender = new SingularEdgeSearchAppender();
        SearchBuilder searchBuilder = new SearchBuilder();
        appender.append(searchBuilder, new VertexControllerContext.Default(
                null,
                ElementType.edge,
                getSchemaProvider(),
                Optional.empty(),
                Collections.emptyList(),
                0,
                Direction.IN,
                Arrays.asList(
                        new DiscreteVertex("3", "vertexType2", null, null),
                        new DiscreteVertex("4", "vertexType2", null, null)
                )));

        QueryBuilder expectedQueryBuilder = new QueryBuilder();
        expectedQueryBuilder.query().filtered().filter().bool().must().terms("vertexType2Id", Arrays.asList("3", "4"));
        JSONAssert.assertEquals(expectedQueryBuilder.getQuery().toString(), searchBuilder.getQueryBuilder().getQuery().toString(), false);
    }

    @Test
    public void testInEdgeType1() {
        SingularEdgeSearchAppender appender = new SingularEdgeSearchAppender();
        SearchBuilder searchBuilder = new SearchBuilder();
        appender.append(searchBuilder, new VertexControllerContext.Default(
                null,
                ElementType.edge,
                getSchemaProvider(),
                Optional.of(new TraversalConstraint(__.has(T.label, "edgeType1"))),
                Collections.emptyList(),
                0,
                Direction.IN,
                Arrays.asList(
                        new DiscreteVertex("3", "vertexType2", null, null),
                        new DiscreteVertex("4", "vertexType2", null, null)
                )));

        QueryBuilder expectedQueryBuilder = new QueryBuilder();
        expectedQueryBuilder.query().filtered().filter().bool().must().terms("vertexType2Id", Arrays.asList("3", "4"));
        JSONAssert.assertEquals(expectedQueryBuilder.getQuery().toString(), searchBuilder.getQueryBuilder().getQuery().toString(), false);
    }

    @Test
    public void testOutEdgeTypeNonExistent() {
        SingularEdgeSearchAppender appender = new SingularEdgeSearchAppender();
        SearchBuilder searchBuilder = new SearchBuilder();
        appender.append(searchBuilder, new VertexControllerContext.Default(
                null,
                ElementType.edge,
                getSchemaProvider(),
                Optional.of(new TraversalConstraint(__.has(T.label, "edgeTypeNonExistent"))),
                Collections.emptyList(),
                0,
                Direction.OUT,
                Arrays.asList(
                        new DiscreteVertex("1", "vertexType1", null, null),
                        new DiscreteVertex("2", "vertexType1", null, null)
                )));

        Assert.assertEquals(null, searchBuilder.getQueryBuilder().getQuery());
    }

    @Test
    public void testOutVertexType2() {
        SingularEdgeSearchAppender appender = new SingularEdgeSearchAppender();
        SearchBuilder searchBuilder = new SearchBuilder();
        appender.append(searchBuilder, new VertexControllerContext.Default(
                null,
                ElementType.edge,
                getSchemaProvider(),
                Optional.empty(),
                Collections.emptyList(),
                0,
                Direction.OUT,
                Arrays.asList(
                        new DiscreteVertex("3", "vertexType2", null, null),
                        new DiscreteVertex("4", "vertexType2", null, null)
                )));

        Assert.assertEquals(null, searchBuilder.getQueryBuilder().getQuery());
    }

    @Test
    public void testInVertexType1() {
        SingularEdgeSearchAppender appender = new SingularEdgeSearchAppender();
        SearchBuilder searchBuilder = new SearchBuilder();
        appender.append(searchBuilder, new VertexControllerContext.Default(
                null,
                ElementType.edge,
                getSchemaProvider(),
                Optional.empty(),
                Collections.emptyList(),
                0,
                Direction.IN,
                Arrays.asList(
                        new DiscreteVertex("1", "vertexType1", null, null),
                        new DiscreteVertex("2", "vertexType1", null, null)
                )));

        Assert.assertEquals(null, searchBuilder.getQueryBuilder().getQuery());
    }

    //endregion

    //region Private Methods
    private GraphElementSchemaProvider getSchemaProvider() {
        return new GraphElementSchemaProvider() {
            @Override
            public Optional<GraphVertexSchema> getVertexSchema(String label) {
                return null;
            }

            @Override
            public Optional<GraphEdgeSchema> getEdgeSchema(String label) {
                switch (label) {
                    case "edgeType1": return Optional.of(new GraphEdgeSchema() {
                        @Override
                        public Optional<End> getSource() {
                            return Optional.of(
                                    new End.Impl("vertexType1Id",
                                            Optional.of("vertexType1"),
                                            Collections.emptyList()));
                        }

                        @Override
                        public Optional<End> getDestination() {
                            return Optional.of(
                                    new End.Impl("vertexType2Id",
                                            Optional.of("vertexType2"),
                                            Collections.emptyList()));
                        }

                        @Override
                        public Optional<Direction> getDirection() {
                            return Optional.empty();
                        }

                        @Override
                        public String getType() {
                            return "edgeType1";
                        }

                        @Override
                        public Optional<GraphElementRouting> getRouting() {
                            return null;
                        }

                        @Override
                        public IndexPartitions getIndexPartitions() {
                            return null;
                        }

                        @Override
                        public Iterable<GraphElementPropertySchema> getProperties() {
                            return null;
                        }

                        @Override
                        public Optional<GraphElementPropertySchema> getProperty(String name) {
                            return null;
                        }
                    });
                }

                return Optional.empty();
            }

            @Override
            public Iterable<GraphEdgeSchema> getEdgeSchemas(String label) {
                Optional<GraphEdgeSchema> graphEdgeSchema = getEdgeSchema(label);
                return graphEdgeSchema.map(Collections::singletonList).orElseGet(Collections::emptyList);
            }

            @Override
            public Optional<GraphElementPropertySchema> getPropertySchema(String name) {
                return null;
            }

            @Override
            public Iterable<String> getVertexLabels() {
                return Arrays.asList("vertexType1", "vertexType2");
            }

            @Override
            public Iterable<String> getEdgeLabels() {
                return Arrays.asList("edgeType1");
            }
        };
    }
    //endregion
}
