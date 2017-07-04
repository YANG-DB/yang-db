package com.kayhut.fuse.neo4j.cypher;


import javaslang.collection.Stream;
import javaslang.control.Option;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by elad on 06/03/2017.
 */
public class CypherStatement {

    private CypherMatch match;
    private CypherReturn returns;
    private CypherWhere where;
    private CypherWith with;
    private int relsCounter = 0;
    private int aggsCounter = 0;
    private int nodesCounter = 0;

    private CypherStatement() {
        match = CypherMatch.cypherMatch();
        where = CypherWhere.cypherWhere();
        returns = CypherReturn.cypherReturn();
        with = CypherWith.cypherWith();
    }

    public static CypherStatement cypherStatement() {
        return new CypherStatement();
    }

    public CypherStatement copy() {
        CypherStatement newCs = new CypherStatement();
        newCs.match = match.copy();
        newCs.where = where.copy();
        newCs.returns = returns.copy();
        newCs.with = with.copy();
        return newCs;
    }

    public CypherStatement appendNode(String pathTag, CypherNode node) {
        if(!match.getPaths().containsKey(pathTag)) {
            match.addPath(CypherPath.cypherPath(pathTag));
        }
        match.getPaths().get(pathTag).appendNode(node);
        return this;
    }

    public CypherStatement appendRel(String pathTag, CypherRelationship rel) {
        if(!match.getPaths().containsKey(pathTag)) {
            match.addPath(CypherPath.cypherPath(pathTag));
        }
        match.getPaths().get(pathTag).appendRelationship(rel);
        return this;
    }

    public CypherStatement appendCondition(CypherCondition cond) {
        where.conditions.add(cond);
        return this;
    }

    public CypherStatement addReturn(CypherReturnElement returnElement) {
        returns.withElement(returnElement);
        return this;
    }

    public CypherStatement addWith(CypherReturnElement returnElement) {
        with.withElement(returnElement);
        return this;
    }

    public String getNextPathTag() {
        return "p" + match.getPaths().size();
    }

    public CypherPath getPath(String tag) {
        return match.getPaths().get(tag);
    }

    public String getNewNodeTag() {
        return "n" + (++nodesCounter);
    }

    public String getNewRelTag() {
        return "r" + (++relsCounter);
    }

    public String getNewAggTag() {
        return "agg" + (++aggsCounter);
    }

    public CypherReturn getReturns() {
        return returns;
    }

    private void removeRedundantPaths() {

        if(match.getPaths().size() == 1) {
            return;
        }

        List<String> redundantPaths = new ArrayList<>();

        List<CypherPath> paths = Stream.ofAll(match.getPaths().values()).toJavaList();

        for(int i=0; i<paths.size(); i++) {

            CypherPath curPath = paths.get(i);

            if (curPath.pathElements.size() == 1) {

                CypherElement element = curPath.getElementFromEnd(1);

                //check if this element also appears in other paths
                List<CypherPath> otherPaths = Stream.ofAll(paths).filter(p -> !p.tag.equals(curPath.tag) && !Stream.ofAll(p.pathElements).find(e -> e.tag.equals(element.tag)).toJavaList().isEmpty()).toJavaList();

                if (otherPaths.isEmpty()) {
                    continue;
                }

                otherPaths.forEach(otherPath -> {

                    Option<CypherElement> otherElement = Stream.ofAll(otherPath.pathElements).find(e -> e.tag.equals(element.tag));
                    if (!otherElement.isEmpty()) {
                        if (otherElement.get().label == null) {
                            otherElement.get().label = element.label;
                        }
                        if (otherElement.get().inlineProps == null) {
                            otherElement.get().inlineProps = element.inlineProps;
                        } else {
                            if (element.inlineProps != null) {
                                element.inlineProps.forEach((k, v) -> {
                                    if (!otherElement.get().inlineProps.containsKey(k)) {
                                        otherElement.get().inlineProps.put(k, v);
                                    }
                                });
                            }
                        }
                    }
                });

                //remove redundant path
                redundantPaths.add(curPath.tag);

            }

        }

        redundantPaths.forEach(tag -> match.removePath(tag));

    }

    private void validateWithAndReturn() {

        if (with.getElements().size() == 0) {
            return;
        }

        //make sure that every tag in the return clause also appears in the with clause
        for (CypherReturnElement retEl :
                returns.getElements()) {
            boolean isFound = false;
            for (CypherReturnElement withEl :
                    with.getElements()) {

                if (isSameElement(retEl, withEl)) {
                    isFound = true;
                    break;
                }

            }
            if (!isFound) {
                //add to with clause
                with.withElement(retEl);
            }
        }
    }

    private boolean isSameElement(CypherReturnElement a, CypherReturnElement b) {
        if(a.getAlias() != null && b.getAlias() != null) {
            return a.getAlias().equals(b.getAlias());
        }
        else {
            if(a.getAlias() != null) {
                return a.getAlias().equals(b.getTag());
            } else if (b.getAlias() != null) {
                return b.getAlias().equals(a.getTag());
            }
        }
        return a.getTag().equals(b.getTag());
    }

    public String toString() {
        removeRedundantPaths();
        validateWithAndReturn();
        StringBuilder sb = new StringBuilder();
        sb.append(match.toString());
        sb.append(with.toString());
        sb.append(where.toString());
        sb.append(returns.toString());
        return sb.toString();
    }

}
