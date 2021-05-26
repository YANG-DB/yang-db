package com.yangdb.fuse.generator.model.relation;

/*-
 * #%L
 * fuse-domain-dragons-datagen
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
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



import com.yangdb.fuse.generator.model.enums.RelationType;

import java.util.Date;

/**
 * Created by benishue on 05-Jun-17.
 */
public class MemberOf extends RelationBase {

    //region Ctrs
    public MemberOf(String id, String source, String target, Date since, Date till) {
        super(id, source, target, RelationType.MEMBER_OF);
        this.since = since;
        this.till = till;
    }
    //endregion

    //region Getters & Setters
    public Date getSince() {
        return since;
    }

    public void setSince(Date since) {
        this.since = since;
    }

    public Date getTill() {
        return till;
    }

    public void setTill(Date till) {
        this.till = till;
    }
    //endregion

    //region Public Methods
    @Override
    public String[] getRecord() {
        return new String[]{
                this.getId(),
                this.getSource(),
                "Person",// source entity type
                this.getTarget(),
                "Guild",// target entity type
                Long.toString(this.getSince().getTime()),
                Long.toString(this.getTill().getTime())
        };
    }
    //endregion

    //region Fields
    private Date since;
    private Date till;
    //endregion
}

