package org.jooq.impl;

import org.jooq.Constraint;
import org.jooq.Field;

public class ConstraintStatement {
    private Field<?>[] foreignKey;
    private Field<?>[] primaryKey;

    public ConstraintStatement(Constraint constraint) {
        assert ConstraintImpl.class.isAssignableFrom(constraint.getClass());
        ConstraintImpl constraintImpl = (ConstraintImpl) constraint;
        foreignKey = constraintImpl.$foreignKey();
        primaryKey = constraintImpl.$primaryKey();
    }

    public Field<?>[] getForeignKey() {
        return foreignKey;
    }

    public Field<?>[] getPrimaryKey() {
        return primaryKey;
    }
}
