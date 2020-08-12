package com.yangdb.fuse.dispatcher.query.rdf;

/*-
 * #%L
 * fuse-core
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

import com.yangdb.fuse.dispatcher.ontology.OntologyTransformerIfc;
import com.yangdb.fuse.model.ontology.Ontology;
import org.apache.commons.lang.NotImplementedException;

/**
 * transform Json-LD RDF ontology schema to YangDB ontology support
 */
public class JsonLD2OntologyTransformer implements OntologyTransformerIfc<String, Ontology> {
    @Override
    public Ontology transform(String source) {
        throw new NotImplementedException("");
    }

    @Override
    public String translate(Ontology source) {
        throw new NotImplementedException("");
    }
}
