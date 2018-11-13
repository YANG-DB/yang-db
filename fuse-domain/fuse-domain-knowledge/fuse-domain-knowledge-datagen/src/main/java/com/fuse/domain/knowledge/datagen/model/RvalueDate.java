package com.fuse.domain.knowledge.datagen.model;

/*-
 * #%L
 * fuse-domain-knowledge-datagen
 * %%
 * Copyright (C) 2016 - 2018 kayhut
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

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class RvalueDate extends RvalueBase {
    //region Constructors
    public RvalueDate() {
        super();
    }

    public RvalueDate(String relationId, String context, String fieldId, Date dateValue) {
        this(relationId, context, fieldId, dateValue, null);
    }

    public RvalueDate(String relationId, String context, String fieldId, Date dateValue, KnowledgeEntityBase.Metadata metadata) {
        super(relationId, context, fieldId, metadata);
        this.dateValue = dateValue;
    }
    //endregion

    //region Properties
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone="GMT")
    public Date getDateValue() {
        return dateValue;
    }

    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone="GMT")
    public void setDateValue(Date dateValue) {
        this.dateValue = dateValue;
    }
    //endregion

    //region Fields
    private Date dateValue;
    //endregion
}
