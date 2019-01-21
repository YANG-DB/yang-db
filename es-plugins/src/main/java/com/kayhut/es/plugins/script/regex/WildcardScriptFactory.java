package com.kayhut.es.plugins.script.regex;

/*-
 * #%L
 * es-plugin
 * %%
 * Copyright (C) 2016 - 2018 kayhut
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.apache.lucene.queryparser.flexible.standard.parser.EscapeQuerySyntaxImpl;
//import org.elasticsearch.script.ExecutableScript;
//import org.elasticsearch.script.NativeScriptFactory;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by Roman on 5/26/2018.
 */
public class WildcardScriptFactory /*implements NativeScriptFactory*/ {
    //region Constructors
    public WildcardScriptFactory() {
        this.maxPatternCacheSize = 10000;
        this.patternCache = Collections.synchronizedMap(new HashMap<>(this.maxPatternCacheSize));
    }
    //endregion

    //region NativeScriptFactory Implementation
/*
    @Override
    public ExecutableScript newScript(Map<String, Object> params) {
        String field = params == null ? null : (String)params.get("field");
        if (field == null) {
            throw new IllegalArgumentException("Missing the field parameter");
        }

        String expression = (String)params.get("expression");
        if (expression == null) {
            throw new IllegalArgumentException("Missing the expression parameter");
        }

        if (this.patternCache.size() > this.maxPatternCacheSize) {
            this.patternCache.clear();
        }

        Pattern regexPattern = this.patternCache.computeIfAbsent(expression, this::compileWildcardExpression);
        return new RegexScript(field, regexPattern);
    }
*/

//    @Override
    public boolean needsScores() {
        return false;
    }

//    @Override
    public String getName() {
        return "wildcard";
    }
    //endregion

    //region Private Methods


    private Pattern compileWildcardExpression(String wildcardExpression) {
        return Pattern.compile(RegexUtil.wildcardToRegex(wildcardExpression));
    }
    //endregion

    //region Fields
    private Map<String, Pattern> patternCache;
    private int maxPatternCacheSize;
    //endregion
}
