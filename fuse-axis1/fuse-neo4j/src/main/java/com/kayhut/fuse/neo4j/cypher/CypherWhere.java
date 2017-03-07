package com.kayhut.fuse.neo4j.cypher;


/**
 * Created by elad on 06/03/2017.
 */
public class CypherWhere {

    final String WHERE = " WHERE ";
    final String CONDITION_FORMAT = "%s %s %s %s";
    final String TAG_FIELD_FORMAT = "%s.%s";
    final String FUNC_FORMAT = "%s(%s)";

    //TODO: define operators and functions converter from v1 to cypher

    private StringBuilder builder;

    public enum ConditionType {AND, OR, NONE}

    public enum OpType {LARGER, SMALLER, EQUALS}

    public CypherWhere() {
        builder = new StringBuilder(WHERE);
    }

    public CypherWhere(String initialString) {
        builder = new StringBuilder(initialString);
    }

    public void appendUnary(ConditionType condType, String tag,String field, String func, OpType opType, String value) {
        builder.append(String.format(CONDITION_FORMAT,
                                     builder.toString().endsWith(WHERE) ? "" : condType,
                                     func == null ? String.format(TAG_FIELD_FORMAT, tag, field) :
                                                    String.format(FUNC_FORMAT, func, String.format(TAG_FIELD_FORMAT,tag, field)),
                                     opType == OpType.EQUALS ? "=" : ">",
                                     value));
     }

    public void appendBinary(ConditionType condType, String tagA, String funcA,String tagB, String funcB, OpType opType) {
        //TODO: add field names and create

    }

    @Override
    public String toString() {
        if(builder.toString().endsWith(WHERE)) {
            return "";
        }
        return builder.toString();
    }
}
