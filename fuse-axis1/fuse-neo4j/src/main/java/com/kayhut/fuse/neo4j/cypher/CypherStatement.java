package com.kayhut.fuse.neo4j.cypher;


/**
 * Created by elad on 06/03/2017.
 */
public class CypherStatement {

    private CypherMatch match;
    private CypherReturn returns;
    private CypherWhere where;

    private CypherStatement() {
        match = CypherMatch.cypherMatch();
        where = CypherWhere.cypherWhere();
        returns = CypherReturn.cypherReturn();
    }

    public static CypherStatement cypherStatement() {
        return new CypherStatement();
    }

    public CypherStatement copy() {
        CypherStatement newCs = new CypherStatement();
        newCs.match = match.copy();
        newCs.where = where.copy();
        newCs.returns = returns.copy();
        return newCs;
    }

    public CypherStatement withMatch(CypherMatch m) {
        match = m;
        return this;
    }

    public CypherStatement withWhere(CypherWhere w) {
        where = w;
        return this;
    }

    public CypherStatement withReturn(CypherReturn r) {
        returns = r;
        return this;
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

    public CypherMatch getMatch() {
        return match;
    }

    public CypherReturn getReturns() {
        return returns;
    }

    public CypherWhere getWhere() {
        return where;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(match.toString());
        sb.append("\n");
        if(getWhere().conditions.size() > 0) {
            sb.append(where.toString());
            sb.append("\n");
        }
        sb.append(returns.toString());
        sb.append("\n");
        return sb.toString();
    }

    public String getNextPathTag() {
        return "p" + match.getPaths().size();
    }

    public CypherPath getPath(String tag) {
        return match.getPaths().get(tag);
    }

    public String startNewPath() {
        String tag = getNextPathTag();
        match.addPath(CypherPath.cypherPath(tag));
        return tag;
    }
}