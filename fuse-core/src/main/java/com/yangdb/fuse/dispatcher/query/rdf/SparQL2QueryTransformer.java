package com.yangdb.fuse.dispatcher.query.rdf;

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

import com.google.inject.Inject;
import com.yangdb.fuse.dispatcher.ontology.OntologyProvider;
import com.yangdb.fuse.dispatcher.query.QueryTransformer;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.query.Query;
import com.yangdb.fuse.model.query.QueryInfo;
import com.yangdb.fuse.model.resourceInfo.FuseError;

import java.util.Optional;

public class SparQL2QueryTransformer implements QueryTransformer<QueryInfo<String>, Query>  {

    private OntologyProvider ontologyProvider;

    @Inject
    public SparQL2QueryTransformer(OntologyProvider ontologyProvider) {
        this.ontologyProvider = ontologyProvider;
    }

    /**
     * transform Spqrql to V1 query
     * @param query
     * @return
     */
    public Query transform(QueryInfo<String> query) {
        Optional<Ontology> ontology =  ontologyProvider.get(query.getOntology());
        if(!ontology.isPresent())
            throw new FuseError.FuseErrorException(new FuseError("No ontology was found","Ontology not found "+query.getOntology()));

        return transform(ontology.get(),query.getQuery());
    }

    private Query transform(Ontology ontology, String query) {
        //todo
        return Query.Builder.instance().build();
    }


}
