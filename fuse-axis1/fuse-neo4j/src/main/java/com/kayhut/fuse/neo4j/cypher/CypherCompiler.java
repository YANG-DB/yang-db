package com.kayhut.fuse.neo4j.cypher;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.combiner.EComb;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.quant.Quant1;
import org.parboiled.common.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by EladW on 23/02/2017.
 */
public class CypherCompiler {

    static Logger logger = LoggerFactory.getLogger("com.kayhut.fuse.neo4j.cypher.CypherCompiler");

    //Traverses a tree of query elements, building the cypher statements along the way.
    //Each leaf node will contain a complete ready-to-run cypher statement, describing the single path from the root node to this leaf node.
    private static void cypherTreeTraverse(AsgEBase curNode,
                                           AsgEBase ancestor,
                                           Map<AsgEBase, Tuple2<CypherStatement, String>> cypherStatemenstMap,
                                           Ontology ontology) {

        //start node - initialize an empty cypher statement
        if(ancestor == null) {
            CypherStatement newStatement = CypherStatement.cypherStatement();
            cypherStatemenstMap.put(curNode,new Tuple2(newStatement, newStatement.getNextPathTag()));
        }

        //append to ancestor's statement
        if (ancestor != null) {
            Tuple2<CypherStatement, String> ancestorStatement = cypherStatemenstMap.get(ancestor);
            if(ancestor.geteBase() instanceof Quant1) {
                Quant1 quant1 = (Quant1) ancestor.geteBase();
                if(quant1.getqType().equals("all")) {
                    //start new path
                    cypherStatemenstMap.put(curNode,
                                            new Tuple2<>(
                                                    append(ancestorStatement.a,
                                                           ancestorStatement.a.getNextPathTag(),
                                                           curNode.geteBase(),
                                                           ontology),
                                                    ancestorStatement.a.getNextPathTag()));
                }
                else if(quant1.getqType().equals("some")) {
                    cypherStatemenstMap.put(curNode,
                                            new Tuple2<>(
                                                    append(ancestorStatement.a.copy(),
                                                           ancestorStatement.b,
                                                           curNode.geteBase(),
                                                           ontology),
                                                    ancestorStatement.b));
                }
            } else {
                cypherStatemenstMap.put(curNode,
                        new Tuple2<>(
                                append(ancestorStatement.a,
                                       ancestorStatement.b,
                                       curNode.geteBase(),
                                       ontology),
                                ancestorStatement.b));
            }
        }

        //recursively apply to children
        curNode.getNext().forEach(child -> cypherTreeTraverse((AsgEBase) child,curNode,cypherStatemenstMap,ontology));

    }

    private static CypherStatement append(CypherStatement cypherStatement,String pathTag, EBase eBase, Ontology ontology) {

        if(eBase instanceof ETyped) {

            ETyped eTyped = (ETyped) eBase;

            Optional<String> label = ontology.getEntityLabel(eTyped.geteType());

            if(!label.isPresent()) {
                throw new RuntimeException("Failed compiling query. Unknown entity type: " + eTyped.geteType());
            }

            CypherNode node = CypherNode.cypherNode()
                                        .withTag(eTyped.geteTag())
                                        .withLabel(label.get());

            CypherReturnElement returnElement = CypherReturnElement.cypherReturnElement(node);

            return cypherStatement.appendNode(pathTag,node).addReturn(returnElement);

        }
        if(eBase instanceof EConcrete) {
            EConcrete eConcrete = (EConcrete)eBase;

            Optional<String> label = ontology.getEntityLabel(eConcrete.geteType());

            if(!label.isPresent()) {
                throw new RuntimeException("Failed compiling query. Unknown entity type: " + eConcrete.geteType());
            }

            CypherNode node = CypherNode.cypherNode()
                    .withTag(eConcrete.geteTag())
                    .withLabel(label.get());

            CypherCondition cond = CypherCondition.cypherCondition()
                                                  .withOperator("=")
                                                  .withTarget(eConcrete.geteTag() + ".id")
                                                  .withValue(eConcrete.geteID());

            CypherReturnElement returnElement = CypherReturnElement.cypherReturnElement(node);

            return cypherStatement.appendNode(pathTag,node).appendCondition(cond).addReturn(returnElement);
        }
        if(eBase instanceof Rel) {

            Rel rel = (Rel)eBase;
            Optional<String> label = ontology.getRelationLabel(rel.getrType());

            if(!label.isPresent()) {
                throw new RuntimeException("Failed compiling query. Unknown entity type: " + rel.getrType());
            }

            CypherRelationship crel = CypherRelationship.cypherRel()
                                                        .withLabel(label.get())
                                                        .withDirection(rel.getDir().equals("R") ? CypherRelationship.Direction.RIGHT :
                                                                      (rel.getDir().equals("L") ? CypherRelationship.Direction.LEFT :
                                                                                                  CypherRelationship.Direction.BOTH));

            return cypherStatement.appendRel(pathTag,crel);

        }
        return cypherStatement;
    }

    public static String compile(AsgQuery asgQuery, Ontology ontology) {

        Map<AsgEBase, Tuple2<CypherStatement,String>> cypherStatements = new HashMap<>();

        AsgEBase<Start> start = asgQuery.getStart();

        cypherTreeTraverse(start,null,cypherStatements,ontology);

        CypherStatement finalStatement = CypherStatement.union(cypherStatements.values()
                                                                               .stream()
                                                                               .map(t -> t.a)
                                                                               .distinct()
                                                                               .collect(Collectors.toList()));

        logger.info(String.format("\n(%s) -[:Compiled]-> ( %s)", asgQuery.getName(), finalStatement.toString()));

        return finalStatement.toString();

    }

}
