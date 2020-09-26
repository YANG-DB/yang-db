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

import org.jooq.Constraint;
import org.jooq.Field;
import org.jooq.Name;
import org.jooq.Table;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * jooq accessable wrapper for parsing DDL queries fragments
 */
public class ConstraintStatement implements Constraint {
    private Field<?>[] foreignKey;
    private Field<?>[] primaryKey;
    private ConstraintImpl constraintImpl;
    private Table<?> $referencesTable;
    private Field<?>[] references;

    public ConstraintStatement(Constraint constraint) {
        assert ConstraintImpl.class.isAssignableFrom(constraint.getClass());
        constraintImpl = (ConstraintImpl) constraint;
        foreignKey = constraintImpl.$foreignKey();
        primaryKey = constraintImpl.$primaryKey();
        $referencesTable = constraintImpl.$referencesTable();
        references = constraintImpl.$references();
    }

    public Field<?>[] getReferences() {
        return Objects.isNull(references) ? new Field[]{} : references;
    }

    public Table<?> get$referencesTable() {
        return $referencesTable;
    }

    public Field<?>[] getForeignKey() {
        return Objects.isNull(foreignKey) ? new Field[]{} : foreignKey;
    }

    public Field<?>[] getPrimaryKey() {
        return Objects.isNull(primaryKey) ? new Field[]{} : primaryKey;
    }

    @Override
    public String getName() {
        return constraintImpl.getName();
    }

    @Override
    public Name getQualifiedName() {
        return constraintImpl.getQualifiedName();
    }

    @Override
    public Name getUnqualifiedName() {
        return constraintImpl.getUnqualifiedName();
    }

    @Override
    public String getComment() {
        return constraintImpl.getComment();
    }


    public static List<ConstraintStatement> foreignKey(List<ConstraintStatement> constraints) {
        return constraints.stream().noneMatch(c -> c.getForeignKey().length > 0) ?
                Collections.emptyList() : constraints.stream().filter(c -> c.getForeignKey().length > 0).collect(Collectors.toList());
    }

    public static List<ConstraintStatement> primaryKey(List<ConstraintStatement> constraints) {
        return constraints.stream().noneMatch(c -> c.getPrimaryKey().length > 0) ?
                Collections.emptyList() : constraints.stream().filter(c -> c.getPrimaryKey().length > 0).collect(Collectors.toList());
    }
}
