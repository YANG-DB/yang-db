package com.kayhut.fuse.neo4j.cypher.strategy;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.neo4j.cypher.CypherElement;
import com.kayhut.fuse.neo4j.cypher.CypherNode;
import com.kayhut.fuse.neo4j.cypher.CypherReturnElement;
import com.kayhut.fuse.neo4j.cypher.CypherStatement;
import javaslang.Tuple2;

import java.util.Map;
import java.util.Optional;

import static com.kayhut.fuse.neo4j.cypher.CypherNode.EMPTY;

/**
 * Created by User on 26/03/2017.
 */
public class TypedNodeCypherStrategy extends CypherStrategy {

    public TypedNodeCypherStrategy(Ontology ontology, Map<AsgEBase, Tuple2<CypherStatement, String>> cypherStatementsMap) {
        super(ontology,cypherStatementsMap);
    }

    public CypherStatement apply(AsgEBase element) {

        if (element.geteBase() instanceof ETyped) {

            ETyped eTyped = (ETyped) element.geteBase();

            Optional<String> label = ontology.getEntityLabel(eTyped.geteType());

            if (!label.isPresent()) {
                throw new RuntimeException("Failed compiling query. Unknown entity type: " + eTyped.geteType());
            }

            CypherNode node = CypherNode.cypherNode()
                    .withTag(eTyped.geteTag())
                    .withLabel(label.get());

            CypherReturnElement returnElement = CypherReturnElement.cypherReturnElement(node);
            Tuple2<CypherStatement, String> workingStatement = getWorkingStatement(element);
            return context(element, workingStatement._1().appendNode(workingStatement._2, node).addReturn(returnElement));
        }
        return getWorkingStatement(element)._1();

    }
}

