package com.kayhut.fuse.neo4j.cypher;

import com.kayhut.fuse.model.query.*;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.quant.Quant1;
import com.kayhut.fuse.model.query.quant.Quant2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by EladW on 23/02/2017.
 */
public class CypherCompiler {

    static Logger logger = LoggerFactory.getLogger("com.kayhut.fuse.neo4j.cypher.CypherCompiler");

    // A node in the query tree
    private static class QueryElementTreeNode {
        EBase value;
        ArrayList<QueryElementTreeNode> children;
        CypherStatement cypher;

        public QueryElementTreeNode(EBase val) {
            value = val;
        }

        public void addChildNode(QueryElementTreeNode child) {
            if(children == null) {
                children = new ArrayList<>();
            }
            children.add(child);
        }

        public void setCypher(CypherStatement statement) {
            cypher = statement;
        }
    }

    // A tree representing a query
    private static class QueryElementsTree {
        QueryElementTreeNode root;

        public QueryElementsTree(QueryElementTreeNode r) {
            root = r;
        }
    }

    // Gets a Query, and return a tree of elements representing that query
    private static QueryElementsTree buildTree(Query query) {

        EBase startElement = query.getElements().stream().filter(element -> element instanceof Start).findFirst().get();

        QueryElementTreeNode root = new QueryElementTreeNode(startElement);

        buildSubTree(root, query.getElements());

        return new QueryElementsTree(root);
    }

    // Gets a node in the tree, and builds the sub-tree rooted at that node
    private static void buildSubTree(QueryElementTreeNode node, List<EBase> elementsList) {

        elementsList.stream()
                //find node's children - the elements that their number is contained in the node's next list
                //todo: move next-is-not-start to getNext()
                .filter(entry -> !getNext(node.value).contains(0) && getNext(node.value).contains(entry.geteNum()))
                .collect(Collectors.toList())
                //recursively build sub-trees for each child node
                .forEach(entry -> {
                    QueryElementTreeNode childNode = new QueryElementTreeNode(entry);
                    buildSubTree(childNode, elementsList);
                    //add child node to the node's children list
                    node.addChildNode(childNode);
                });

    }

    //Returns the next elements following a given element
    private static List<Integer> getNext(EBase e) {
        if( e instanceof Start) {
            ArrayList<Integer> list = new ArrayList<>();
            list.add(((Start)e).getNext());
            return list;
        }
        if(e instanceof Quant1) {
            return ((Quant1)e).getNext();
        }
        if(e instanceof Quant2) {
            return ((Quant2)e).getNext();
        }
        if( e instanceof ETyped) {
            ArrayList<Integer> list = new ArrayList<>();
            list.add(((ETyped)e).getNext());
            return list;
        }
        if( e instanceof Rel) {
            ArrayList<Integer> list = new ArrayList<>();
            list.add(((Rel)e).getNext());
            return list;
        }
        return new ArrayList<>();
    }

    //Traverses a tree of query elements and building the cypher statement along the way.
    //Each leaf node will contain a complete ready to run cypher statement, describing the single path from the root node to this leaf node.
    private static void cypherTreeTraverse(QueryElementTreeNode curNode, QueryElementTreeNode ancestor, Schema schema) throws CypherCompilerException {

        //start with ancestor statement, and update it
        if (ancestor != null) {

            CypherStatement curStmt = ancestor.cypher.copy();

            if (curNode.value instanceof ETyped) {
                curStmt.getMatch().appendNode(((ETyped) curNode.value).geteTag(),
                        schema.getEntityLabel(((ETyped) curNode.value).geteType()),
                        null);
                curStmt.getReturn().append(((ETyped) curNode.value).geteTag(), null, null);
            }

            if (curNode.value instanceof Rel) {
                curStmt.getMatch().appendRelationship(null,
                        schema.getRelationLabel(((Rel) curNode.value).getrType()),
                        null,
                        ((Rel) curNode.value).getDir() == "R" ? CypherMatch.Direction.RIGHT : CypherMatch.Direction.LEFT);
            }

            if (curNode.value instanceof EProp) {
                EProp eprop = (EProp) curNode.value;
                Schema.Property prop = schema.getProperty(((ETyped) ancestor.value).geteType(), eprop.getpType());
                curStmt.getWhere().appendUnary(CypherWhere.ConditionType.AND,
                        ((ETyped) ancestor.value).geteTag(),
                        schema.getPropertyField(schema.getProperty(((ETyped) ancestor.value).geteType(), eprop.getpType()).name),
                        null,
                        eprop.getCon().getOp() == ConstraintOp.eq ? CypherWhere.OpType.EQUALS :
                                CypherWhere.OpType.LARGER,
                        prop.type.equals("string") ? String.format("'%s'", eprop.getCon().getExpr()) :
                                String.valueOf(eprop.getCon().getExpr()));

            }

            curNode.cypher = curStmt;
        }

        if (curNode.children != null) {
            for (QueryElementTreeNode child :
                    curNode.children) {
                cypherTreeTraverse(child, curNode, schema);
            }
        }

    }

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

    public static String compile(com.kayhut.fuse.model.query.Query query, Schema schema) throws CypherCompilerException {

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

}
