package org.jooq.impl;

import org.jooq.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
