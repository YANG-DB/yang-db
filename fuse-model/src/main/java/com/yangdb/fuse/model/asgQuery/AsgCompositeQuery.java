package com.yangdb.fuse.model.asgQuery;

/*-
 *
 * fuse-model
 * %%
 * Copyright (C) 2016 - 2019 The Fuse Graph Database Project
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
 *
 */

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.*;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AsgCompositeQuery extends AsgQuery {
    public static final String INNER = "_Inner";
    private Set<AsgQuery> queryChain = new LinkedHashSet<>();

    public AsgCompositeQuery() {
    }

    public AsgCompositeQuery(AsgQuery asgQuery) {
        this.setName(asgQuery.getName());
        this.setOnt(asgQuery.getOnt());
        this.setOrigin(asgQuery.getOrigin());
        this.setParameters(asgQuery.getParameters());
        this.setStart(asgQuery.getStart());
        this.setElements(asgQuery.getElements());
    }

    public AsgCompositeQuery with(AsgQuery query) {
        queryChain.add(query);
        return this;
    }

    public List<AsgQuery> getQueryChain() {
        return new ArrayList<>(queryChain);
    }

    public static boolean isComposite(AsgQuery asgQuery) {
        return asgQuery instanceof AsgCompositeQuery;
    }

    public static boolean hasInnerQuery(AsgQuery asgQuery) {
        return isComposite(asgQuery) && !((AsgCompositeQuery) asgQuery).getQueryChain().isEmpty();
    }


}
