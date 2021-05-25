package com.yangdb.fuse.model.schema.implementation.relational;

/*-
 * #%L
 * fuse-model
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

/***
 * This is used for multi-column restrictions. This clauses will be handled as <b>ORs</b>
 *
 *
 */
public class RestrictionClause {
    /***
     * Name of the table where the restriction is going to be applied.
     */
    private String tableName;

    /***
     * Name of the table's column where the restriction is going to be applied.
     */
    private String columnName;

    /**
     * SQL92 Regex that will be applied on that column.
     */
    private String pattern;

    /***
     * Default constructor.
     */
    public RestrictionClause() {}

    /***
     * Generates a restriction clause.
     * @param tableName table name to be restricted
     * @param columnName column name to be restricted
     * @param pattern pattern to restrict on
     */
    public RestrictionClause(final String tableName, final String columnName, final String pattern) {
        super();
        this.tableName = tableName;
        this.columnName = columnName;
        this.pattern = pattern;
    }

    /**
     * @return the tableName
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * @param tableName the tableName to set
     */
    public void setTableName(final String tableName) {
        this.tableName = tableName;
    }

    /**
     * @return the columnName
     */
    public String getColumnName() {
        return columnName;
    }

    /**
     * @param columnName the columnName to set
     */
    public void setColumnName(final String columnName) {
        this.columnName = columnName;
    }

    /**
     * @return the pattern
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * @param pattern the pattern to set
     */
    public void setPattern(final String pattern) {
        this.pattern = pattern;
    }
}
