package com.yangdb.fuse.pgql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yangdb.fuse.model.execution.plan.descriptors.OntologyDescriptor;
import com.yangdb.fuse.model.ontology.*;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

public class PgqlDDLOntologyParserTest extends TestCase {
    public static final ObjectMapper mapper = new ObjectMapper();

    public static final String DDL_QUERY_SELF = "CREATE PROPERTY GRAPH hr_simplified\n" +
            "  VERTEX TABLES (\n" +
            "    employees LABEL employee\n" +
            "      PROPERTIES ARE ALL COLUMNS EXCEPT ( job_id, manager_id, department_id ),\n" +
            "    departments LABEL department\n" +
            "      PROPERTIES ( department_id, department_name )\n" +
            "  )\n" +
            "  EDGE TABLES (\n" +
            "    employees AS works_for\n" +
            "      SOURCE KEY ( employee_id ) REFERENCES employees\n" +
            "      DESTINATION employees\n" +
            "      NO PROPERTIES\n" +
            "  )";

    public static final String DDL_QUERY_1 = "CREATE PROPERTY GRAPH financial_transactions\n" +
            "  VERTEX TABLES (\n" +
            "    Individuals LABEL Individual PROPERTIES ( name ),\n" +
            "    Companies LABEL Company PROPERTIES ( name ),\n" +
            "    Accounts LABEL Account PROPERTIES ( number )\n" +
            "  )\n" +
            "  EDGE TABLES (\n" +
            "    Transactions\n" +
            "      SOURCE KEY ( from_account ) REFERENCES Accounts\n" +
            "      DESTINATION KEY ( to_account ) REFERENCES Accounts\n" +
            "      LABEL transaction PROPERTIES ( amount ),\n" +
            "    Ownership AS Owner\n" +
            "      SOURCE KEY ( number ) REFERENCES Accounts\n" +
            "      DESTINATION  KEY ( name ) REFERENCES Individuals\n" +
            "      LABEL owner NO PROPERTIES,\n" +
            "    Accounts AS Owner\n" +
            "      SOURCE KEY ( number ) REFERENCES Accounts\n" +
            "      DESTINATION  KEY ( name ) REFERENCES Companies\n" +
            "      LABEL owner NO PROPERTIES,\n" +
            "    Organization AS worksFor\n" +
            "      SOURCE KEY ( id ) REFERENCES Individuals\n" +
            "      DESTINATION  KEY ( name ) REFERENCES Companies\n" +
            "      NO PROPERTIES\n" +
            "  )";

    public static final String DDL_QUERY_2 = "CREATE PROPERTY GRAPH financial_transactions\n" +
            "  VERTEX TABLES (\n" +
            "    Individuals LABEL Individual PROPERTIES ( name ),\n" +
            "    Companies LABEL Company PROPERTIES ( name ),\n" +
            "    Accounts LABEL Account PROPERTIES ( number )\n" +
            "  )\n" +
            "  EDGE TABLES (\n" +
            "    Transactions\n" +
            "      SOURCE KEY ( from_account ) REFERENCES Accounts\n" +
            "      DESTINATION KEY ( to_account ) REFERENCES Accounts\n" +
            "      LABEL transaction PROPERTIES ( amount ),\n" +
            "    Accounts AS Owner\n" +
            "      SOURCE KEY ( number ) REFERENCES Accounts\n" +
            "      DESTINATION Individuals\n" +
            "      LABEL owner NO PROPERTIES,\n" +
            "    Accounts AS Owner\n" +
            "      SOURCE KEY ( number ) REFERENCES Accounts\n" +
            "      DESTINATION Companies\n" +
            "      LABEL owner NO PROPERTIES,\n" +
            "    Individuals AS worksFor\n" +
            "      SOURCE KEY ( id ) REFERENCES Individuals\n" +
            "      DESTINATION Companies\n" +
            "      NO PROPERTIES\n" +
            "  )";

    Ontology ontology;
    Ontology.Accessor accessor;

    @Override
    public void setUp() throws Exception {
        ontology = new PgqlOntologyParser().transform("financial_transactions", DDL_QUERY_2);
        accessor = new Ontology.Accessor(ontology);

    }

    @Test
    public void testTransformVertices() {
        Assert.assertEquals(accessor.$entity$("INDIVIDUAL"), EntityType.Builder.get()
                .withEType("INDIVIDUAL").withName("INDIVIDUAL").withProperty("INDIVIDUAL_NAME").build());
        Assert.assertEquals(accessor.$entity$("COMPANY"), EntityType.Builder.get()
                .withEType("COMPANY").withName("COMPANY").withProperty("COMPANY_NAME").build());
        Assert.assertEquals(accessor.$entity$("ACCOUNT"), EntityType.Builder.get()
                .withEType("ACCOUNT").withName("ACCOUNT").withProperty("ACCOUNT_NUMBER").build());
    }


