package com.yangdb.fuse.model.query;

/*-
 * #%L
 * fuse-model
 * %%
 * Copyright (C) 2016 - 2020 The YangDb Graph Database Project
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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class QueryInfo<Query> {
    private Query query;
    private String queryName;
    /**
     * todo replace with enum - see CreateQueryRequestMetadata.QueryLanguage
     */
    private String queryType;
    private String ontology;

    /**
     *
     * @param query - the actual quary
     * @param queryName - name
     * @param queryType - the query language type ( v1,cypher,sparql,graphql)
     * @param ontology - ontology name
     */
    public QueryInfo(Query query,String queryName,String queryType, String ontology) {
        this.query = query;
        this.queryName = queryName;
        this.queryType = queryType;
        this.ontology = ontology;
    }

    /**
     *  the query language type ( v1,cypher,sparql,graphql)
     * @return
     */
    public String getQueryType() {
        return queryType;
    }

    /**
     * query name
     * @return
     */
    public String getQueryName() {
        return queryName;
    }

    public Query getQuery() {
        return query;
    }

    public String getOntology() {
        return ontology;
    }
}
