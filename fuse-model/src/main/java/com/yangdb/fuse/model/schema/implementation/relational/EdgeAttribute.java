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

import com.fasterxml.jackson.annotation.JsonInclude;

/***
 * Attribute that an edge may contain, in relational implementation level.
 *
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EdgeAttribute extends Attribute {

    /**
     * Name of the table where this attribute is found. Can be in the source, destination or any intermediate join table.
     */
    private String tableName;

    /***
     * Default Constructor.
     */
    public EdgeAttribute() {}

    /***
     * Edge Attribute generator.
     * @param tableName name of the table where the edge attribute can be found
     */
    public EdgeAttribute(final String tableName) {
        super();
        this.tableName = tableName;
    }

    /***
     * Edge Attribute generator.
     * @param columnName name of the column where the attribute is found
     * @param abstractionLevelName name used to refer this attribute on the abstraction level
     * @param dataType relational datatype of this attribute
     * @param tableName name of the table from where this attribute comes from.
     */
    public EdgeAttribute(final String columnName, final String abstractionLevelName, final String dataType,
            final String tableName) {
        super(columnName, abstractionLevelName, dataType);
        this.tableName = tableName;
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
}
