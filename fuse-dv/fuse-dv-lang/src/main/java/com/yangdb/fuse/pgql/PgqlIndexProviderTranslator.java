package com.yangdb.fuse.pgql;

/*-
 * #%L
 * fuse-dv-lang
 * %%
 * Copyright (C) 2016 - 2021 The YangDb Graph Database Project
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

import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.schema.IndexProvider;
import com.yangdb.fuse.model.schema.IndexProviderTranslator;

import javax.inject.Inject;

/**
 * translates the PGQL DDL statement into an index provider low level schema used to generate the E/S mapping & indices
 */
public class PgqlIndexProviderTranslator implements IndexProviderTranslator<String> {
    private PgqlOntologyParser ontologyParser;

    @Inject
    public PgqlIndexProviderTranslator(PgqlOntologyParser ontologyParser) {
        this.ontologyParser = ontologyParser;
    }

    @Override
    public IndexProvider translate(String ontology, String statement) {
        Ontology transform = ontologyParser.transform(ontology, statement);
        return IndexProvider.Builder.generate(transform);
    }
}
