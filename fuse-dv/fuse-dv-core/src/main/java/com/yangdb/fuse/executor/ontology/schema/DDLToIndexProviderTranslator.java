package com.yangdb.fuse.executor.ontology.schema;

/*-
 * #%L
 * fuse-dv-core
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

import com.google.common.collect.ImmutableList;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import com.yangdb.fuse.model.schema.*;
import org.jooq.Parser;
import org.jooq.Queries;
import org.jooq.Query;
import org.jooq.Table;
import org.jooq.impl.CreateTableStatement;
import org.jooq.impl.DefaultConfiguration;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.yangdb.fuse.executor.elasticsearch.MappingIndexType.STATIC;
import static com.yangdb.fuse.executor.elasticsearch.MappingIndexType.UNIFIED;
import static org.jooq.impl.ConstraintStatement.foreignKey;
import static org.jooq.impl.DSL.using;

public class DDLToIndexProviderTranslator implements IndexProviderTranslator<List<String>> {
    private Parser parser;

    @Override
    public IndexProvider translate(String ontology, List<String> source) {
        IndexProvider indexProvider = IndexProvider.Builder.generate(ontology);
        parser = using(new DefaultConfiguration()).parser();
        source.forEach(s -> parseTable(s, indexProvider));
        return indexProvider;
    }

    private void parseTable(String table, IndexProvider indexProvider) throws FuseError.FuseErrorException {
        try {
            Queries queries = parser.parse(table);
            Arrays.stream(queries.queries())
                    .filter(q -> q.getClass().getSimpleName().endsWith("CreateTableImpl"))
                    .forEach(q -> parse(q, indexProvider));
        } catch (Throwable t) {
            throw new FuseError.FuseErrorException("Error Parsing DDL file " + table, t);
        }
    }

    private void parse(Query createTable, IndexProvider indexProvider) {
        CreateTableStatement statement = new CreateTableStatement(createTable);
        //get table entity
        Table<?> table = statement.getTable();

        //build ontology entity
        String tableName = table.getName().toLowerCase();
        indexProvider.withEntity(new Entity(tableName, STATIC.name(), "Index",
                new Props(ImmutableList.of(tableName)), Collections.emptyList(), Collections.emptyMap()));

        //build relations
        foreignKey(statement.getConstraints())
                .forEach(fk ->
                        indexProvider.withRelation(
                                new Relation(fk.getName().toLowerCase(),
                                        UNIFIED.name(),
                                        "Index",
                                        false,
                                        Collections.emptyList(),
                                        new Props(ImmutableList.of(tableName)),
                                        Collections.emptyList(),
                                        Collections.emptyMap())));
    }

}
