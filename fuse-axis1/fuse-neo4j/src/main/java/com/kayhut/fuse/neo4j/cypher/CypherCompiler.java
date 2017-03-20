package com.kayhut.fuse.neo4j.cypher;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.ETyped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by EladW on 23/02/2017.
 */
public class CypherCompiler {

    static Logger logger = LoggerFactory.getLogger("com.kayhut.fuse.neo4j.cypher.CypherCompiler");

    //Traverses a tree of query elements, building the cypher statements along the way.
    //Each leaf node will contain a complete ready-to-run cypher statement, describing the single path from the root node to this leaf node.
    private static void cypherTreeTraverse(AsgEBase curNode,
                                           AsgEBase ancestor,
                                           List<CypherStatement> cypherStatemenstMap,
                                           Ontology ontology) {

        //start node - initialize an empty cypher statement
        if(ancestor == null) {
            //TODO: add new cypher statement to list
            //cypherStatemenstMap.addput(curNode,CypherStatement.cypherStatement());
        }

        //append to ancestor's statement
        if (ancestor != null) {
            //TODO: pass ancestor's statement along the way
            //TODO: When to create new statement ??
            // cypherStatemenstMap.put(curNode, append(cypherStatemenstMap.get(ancestor),curNode.geteBase(),ontology));
        }

        //recursively apply to children
        curNode.getNext().forEach(child -> cypherTreeTraverse((AsgEBase) child,curNode,cypherStatemenstMap,ontology));

    }

    private static CypherStatement append(CypherStatement cypherStatement, EBase eBase, Ontology ontology) {

        CypherStatement newStmt = cypherStatement.copy();

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

            //TODO: use path tag
            return newStmt.appendNode("",node).addReturn(returnElement);

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

            //TODO: use path tag
            return newStmt.appendNode("",node).appendCondition(cond).addReturn(returnElement);
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
            //TODO: use path tag
            return newStmt.appendRel("",crel);

        }
        return newStmt;
    }

    /*
    //Perform a post order traversal on the tree (bottom-up), collecting cypher statements from the leaf nodes,
    // and combining them on AND \ OR nodes along the way.
    //The result should be a single cypher statement represented by the query tree.
    private static CypherStatement collectFinalStatement(QueryElementTreeNode node) {

        if(node.children != null) {

            if(node.children.size() == 1) {
                 node.cypher = collectFinalStatement(node.children.get(0));
            }
            else {

                List<CypherStatement> childStatements = new ArrayList<>();

                for (QueryElementTreeNode child :
                        node.children) {
                    childStatements.add(collectFinalStatement(child));
                }

                //this node has to be a quantifier
                if(node.value instanceof Quant1) {

                    Quant1 quant = (Quant1) node.value;

                    if (quant.getqType().equals("all")) {

                        // unite patterns to a single match before returning
                        node.cypher = CypherStatement.and(childStatements);

                    }
                    if (quant.getqType().equals("some")) {

                        // union all statements before returning
                        node.cypher = CypherStatement.or(childStatements);

                    }

                }
            }

        }

        return node.cypher;
    }
    */

    public static String compile(AsgQuery asgQuery, Ontology ontology) {

        List<CypherStatement> cypherStatements = new LinkedList<>();

        //perform top-bottom traversal to build separate statements for each branch
        AsgEBase<Start> start = asgQuery.getStart();

        cypherTreeTraverse(start,null,cypherStatements,ontology);

        CypherStatement finalStatement = CypherStatement.union(cypherStatements);

        logger.info(String.format("\n(%s) -[:Compiled]-> ( %s)", asgQuery.getName(), finalStatement.toString()));

        return finalStatement.toString();
    }

    /*
    public static String compile(Query query, Ontology schema) throws CypherCompilerException {

        //build query tree
        QueryElementsTree tree = buildTree(query);

        //init tree root with an empty statement
        tree.root.setCypher(new CypherStatement());

        //build cypher statement for each path along the tree
        cypherTreeTraverse(tree.root, null, schema);

        CypherStatement statement = collectFinalStatement(tree.root);

        String cypher = statement.compose();

        logger.info(String.format("(%s) -[:Compiled]-> ( %s)", query.getName(), cypher));

        return cypher.toString();
    }
    */

}
