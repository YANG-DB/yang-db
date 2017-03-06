package com.kayhut.fuse.neo4j.cypher;

import java.util.IllegalFormatException;

/**
 * Created by elad on 06/03/2017.
 */
public class CypherReturn {

    final String RETURN = " RETURN ";
    final String TAG_ONLY_FORMAT = "%s,";
    final String TAG_WITH_FUNC_FORMAT = "%s(%s), ";
    final String ALIAS_FORMAT = "%s AS %s";

    private StringBuilder builder = new StringBuilder(RETURN);

    public void append(String tag, String func, String alias) throws CypherCompilerException {

        if (tag == null) {
            throw new CypherCompilerException("Tag cannot be null in a cypher return statement.");
        }
        if (func == null) {
            if (alias == null) {
                builder.append(String.format(TAG_ONLY_FORMAT, tag));
            } else {
                builder.append(String.format(ALIAS_FORMAT, tag, alias));
            }
        } else {
            if (alias == null) {
                builder.append(String.format(TAG_WITH_FUNC_FORMAT, tag, func));
            }else{
                builder.append(String.format(ALIAS_FORMAT, String.format(TAG_WITH_FUNC_FORMAT, tag, func), alias));
            }
        }
    }

    @Override
    public String toString() {
        String str = builder.toString();
        //remove last comma
        if(str.endsWith(",")) {
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }

}
