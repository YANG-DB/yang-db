package com.yangdb.fuse.pgql;

import com.yangdb.fuse.model.ontology.Ontology;
import junit.framework.TestCase;

public class PgqlDMLOntologyParserTest extends TestCase {
    public static final String INSERT_VERTEX = "INSERT VERTEX x LABELS ( Male ) PROPERTIES ( x.age = 22 )";

    public static final String INSERT_VERTEX_MATCH = "INSERT VERTEX x LABELS ( Male ) PROPERTIES ( x.age = y.age )\n" +
            "  FROM MATCH (y:Male)";

    public static final String INSERT_EDGE = "INSERT EDGE e BETWEEN x AND y LABELS ( knows )\n" +
            "  FROM MATCH (x:Person)\n" +
            "     , MATCH (y:Person)\n" +
            " WHERE id(x) = 1 AND id(y) = 2";

    public static final String INSERT_EDGE_PROPERTIES =
            "INSERT EDGE e BETWEEN x AND y LABELS ( knows ) PROPERTIES ( e.since = DATE '2017-09-21' )\n" +
                    "  FROM MATCH (x:Person)\n" +
                    "     , MATCH (y:Person)\n" +
                    " WHERE id(x) = 1 AND id(y) = 2";


    public static final String MULTI_VERTEX_INSERT = "INSERT\n" +
            "  VERTEX v LABELS ( Male ) PROPERTIES ( v.age = 23, v.name = 'John' ),\n" +
            "  VERTEX u LABELS ( Female ) PROPERTIES ( u.age = 24, u.name = 'Jane' )";

    public static final String MULTI_VERTEX_EDGE_INSERT_ = "INSERT VERTEX x LABELS ( Person ) PROPERTIES ( x.name = 'John' )\n" +
            "     , EDGE e BETWEEN x AND y LABELS ( knows ) PROPERTIES ( e.since = DATE '2017-09-21' )\n" +
            "  FROM MATCH (y)\n" +
            " WHERE y.name = 'Jane'";


    public static final String DDL_QUERY = "CREATE PROPERTY GRAPH community\n" +
            "  VERTEX TABLES (\n" +
            "    Persons LABEL Person PROPERTIES ( name,age,gender )\n" +
            "  )\n" +
            "  EDGE TABLES (\n" +
            "    know\n" +
            "      SOURCE KEY ( source_id ) REFERENCES knows\n" +
            "      DESTINATION KEY ( target_id ) REFERENCES knows\n" +
            "      LABEL knows  PROPERTIES ( since )\n" +
            "  )";

    Ontology ontology;
    Ontology.Accessor accessor;

    @Override
    public void setUp() throws Exception {
        ontology = new PgqlOntologyParser().transform("community", DDL_QUERY);
        accessor = new Ontology.Accessor(ontology);
    }


}