package com.kayhut.fuse.neo4j.cypher.strategy;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.ontology.OntologyUtil;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.neo4j.cypher.CypherNode;
import com.kayhut.fuse.neo4j.cypher.CypherRelationship;
import com.kayhut.fuse.neo4j.cypher.CypherReturnElement;
import com.kayhut.fuse.neo4j.cypher.CypherStatement;
import javaslang.Tuple2;
import org.neo4j.shell.kernel.apps.cypher.Cypher;

import java.util.Map;
import java.util.Optional;

/**
 * Created by User on 26/03/2017.
 */
public class TypedRelCypherStrategy extends CypherStrategy {

    public TypedRelCypherStrategy(Ontology ontology, Map<AsgEBase, Tuple2<CypherStatement, String>> cypherStatementsMap) {
        super(ontology,cypherStatementsMap);
    }

    public CypherStatement apply(AsgEBase element) {

        if (element.geteBase() instanceof Rel) {

            Rel eTyped = (Rel) element.geteBase();

            Optional<String> label = OntologyUtil.getEntityLabel(ontology, eTyped.getrType());

            if (!label.isPresent()) {
                throw new RuntimeException("Failed compiling query. Unknown entity type: " + eTyped.getrType());
            }

            CypherRelationship rel = CypherRelationship.cypherRel().withLabel(label.get());
            CypherReturnElement returnElement = CypherReturnElement.cypherReturnElement(rel);
            Tuple2<CypherStatement, String> workingStatement = getWorkingStatement(element);
            return context(element, workingStatement._1().appendRel(workingStatement._2, rel).addReturn(returnElement));
        }
        return getWorkingStatement(element)._1();
    }
}