    @Test
    public void testTransformEdges() {
        Assert.assertEquals(accessor.$relation$("TRANSACTIONS"),
                RelationshipType.Builder.get()
                        .withDBrName("TRANSACTIONS")
                        .withName("TRANSACTION")
                        .withRType("TRANSACTIONS")
                        .withProperty("TRANSACTION_AMOUNT")
                        .withEPair(EPair.EPairBuilder.anEPair()
                                .with("ACCOUNT", "ACCOUNT")
                                .withETypeAIdField("FROM_ACCOUNT")
                                .withETypeBIdField("TO_ACCOUNT")
                                .build())
                        .build());

        Assert.assertEquals(accessor.$relation$("WORKSFOR"),
                RelationshipType.Builder.get()
                        .withDBrName("INDIVIDUAL")
                        .withName("WORKSFOR")
                        .withRType("WORKSFOR")
                        .withEPair(EPair.EPairBuilder.anEPair()
                                .with("INDIVIDUAL", "COMPANY")
                                .withETypeAIdField("ID")
                                .withETypeBIdField(null)
                                .build())
                        .build());
        Assert.assertEquals(accessor.$relation$("OWNER"),
                RelationshipType.Builder.get()
                        .withDBrName("INDIVIDUAL")
                        .withName("OWNER")
                        .withRType("OWNER")
                        .withEPair(EPair.EPairBuilder.anEPair()
                                .with("ACCOUNT","COMPANY" )
                                .withETypeAIdField("NUMBER")
                                .withETypeBIdField(null) // TODO - THIS SHOULD BE explicitly declared in the generate graph DDL  & THE VERIFIER SHOULD REJECT SUCH NON POPULATED MAPPING
                                .build())
                        .withEPair(EPair.EPairBuilder.anEPair()
                                .with("ACCOUNT","INDIVIDUAL")
                                .withETypeAIdField("NUMBER")
                                .withETypeBIdField(null) // TODO - THIS SHOULD BE explicitly declared in the generate graph DDL  & THE VERIFIER SHOULD REJECT SUCH NON POPULATED MAPPING
                                .build())
                        .build());
    }


    @Test
    public void testTransformProperties() {
        Assert.assertEquals(accessor.property$("INDIVIDUAL_NAME"), new Property("INDIVIDUAL_NAME", "INDIVIDUAL_NAME", Ontology.OntologyPrimitiveType.STRING.name()));
        Assert.assertEquals(accessor.property$("ACCOUNT_NUMBER"), new Property("ACCOUNT_NUMBER", "ACCOUNT_NUMBER", Ontology.OntologyPrimitiveType.STRING.name()));
        Assert.assertEquals(accessor.property$("COMPANY_NAME"), new Property("COMPANY_NAME", "COMPANY_NAME", Ontology.OntologyPrimitiveType.STRING.name()));
        Assert.assertEquals(accessor.property$("TRANSACTION_AMOUNT"), new Property("TRANSACTION_AMOUNT", "TRANSACTION_AMOUNT", Ontology.OntologyPrimitiveType.STRING.name()));
    }

