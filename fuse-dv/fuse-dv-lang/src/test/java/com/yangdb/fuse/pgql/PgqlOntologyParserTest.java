package com.yangdb.fuse.pgql;

import com.yangdb.fuse.model.ontology.*;
import junit.framework.TestCase;
import oracle.pgql.lang.Pgql;
import oracle.pgql.lang.PgqlException;
import oracle.pgql.lang.PgqlResult;
import org.junit.Assert;
import org.junit.Test;

public class PgqlOntologyParserTest extends TestCase {
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

    @Test
    public void testTransform() {
        try (Pgql pgql = new Pgql()) {
            //parse DDL graph query
            PgqlResult result3 = pgql.parse(DDL_QUERY);
            System.out.println(result3.getPgqlStatement());
            Ontology ontology = new PgqlOntologyParser().transform("financial_transactions", DDL_QUERY);
            Ontology.Accessor accessor = new Ontology.Accessor(ontology);

            Assert.assertEquals(accessor.$entity$("PERSON"), EntityType.Builder.get()
                    .withEType("PERSON").withName("PERSON").withProperty("NAME").build());
            Assert.assertEquals(accessor.$entity$("COMPANY"), EntityType.Builder.get()
                    .withEType("COMPANY").withName("COMPANY").withProperty("NAME").build());
            Assert.assertEquals(accessor.$entity$("ACCOUNT"), EntityType.Builder.get()
                    .withEType("ACCOUNT").withName("ACCOUNT").withProperty("NUMBER").build());

            Assert.assertEquals(accessor.property$("PERSON.NAME"),new Property("PERSON.NAME","PERSON.NAME", Ontology.OntologyPrimitiveType.STRING.name()));
            Assert.assertEquals(accessor.property$("COMPANY.NAME"),new Property("COMPANY.NAME","COMPANY.NAME", Ontology.OntologyPrimitiveType.STRING.name()));
            Assert.assertEquals(accessor.property$("ACCOUNT.NUMBER"),new Property("ACCOUNT.NUMBER","ACCOUNT.NUMBER", Ontology.OntologyPrimitiveType.STRING.name()));

            Assert.assertEquals(accessor.$relation$("Transactions"), RelationshipType.Builder.get()
                    .withDBrName("Transactions")
                    .withEPair(EPair.EPairBuilder.anEPair()
                            .with("Accounts","Accounts")
                            .withSideAIdField("from_account")
                            .withSideBIdField("to_account"))
                    .withProperty("amount").build());
        } catch (PgqlException e) {
            Assert.fail(e.getMessage());
        }

    }
}