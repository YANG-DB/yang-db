package com.kayhut.fuse.neo4j.cypher;


/**
 * Created by User on 19/03/2017.
 */
public class CypherCondition {

    public CypherCondition copy() {
        CypherCondition c = new CypherCondition();
        c.target = target;
        c.value = value;
        c.targetFunction = targetFunction;
        c.valueFunction = valueFunction;
        c.operator = operator;
        c.type = type;
        return c;
    }

    public enum Condition{AND, OR}

    String target;
    String value;
    String targetFunction;
    String valueFunction;
    String operator;
    Condition type;

    private CypherCondition(){

    }

    public static CypherCondition cypherCondition() {
        return new CypherCondition();
    }

    public CypherCondition withTarget(String t) {
        target = t;
        return this;
    }

    public CypherCondition withValue(String v) {
        value = v;
        return this;
    }

    public CypherCondition withTargetFunc(String tf) {
        targetFunction = tf;
        return this;
    }

    public CypherCondition withValueFunc(String vf) {
        targetFunction = vf;
        return this;
    }

    public CypherCondition withOperator(String op) {
        operator = op;
        return this;
    }

    public CypherCondition withType(Condition t) {
        type = t;
        return this;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if(targetFunction != null) {
            sb.append(targetFunction + "(");
        }
        sb.append(target);
        if(targetFunction != null) {
            sb.append(")");
        }
        sb.append(" " + operator + " ");
        if(valueFunction != null) {
            sb.append(valueFunction + "(");
        }
        sb.append(value);
        if(valueFunction != null) {
            sb.append(")");
        }
        return sb.toString();
    }
}