    /**
     * view using https://dreampuf.github.io/GraphvizOnline/
     */
    public void testTransformsToSvgGraphix() throws JsonProcessingException {
        Assert.assertEquals(mapper.writeValueAsString(ontology), "{\"ont\":\"financial_transactions\",\"directives\":[],\"entityTypes\":[{\"idField\":[\"id\"],\"eType\":\"INDIVIDUAL\",\"name\":\"INDIVIDUAL\",\"properties\":[\"INDIVIDUAL_NAME\"],\"DBrName\":\"INDIVIDUALS\"},{\"idField\":[\"id\"],\"eType\":\"COMPANY\",\"name\":\"COMPANY\",\"properties\":[\"COMPANY_NAME\"],\"DBrName\":\"COMPANIES\"},{\"idField\":[\"id\"],\"eType\":\"ACCOUNT\",\"name\":\"ACCOUNT\",\"properties\":[\"ACCOUNT_NUMBER\"],\"DBrName\":\"ACCOUNTS\"}],\"relationshipTypes\":[{\"idField\":[\"id\"],\"rType\":\"WORKSFOR\",\"name\":\"WORKSFOR\",\"directional\":false,\"ePairs\":[{\"name\":\"INDIVIDUAL->COMPANY\",\"eTypeA\":\"INDIVIDUAL\",\"sideAIdField\":\"ID\",\"eTypeB\":\"COMPANY\"}],\"DBrName\":\"INDIVIDUALS\"},{\"idField\":[\"id\"],\"rType\":\"OWNER\",\"name\":\"OWNER\",\"directional\":false,\"ePairs\":[{\"name\":\"ACCOUNT->COMPANY\",\"eTypeA\":\"ACCOUNT\",\"sideAIdField\":\"NUMBER\",\"eTypeB\":\"COMPANY\"},{\"name\":\"ACCOUNT->INDIVIDUAL\",\"eTypeA\":\"ACCOUNT\",\"sideAIdField\":\"NUMBER\",\"eTypeB\":\"INDIVIDUAL\"}],\"DBrName\":\"ACCOUNTS\"},{\"idField\":[\"id\"],\"rType\":\"TRANSACTIONS\",\"name\":\"TRANSACTION\",\"directional\":false,\"ePairs\":[{\"name\":\"ACCOUNT->ACCOUNT\",\"eTypeA\":\"ACCOUNT\",\"sideAIdField\":\"FROM_ACCOUNT\",\"eTypeB\":\"ACCOUNT\",\"sideBIdField\":\"TO_ACCOUNT\"}],\"properties\":[\"TRANSACTION_AMOUNT\"],\"DBrName\":\"TRANSACTIONS\"}],\"properties\":[{\"pType\":\"INDIVIDUAL_NAME\",\"name\":\"INDIVIDUAL_NAME\",\"type\":\"STRING\"},{\"pType\":\"COMPANY_NAME\",\"name\":\"COMPANY_NAME\",\"type\":\"STRING\"},{\"pType\":\"ACCOUNT_NUMBER\",\"name\":\"ACCOUNT_NUMBER\",\"type\":\"STRING\"},{\"pType\":\"TRANSACTION_AMOUNT\",\"name\":\"TRANSACTION_AMOUNT\",\"type\":\"STRING\"}],\"enumeratedTypes\":[],\"compositeTypes\":[]}");

        String dotGraph = OntologyDescriptor.printGraph(ontology);
        Assert.assertEquals(dotGraph, "digraph G { \n" +
                " \t node [shape=Mrecord]; \n" +
                " \t node [style=filled]; \n" +
                " \n" +
                "  \tINDIVIDUAL [ shape=octagon, label=\"INDIVIDUAL\", fillcolor=lightblue]  \n" +
                " subgraph cluster_Props_INDIVIDUAL { \n" +
                " \t color=green; \n" +
                " \t node [fillcolor=khaki3, shape=component]; \n" +
                " \t INDIVIDUAL_INDIVIDUAL_NAME[fillcolor=white, label=\"INDIVIDUAL_NAME\" ]\n" +
                " \t INDIVIDUAL->INDIVIDUAL_INDIVIDUAL_NAME\n" +
                " \n" +
                " } \n" +
                " \n" +
                "  \n" +
                "  \tCOMPANY [ shape=octagon, label=\"COMPANY\", fillcolor=lightblue]  \n" +
                " subgraph cluster_Props_COMPANY { \n" +
                " \t color=green; \n" +
                " \t node [fillcolor=khaki3, shape=component]; \n" +
                " \t COMPANY_COMPANY_NAME[fillcolor=white, label=\"COMPANY_NAME\" ]\n" +
                " \t COMPANY->COMPANY_COMPANY_NAME\n" +
                " \n" +
                " } \n" +
                " \n" +
                "  \n" +
                "  \tACCOUNT [ shape=octagon, label=\"ACCOUNT\", fillcolor=lightblue]  \n" +
                " subgraph cluster_Props_ACCOUNT { \n" +
                " \t color=green; \n" +
                " \t node [fillcolor=khaki3, shape=component]; \n" +
                " \t ACCOUNT_ACCOUNT_NUMBER[fillcolor=white, label=\"ACCOUNT_NUMBER\" ]\n" +
                " \t ACCOUNT->ACCOUNT_ACCOUNT_NUMBER\n" +
                " \n" +
                " } \n" +
                " \n" +
                "  \tWORKSFOR [ shape=rarrow, label=\"WORKSFOR\", fillcolor=darkkhaki]  \n" +
                " subgraph cluster_Props_WORKSFOR { \n" +
                " \t color=green; \n" +
                " \t node [fillcolor=khaki3, shape=component]; \n" +
                " \n" +
                " } \n" +
                " \t INDIVIDUAL->WORKSFOR->COMPANY\n" +
                " \n" +
                "  \tOWNER [ shape=rarrow, label=\"OWNER\", fillcolor=darkkhaki]  \n" +
                " subgraph cluster_Props_OWNER { \n" +
                " \t color=green; \n" +
                " \t node [fillcolor=khaki3, shape=component]; \n" +
                " \n" +
                " } \n" +
                " \t ACCOUNT->OWNER->COMPANY\n" +
                " \t ACCOUNT->OWNER->INDIVIDUAL\n" +
                " \n" +
                "  \tTRANSACTIONS [ shape=rarrow, label=\"TRANSACTIONS\", fillcolor=darkkhaki]  \n" +
                " subgraph cluster_Props_TRANSACTIONS { \n" +
                " \t color=green; \n" +
                " \t node [fillcolor=khaki3, shape=component]; \n" +
                " \t TRANSACTIONS_TRANSACTION_AMOUNT[fillcolor=white, label=\"TRANSACTION_AMOUNT\" ]\n" +
                " \t TRANSACTIONS->TRANSACTIONS_TRANSACTION_AMOUNT\n" +
                " \n" +
                " } \n" +
                " \t ACCOUNT->TRANSACTIONS->ACCOUNT\n" +
                " \n" +
                "  \n" +
                "\t }");
    }
}