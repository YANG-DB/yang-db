package com.kayhut.fuse.neo4j.executor;

import com.google.inject.Inject;
import com.kayhut.fuse.dispatcher.cursor.Cursor;
import com.kayhut.fuse.dispatcher.cursor.CursorFactory;
import com.kayhut.fuse.dispatcher.resource.QueryResource;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.results.Assignment;
import com.kayhut.fuse.model.results.Entity;
import com.kayhut.fuse.model.results.QueryResult;
import com.kayhut.fuse.model.results.Relationship;
import com.kayhut.fuse.model.transport.CreateCursorRequest;
import com.kayhut.fuse.neo4j.GraphProvider;
import org.neo4j.driver.internal.InternalNode;
import org.neo4j.driver.internal.InternalRelationship;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

import java.util.*;

/**
 * Created by Roman on 02/04/2017.
 */
public class Neo4jCursorFactory implements CursorFactory {
    //region Constructors
    @Inject
    public Neo4jCursorFactory(GraphProvider graphProvider) {
        this.graphProvider = graphProvider;
    }
    //endregion

    //region CursorFactory Implementation
    @Override
    public Cursor createCursor(CursorFactory.Context context) {
        Neo4jCursorContext neo4jCursorContext = (Neo4jCursorContext)context;
        return new Neo4jCursor(neo4jCursorContext, this.graphProvider);
    }
    //endregion

    //region Fields
    private GraphProvider graphProvider;
    //endregion

    /**
     * Created by Roman on 02/04/2017.
     */
    public static class Neo4jCursorContext implements Context {
        //region Constructors
        public Neo4jCursorContext(QueryResource queryResource, String cypher, Ontology ont) {
            this.queryResource = queryResource;
            this.cypher = cypher;
            this.ont = ont;
        }
        //endregion

        //region Properties
        @Override
        public QueryResource getQueryResource() {
            return this.queryResource;
        }

        @Override
        public CreateCursorRequest.CursorType getCursorType() {
            return CreateCursorRequest.CursorType.paths;
        }

        public String getCypher() {
            return cypher;
        }

        public Ontology getOnt() {
            return ont;
        }

        //endregion

        //region Fields
        private QueryResource queryResource;
        private String cypher;
        private Ontology ont;
        //endregion
    }

    /**
     * Created by User on 06/03/2017.
     */
    public static class Neo4jCursor implements Cursor {
        //region Constructors
        public Neo4jCursor(Neo4jCursorContext context, GraphProvider graphProvider) {
            this.context = context;
            this.graphProvider = graphProvider;
        }
        //endregion

        //region Cursor Implementation
        @Override
        public QueryResult getNextResults(int numResults) {

            if (session != null && !session.isOpen()) {
                return QueryResult.Builder.instance().withAssignments(Collections.emptyList()).build();
            }

            if (session == null) {
                session = graphProvider.getSession();
                statementResult = session.run(context.getCypher());
            }

            int resCount = 0;
            List<Assignment> assignments = new ArrayList<>();
            ArrayList<Entity> ents = new ArrayList<>();
            ArrayList<Relationship> rels = new ArrayList<>();

            while (statementResult.hasNext() && resCount < numResults) {

                Record record = statementResult.next();
                for (Map.Entry<String, Object> entry :
                        record.asMap().entrySet()) {
                    if (entry.getValue() instanceof InternalNode) {
                        ents.add(NeoGraphUtils.entityFromNodeValue(entry.getKey(), (InternalNode) entry.getValue(), context.getOnt()));
                    } else if (entry.getValue() instanceof InternalRelationship) {
                        rels.add(NeoGraphUtils.relFromRelValue(entry.getKey(), (InternalRelationship) entry.getValue(), context.getOnt()));
                    } else {
                        //TODO: ?
                    }
                }

                Assignment assignment = Assignment.Builder.instance()
                        .withEntities(ents)
                        .withRelationships(rels)
                        .build();

                assignments.add(assignment);

                resCount++;

            }

            if (!statementResult.hasNext()) {
                session.close();
            }

            return QueryResult.Builder.instance().withAssignments(assignments).build();

        }
        //endregion

        //region Properties
        public Neo4jCursorContext getContext() {
            return context;
        }
        //endregion

        //region Fields
        private Neo4jCursorContext context;
        private GraphProvider graphProvider;
        private StatementResult statementResult;
        private Session session;
        //endregion+
    }
}
