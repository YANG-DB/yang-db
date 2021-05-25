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
 * Attributes in the Property graph sense. They must be able to be extracted from the database data.
 *
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Attribute {

    /***
     * Table column from where it is extracted. The Table name is always the same as the node.
     */
    protected String columnName;

    /***
     * How this attribute will be referenced on Graph Query Languages (e.g. cypher).
     */
    protected String abstractionLevelName;

    /***
     * Which datatype represents this information. Currently supported are: Integer, Timestamp,
     * Date, String.
     */
    protected String dataType;

    /***
     * Default constructor.
     */
    public Attribute() {}

    /***
     * Attribute generator.
     * @param columnName name of the relational column that it refers to
     * @param abstractionLevelName name used for this attribute on the abstraction level
     * @param dataType relational datatype of this attribute
     */
    public Attribute(final String columnName, final String abstractionLevelName, final String dataType) {
        this.columnName = columnName;
        this.abstractionLevelName = abstractionLevelName;
        this.dataType = dataType;
    }

    /**
     * @return the columnName
     */
    public String getColumnName() {
        return columnName;
    }

    /**
     * @param columnName the columnName to set, in the format table.column
     */
    public void setColumnName(final String columnName) {
        this.columnName = columnName;
    }

    /**
     * @return the abstractionLevelName
     */
    public String getAbstractionLevelName() {
        return abstractionLevelName;
    }

    /**
     * @param fieldName the abstractionLevelName to set
     */
    public void setAbstractionLevelName(final String fieldName) {
        abstractionLevelName = fieldName;
    }

    /**
     * @return the dataType
     */
    public String getDataType() {
        return dataType;
    }

    /**
     * @param dataType the dataType to set
     */
    public void setDataType(final String dataType) {
        this.dataType = dataType;
    }

}
