package com.yangdb.fuse.assembly.knowledge.asg;

/*-
 * #%L
 * fuse-domain-knowledge-ext
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
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



import com.yangdb.fuse.assembly.knowledge.parser.JsonQueryTranslator;
import com.yangdb.fuse.assembly.knowledge.parser.model.BusinessTypesProvider;
import com.yangdb.fuse.dispatcher.query.QueryTransformer;
import com.yangdb.fuse.model.query.Query;
import com.yangdb.fuse.model.query.QueryInfo;

import java.io.IOException;
import java.util.function.Function;

public class AsgClauseTransformer implements QueryTransformer<String, Query> , Function<QueryInfo<String>,Boolean> {

    public static final String CLAUSE_QUERY_LANGUAGE = "clause";

    //region Constructors
    public AsgClauseTransformer(JsonQueryTranslator translator,BusinessTypesProvider typesProvider) {
        this.translator = translator;
        this.typesProvider = typesProvider;
    }
    //endregion

    //region QueryTransformer Implementation
    @Override
    public Query transform(String query) {

        try {
            return translator.translate(query,typesProvider);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }
    //endregion
    @Override
    public Boolean apply(QueryInfo<String> queryInfo) {
        return CLAUSE_QUERY_LANGUAGE.equalsIgnoreCase(queryInfo.getQueryType());
    }

    //region Fields
    private JsonQueryTranslator translator;
    private BusinessTypesProvider typesProvider;
    //endregion
}
