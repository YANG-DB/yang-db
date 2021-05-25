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

import java.util.List;

/***
 * This is used for multi-column restrictions. This clauses will be handled as ANDs.
 *
 *
 */
public class RestrictionClauses {

    /***
     * List of clause inside this restriction. The clause in this list should be applied as ORs.
     */
    private List<RestrictionClause> restrictionClause;

    /***
     * Default constructor.
     */
    public RestrictionClauses() {}

    /***
     * list of restriction clauses to be checked.
     * @param restrictionClause clause to be checked.
     */
    public RestrictionClauses(final List<RestrictionClause> restrictionClause) {
        super();
        this.restrictionClause = restrictionClause;
    }

    /**
     * @return the restrictionClause
     */
    public List<RestrictionClause> getRestrictionClause() {
        return restrictionClause;
    }

    /**
     * @param restrictionClause the restrictionClause to set
     */
    public void setRestrictionClause(final List<RestrictionClause> restrictionClause) {
        this.restrictionClause = restrictionClause;
    }
}
