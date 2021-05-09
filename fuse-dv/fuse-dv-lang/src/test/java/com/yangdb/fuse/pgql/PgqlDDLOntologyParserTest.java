package com.yangdb.fuse.pgql;

import com.yangdb.fuse.model.execution.plan.descriptors.OntologyDescriptor;
import com.yangdb.fuse.model.ontology.*;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

public class PgqlDDLOntologyParserTest extends TestCase {
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

    public static final String DDL_QUERY = "CREATE PROPERTY GRAPH financial_transactions\n" +
            "  VERTEX TABLES (\n" +
            "    Persons LABEL Person PROPERTIES ( name ),\n" +
            "    Companies LABEL Company PROPERTIES ( name ),\n" +
            "    Accounts LABEL Account PROPERTIES ( number )\n" +
            "  )\n" +
            "  EDGE TABLES (\n" +
            "    Transactions\n" +
            "      SOURCE KEY ( from_account ) REFERENCES Accounts\n" +
            "      DESTINATION KEY ( to_account ) REFERENCES Accounts\n" +
            "      LABEL transaction PROPERTIES ( amount ),\n" +
            "    Accounts AS PersonOwner\n" +
            "      SOURCE KEY ( number ) REFERENCES Accounts\n" +
            "      DESTINATION Persons\n" +
            "      LABEL owner NO PROPERTIES,\n" +
            "    Accounts AS CompanyOwner\n" +
            "      SOURCE KEY ( number ) REFERENCES Accounts\n" +
            "      DESTINATION Companies\n" +
            "      LABEL owner NO PROPERTIES,\n" +
            "    Persons AS worksFor\n" +
            "      SOURCE KEY ( id ) REFERENCES Persons\n" +
            "      DESTINATION Companies\n" +
            "      NO PROPERTIES\n" +
            "  )";

    Ontology ontology;
    Ontology.Accessor accessor;

    @Override
    public void setUp() throws Exception {
        ontology = new PgqlOntologyParser().transform("financial_transactions", DDL_QUERY);
        accessor = new Ontology.Accessor(ontology);

    }

    @Test
    public void testTransformVertices() {

        Assert.assertEquals(accessor.$entity$("PERSON"), EntityType.Builder.get()
                .withEType("PERSON").withName("PERSON").withProperty("PERSON_NAME").build());
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
                        .withDBrName("PERSONS")
                        .withName("WORKSFOR")
                        .withRType("WORKSFOR")
                        .withEPair(EPair.EPairBuilder.anEPair()
                                .with("PERSON", "COMPANY")
                                .withETypeAIdField("ID")
                                .withETypeBIdField(null)
                                .build())
                        .build());
    }


    @Test
    public void testTransformProperties() {

        Assert.assertEquals(accessor.property$("PERSON_NAME"),new Property("PERSON_NAME","PERSON_NAME",Ontology.OntologyPrimitiveType.STRING.name()));
        Assert.assertEquals(accessor.property$("ACCOUNT_NUMBER"),new Property("ACCOUNT_NUMBER","ACCOUNT_NUMBER",Ontology.OntologyPrimitiveType.STRING.name()));
        Assert.assertEquals(accessor.property$("COMPANY_NAME"),new Property("COMPANY_NAME","COMPANY_NAME",Ontology.OntologyPrimitiveType.STRING.name()));
        Assert.assertEquals(accessor.property$("TRANSACTION_AMOUNT"),new Property("TRANSACTION_AMOUNT","TRANSACTION_AMOUNT",Ontology.OntologyPrimitiveType.STRING.name()));
    }

    /**
     * view using https://dreampuf.github.io/GraphvizOnline/
     */
    public void testTransformsToSvgGraphix() {
        String dotGraph = OntologyDescriptor.printGraph(ontology);

        Assert.assertEquals(dotGraph, "digraph G { \n" +
                " \t node [shape=Mrecord]; \n" +
                " \t node [style=filled]; \n" +
                " \n" +
                "  \tPERSON [ shape=octagon, label=\"PERSON\", fillcolor=lightblue]  \n" +
                " subgraph cluster_Props_PERSON { \n" +
                " \t color=green; \n" +
                " \t node [fillcolor=khaki3, shape=component]; \n" +
                " \t PERSON_PERSON_NAME[fillcolor=white, label=\"PERSON_NAME\" ]\n" +
                " \t PERSON->PERSON_PERSON_NAME\n" +
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
                "  \tPERSONOWNER [ shape=rarrow, label=\"PERSONOWNER\", fillcolor=darkkhaki]  \n" +
                " subgraph cluster_Props_PERSONOWNER { \n" +
                " \t color=green; \n" +
                " \t node [fillcolor=khaki3, shape=component]; \n" +
                " \n" +
                " } \n" +
                " \t ACCOUNT->PERSONOWNER->PERSON\n" +
                " \n" +
                "  \tCOMPANYOWNER [ shape=rarrow, label=\"COMPANYOWNER\", fillcolor=darkkhaki]  \n" +
                " subgraph cluster_Props_COMPANYOWNER { \n" +
                " \t color=green; \n" +
                " \t node [fillcolor=khaki3, shape=component]; \n" +
                " \n" +
                " } \n" +
                " \t ACCOUNT->COMPANYOWNER->COMPANY\n" +
                " \n" +
                "  \tWORKSFOR [ shape=rarrow, label=\"WORKSFOR\", fillcolor=darkkhaki]  \n" +
                " subgraph cluster_Props_WORKSFOR { \n" +
                " \t color=green; \n" +
                " \t node [fillcolor=khaki3, shape=component]; \n" +
                " \n" +
                " } \n" +
                " \t PERSON->WORKSFOR->COMPANY\n" +
                " \n" +
                "  \n" +
                "\t }");
    }
}