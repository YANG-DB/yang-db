package com.kayhut.fuse.neo4j.cypher;


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

    public String getNewRelTag() {
        return "r" + (++relsCounter);
    }

    public String getNewAggTag() {
        return "agg" + (++aggsCounter);
    }

    public CypherReturn getReturns() {
        return returns;
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
        validateWithAndReturn();
        StringBuilder sb = new StringBuilder();
        sb.append(match.toString());
        sb.append(with.toString());
        sb.append(where.toString());
        sb.append(returns.toString());
        return sb.toString();
    }

}
