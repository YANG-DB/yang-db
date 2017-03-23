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
import com.kayhut.fuse.model.query.quant.Quant2;
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
                                           Map<AsgEBase, Tuple2<CypherStatement, String>> cypherStatementsMap,
                                           Ontology ontology) {

        if(nodesQueue.isEmpty()) return;

        AsgEBase curNode = nodesQueue.poll();

        AsgEBase ancestor = null;

        List<AsgEBase> parents = curNode.getParents();

        if(parents != null && parents.size() > 0) {
            ancestor = parents.get(0);
        }

        //start node - initialize an empty cypher statement, with one path
        if (ancestor == null) {

            CypherStatement newStatement = CypherStatement.cypherStatement();

            cypherStatementsMap.put(curNode, new Tuple2(newStatement, newStatement.getNextPathTag()));

        } else {

            CypherStatement curStatement = cypherStatementsMap.get(curNode) == null ? cypherStatementsMap.get(ancestor).a : cypherStatementsMap.get(curNode).a;

            String curPath = cypherStatementsMap.get(curNode) == null ? cypherStatementsMap.get(ancestor).b : cypherStatementsMap.get(curNode).b;

            if (curNode.geteBase() instanceof Quant1 || curNode.geteBase() instanceof Quant2) {

                String qType = "all";
                if(curNode.geteBase() instanceof Quant1) {
                    qType = ((Quant1)curNode.geteBase()).getqType();
                } else if(curNode.geteBase() instanceof Quant2) {
                    qType = ((Quant2)curNode.geteBase()).getqType();
                }

                List<AsgEBase> children = curNode.getNext();

                switch (qType) {
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
                                } else {
                                    cypherStatementsMap.put(children.get(i), new Tuple2<>(curStatement, curPath));
                                }
                            }
                        }
                        break;
                    case "all":
                        for (int i = 0; i < children.size(); i++) {
                            //first child maintains original path
                            if (i == 0) {
                                cypherStatementsMap.put(children.get(i),
                                        new Tuple2<>(curStatement, curPath));
                            } else {


                                if (!(children.get(i).geteBase() instanceof EProp) &&
                                        !(children.get(i).geteBase() instanceof RelProp)) {

                                    String newPathTag = curStatement.startNewPath();

                                    cypherStatementsMap.put(children.get(i),
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
                                } else {
                                    cypherStatementsMap.put(children.get(i),
                                            new Tuple2<>(curStatement, curPath));
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

        cypherTreeTraverse(nodesQueue, cypherStatementsMap, ontology);

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
                    .withType(CypherCondition.Condition.AND)
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
                    .withLabel(label.get().replace(" ","_"))
                    .withDirection(rel.getDir().equals("R") ? CypherRelationship.Direction.RIGHT :
                            (rel.getDir().equals("L") ? CypherRelationship.Direction.LEFT :
                                    CypherRelationship.Direction.BOTH));

            return cypherStatement.appendRel(pathTag, crel);

        }
        if (eBase instanceof EProp || eBase instanceof RelProp) {

            return cypherStatement.appendCondition(getPropertyCondition(asgNode, cypherStatement, pathTag, ontology));

        }

        return cypherStatement;
    }

    public static String compile(AsgQuery asgQuery, Ontology ontology) {

        Map<AsgEBase, Tuple2<CypherStatement, String>> cypherStatements = new HashMap<>();

        AsgEBase<Start> start = asgQuery.getStart();

        Queue<AsgEBase> q = new LinkedList<>();

        q.add(start);

        cypherTreeTraverse(q, cypherStatements, ontology);

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

    private static CypherCondition getPropertyCondition(AsgEBase asgNode, CypherStatement cypherStatement, String pathTag, Ontology ont) {

        CypherElement lastElement = cypherStatement.getPath(pathTag).getElementFromEnd(1);

        Optional<Property> property = getProperty(asgNode, ont);

        if(!property.isPresent()) {
            // ??
            throw new RuntimeException("Could not find property for Eprop.");
        }

        Constraint con = asgNode.geteBase() instanceof EProp ? ((EProp)asgNode.geteBase()).getCon() : ((RelProp)asgNode.geteBase()).getCon();

        ConstraintOp op = con.getOp();

        String val = property.get().getType().equals("int") ? (String) con.getExpr() : "'" + con.getExpr() + "'";

        Object parent = asgNode.getParents().stream().filter(p -> ((AsgEBase) p).geteBase() instanceof Quant1 ||
                                                             ((AsgEBase) p).geteBase() instanceof Quant2).findAny().get();

        String qType = "all";
        if(parent instanceof Quant1) {
            qType = ((Quant1)parent).getqType();
        } else if(parent instanceof Quant2) {
            qType = ((Quant2)parent).getqType();
        }

        CypherCondition cond = CypherCondition.cypherCondition()
                                              .withTarget(String.format("%s.%s", lastElement.tag, property.get().getName().replace(" ","_")))
                                              .withValue(val)
                                              .withOperator(getOp(op))
                                              .withType(qType.equals("all") ? CypherCondition.Condition.AND : CypherCondition.Condition.OR);

        return cond;

    }

    private static Optional<Property> getProperty(AsgEBase asgNode, Ontology ont) {

        Queue<AsgEBase> parents = new LinkedList<>(asgNode.getParents());

        int pType = 0;
        if (asgNode.geteBase() instanceof EProp) {
            pType = Integer.valueOf(((EProp) asgNode.geteBase()).getpType());
        } else if (asgNode.geteBase() instanceof RelProp) {
            pType = Integer.valueOf(((RelProp) asgNode.geteBase()).getpType());
        }

        while (!parents.isEmpty()) {
            AsgEBase p = parents.poll();
            if (p.geteBase() instanceof ETyped) {
                return ont.getProperty(((ETyped) p.geteBase()).geteType(), pType);
            }
            if (p.geteBase() instanceof Rel) {
                return ont.getProperty(((Rel) p.geteBase()).getrType(), pType);
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
                return " <> ";
        }
    }
}
