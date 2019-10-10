package com.yangdb.fuse.unipop.schemaProviders;

/*-
 *
 * fuse-dv-unipop
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
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

/*public class PrefixedIdEdgeRedundancy extends PrefixedEdgeRedundancy {
    //region Constructor
    public PrefixedIdEdgeRedundancy(GraphEdgeSchema.End end, String prefix) {
        super(prefix);
        this.end = end;
    }
    //endregion

    //region PrefixedEdgeRedundancy Implementation
    @Override
    public Optional<String> getRedundantPropertyName(String propertyName) {
        if (propertyName.equals(T.id.getAccessor())) {
            return Optional.of(this.end.getIdFields());
        } else {
            return Optional.of(this.getPrefix() + propertyName);
        }
    }
    //endregion

    //region Fields
    GraphEdgeSchema.End end;
    //endregion
}*/
