package com.kayhut.fuse.neo4j.cypher;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.ontology.Property;
import com.kayhut.fuse.model.query.*;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.model.query.quant.Quant1;
import org.parboiled.common.Tuple2;
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
    private static void cypherTreeTraverse(Queue<AsgEBase> nodesQueue,
                                           AsgEBase ancestor,
                                           Map<AsgEBase, Tuple2<CypherStatement, String>> cypherStatementsMap,
                                           Ontology ontology) {

        if(nodesQueue.isEmpty()) return;

        AsgEBase curNode = nodesQueue.poll();

        //start node - initialize an empty cypher statement, with one path
        if (ancestor == null) {

            CypherStatement newStatement = CypherStatement.cypherStatement();

            cypherStatementsMap.put(curNode, new Tuple2(newStatement, newStatement.getNextPathTag()));

        } else {

            CypherStatement curStatement = cypherStatementsMap.get(curNode) == null ? cypherStatementsMap.get(ancestor).a : cypherStatementsMap.get(curNode).a;

            String curPath = cypherStatementsMap.get(curNode) == null ? cypherStatementsMap.get(ancestor).b : cypherStatementsMap.get(curNode).b;

            if (curNode.geteBase() instanceof Quant1) {

                Quant1 quant = (Quant1) curNode.geteBase();

                List<AsgEBase> children = curNode.getNext();

                switch (quant.getqType()) {
                    case "some":
                        //assign new statements to all children except eprops
                        for (int i = 0; i < children.size(); i++) {
                            //first child maintains original statement
                            if (i == 0) {
                                cypherStatementsMap.put(children.get(i), new Tuple2<>(curStatement, curPath));
                            } else {
                                if (!(children.get(i).geteBase() instanceof EProp) &&
                                        !(children.get(i).geteBase() instanceof RelProp)) {
                                    cypherStatementsMap.put(children.get(i), new Tuple2<>(curStatement.copy(), curPath));
                                }
                            }
                        }
                        break;
                    case "all":
                        for (int i = 0; i < children.size(); i++) {
                            //first child maintains original path
                            if (i == 0) {
                                cypherStatementsMap.put((AsgEBase) children.get(i),
                                        new Tuple2<>(curStatement, curPath));
                            } else {

                                if (!(children.get(i).geteBase() instanceof EProp) &&
                                        !(children.get(i).geteBase() instanceof RelProp)) {
                                    String newPathTag = curStatement.startNewPath();

                                    cypherStatementsMap.put((AsgEBase) children.get(i),
                                            new Tuple2<>(curStatement, newPathTag));

                                    CypherElement lastElement = curStatement.getPath(curPath).getElementFromEnd(1);

                                    if (lastElement instanceof CypherNode) {
                                        curStatement.appendNode(newPathTag,
                                                CypherNode.cypherNode().withTag(lastElement.tag));
                                    } else {

                                        curStatement.appendNode(newPathTag,
                                                CypherNode.cypherNode().withTag(
                                                        curStatement.
                                                                getPath(curPath).
                                                                getElementFromEnd(2).tag));

                                        curStatement.appendRel(newPathTag,
                                                CypherRelationship.cypherRel().withTag(lastElement.tag));
                                    }
                                }
                            }
                        }
                        break;
                    default:
                        //TODO: more quantifiers
                        break;
                }
            } else {
                cypherStatementsMap.put(curNode,
                        new Tuple2<>(
                                append(curStatement,
                                        curPath,
                                        curNode,
                                        ontology),
                                curPath));
            }
        }

        //recursively apply to children
        curNode.getNext().forEach(child -> nodesQueue.add((AsgEBase) child));

        cypherTreeTraverse(nodesQueue, curNode, cypherStatementsMap, ontology);

    }

    private static CypherStatement append(CypherStatement cypherStatement, String pathTag, AsgEBase asgNode, Ontology ontology) {

        EBase eBase = asgNode.geteBase();

        if (eBase instanceof ETyped) {

            ETyped eTyped = (ETyped) eBase;

            Optional<String> label = ontology.getEntityLabel(eTyped.geteType());

            if (!label.isPresent()) {
                throw new RuntimeException("Failed compiling query. Unknown entity type: " + eTyped.geteType());
            }

            CypherNode node = CypherNode.cypherNode()
                    .withTag(eTyped.geteTag())
                    .withLabel(label.get());

            CypherReturnElement returnElement = CypherReturnElement.cypherReturnElement(node);

            return cypherStatement.appendNode(pathTag, node).addReturn(returnElement);

        }
        if (eBase instanceof EConcrete) {
            EConcrete eConcrete = (EConcrete) eBase;

            Optional<String> label = ontology.getEntityLabel(eConcrete.geteType());

            if (!label.isPresent()) {
                throw new RuntimeException("Failed compiling query. Unknown entity type: " + eConcrete.geteType());
            }

            CypherNode node = CypherNode.cypherNode()
                    .withTag(eConcrete.geteTag())
                    .withLabel(label.get());

            CypherCondition cond = CypherCondition.cypherCondition()
                    .withOperator("=")
                    .withTarget(eConcrete.geteTag())
                    .withTargetFunc("id") //TODO: id function is hard coded
                    .withValue(eConcrete.geteID());

            CypherReturnElement returnElement = CypherReturnElement.cypherReturnElement(node);

            return cypherStatement.appendNode(pathTag, node).appendCondition(cond).addReturn(returnElement);
        }
        if (eBase instanceof Rel) {

            Rel rel = (Rel) eBase;
            Optional<String> label = ontology.getRelationLabel(rel.getrType());

            if (!label.isPresent()) {
                throw new RuntimeException("Failed compiling query. Unknown entity type: " + rel.getrType());
            }

            CypherRelationship crel = CypherRelationship.cypherRel()
                    .withLabel(label.get())
                    .withDirection(rel.getDir().equals("R") ? CypherRelationship.Direction.RIGHT :
                            (rel.getDir().equals("L") ? CypherRelationship.Direction.LEFT :
                                    CypherRelationship.Direction.BOTH));

            return cypherStatement.appendRel(pathTag, crel);

        }
        if (eBase instanceof EProp) {

            return cypherStatement.appendCondition(getPropertyCondition(asgNode, cypherStatement, pathTag, ontology));

        }

        return cypherStatement;
    }

    public static String compile(AsgQuery asgQuery, Ontology ontology) {

        Map<AsgEBase, Tuple2<CypherStatement, String>> cypherStatements = new HashMap<>();

        AsgEBase<Start> start = asgQuery.getStart();

        Queue<AsgEBase> q = new LinkedList<>();

        q.add(start);

        cypherTreeTraverse(q, null, cypherStatements, ontology);

        CypherUnion union = CypherUnion.union();

        cypherStatements.values()
                .stream()
                .map(t -> t.a)
                .distinct()
                .forEach(st -> union.add(st));

        String finalCypher = union.toString();

        logger.info(String.format("\n(%s) -[:Compiled]-> ( %s)", asgQuery.getName(), finalCypher));

        return finalCypher;

    }

    private static CypherCondition getPropertyCondition(AsgEBase<EProp> eprop, CypherStatement cypherStatement, String pathTag, Ontology ont) {

        EProp prop = eprop.geteBase();

        CypherElement lastElement = cypherStatement.getPath(pathTag).getElementFromEnd(1);

        Optional<Property> property = getProperty(eprop, ont);

        if(!property.isPresent()) {
            // ??
            throw new RuntimeException("Could not find property for Eprop.");
        }

        Constraint con = prop.getCon();

        ConstraintOp op = con.getOp();

        String val = property.get().getType().equals("string") ? "'" + con.getExpr() + "'" : (String) con.getExpr();

        Quant1 quant = (Quant1) eprop.getParents().stream().filter(p -> p.geteBase() instanceof Quant1).findAny().get().geteBase();

        CypherCondition cond = CypherCondition.cypherCondition()
                                              .withTarget(String.format("%s.%s", lastElement.tag, property.get().getName().replace(" ","_")))
                                              .withValue(val)
                                              .withOperator(getOp(op))
                                              .withType(quant.getqType().equals("all") ? CypherCondition.Condition.AND : CypherCondition.Condition.OR);

        return cond;

    }

    private static Optional<Property> getProperty(AsgEBase<EProp> eprop, Ontology ont) {

        Queue<AsgEBase> parents = new LinkedList<>(eprop.getParents());

        while (!parents.isEmpty()) {
            AsgEBase p = parents.poll();
            if (p.geteBase() instanceof ETyped) {
                return ont.getProperty(((ETyped) p.geteBase()).geteType(), Integer.valueOf(eprop.geteBase().getpType()));
            } if(p.geteBase() instanceof  Rel) {
                return ont.getProperty(((Rel) p.geteBase()).getrType(), Integer.valueOf(eprop.geteBase().getpType()));
            } else {
                parents.addAll(p.getParents());
            }
        }
        return null;
    }

    private static String getOp(ConstraintOp op) {
        switch (op.name()) {
            case "lt":
                return "<";
            case "eq":
                return "=";
            case "le":
                return "<=";
            case "gt":
                return ">";
            case "ge":
                return ">=";
            default:
                return " != ";
        }
    }
}
