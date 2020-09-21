package com.yangdb.fuse.dispatcher.query.sql;

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

import com.google.inject.Inject;
import com.yangdb.fuse.dispatcher.ontology.OntologyTransformerIfc;
import com.yangdb.fuse.model.ontology.Ontology;

import java.util.Collections;
import java.util.List;
import static org.jooq.impl.DSL.*;
import org.jooq.*;
import org.jooq.impl.*;
/**
 * convert DDL (SQL Definition Language) structure into V1 ontology
 */
public class DDL2OntologyTransformer implements OntologyTransformerIfc<List<String>, Ontology> {
    private DefaultDSLContext context;

    @Inject
    public DDL2OntologyTransformer() {}

    @Override
    public Ontology transform(List<String> source) {
        return Ontology.OntologyBuilder.anOntology().build();
    }

    @Override
    public List<String> translate(Ontology source) {
        return Collections.emptyList();
    }

    private void init() {
        context = new DefaultDSLContext(SQLDialect.DEFAULT);
    }
}
