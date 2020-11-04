package org.jooq.impl;

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

import org.jooq.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * jooq accessable wrapper for parsing DDL queries fragments
 */

public final class CreateTableStatement {
    private List<Field<?>> fields;
    private List<DataType<?>> dataTypes;
    private List<Constraint> constraints;
    private Table<?> table;

    public CreateTableStatement(Query query) {
        assert CreateTableImpl.class.isAssignableFrom(query.getClass());
        //extract fields
        CreateTableImpl createTable = (CreateTableImpl) query;
        fields = createTable.$columnFields();
        dataTypes = createTable.$columnTypes();
        constraints = createTable.$constraints();
        table = createTable.$table();
    }

    public List<Field<?>> getFields() {
        return fields;
    }

    public List<DataType<?>> getDataTypes() {
        return dataTypes;
    }

    public List<ConstraintStatement> getConstraints() {
        return constraints.stream().map(ConstraintStatement::new).collect(Collectors.toList());
    }

    public Table<?> getTable() {
        return table;
    }
}
