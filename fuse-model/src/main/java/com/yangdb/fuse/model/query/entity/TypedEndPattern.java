package com.yangdb.fuse.model.query.entity;

/*-
 * #%L
 * fuse-model
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

/*-
 *
 * ETyped.java - fuse-model - yangdb - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
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


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.yangdb.fuse.model.query.properties.EProp;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by lior.perry on 16-Feb-17.
 */

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class TypedEndPattern<T extends ETyped> extends ETyped implements EndPattern<T> {
    private List<EProp> filter = new ArrayList<>();
    private T endEntity;

    //region Constructors
    public TypedEndPattern() {
    }

    public TypedEndPattern(T endEntity) {
        this(endEntity, new ArrayList<>());
    }

    public TypedEndPattern(T endEntity, List<EProp> filter) {
        super(endEntity.geteNum(), endEntity.geteTag(), endEntity.geteType(), endEntity.getNext(), endEntity.getB());
        this.endEntity = endEntity;
        this.filter = filter;
    }

    public T getEndEntity() {
        return endEntity;
    }

    @JsonIgnore
    public void seteTag(String eTag) {
        this.endEntity.seteTag(eTag);
    }

    @Override
    @JsonIgnore
    public String geteTag() {
        return this.endEntity.geteTag();
    }

    public List<EProp> getFilter() {
        return filter;
    }

    @Override
    public void seteType(String eType) {
        super.seteType(eType);
        if(Objects.nonNull(endEntity))
            endEntity.seteType(eType);
    }

    @Override
    public void setParentType(String[] parentType) {
        super.setParentType(parentType);
        if(Objects.nonNull(endEntity))
            endEntity.setParentType(parentType);
    }

    @Override
    public TypedEndPattern<T> clone() {
        return new TypedEndPattern<>((T) getEndEntity().clone(), getFilter());
    }

    @Override
    public TypedEndPattern<T> clone(int eNum) {
        return new TypedEndPattern<>((T) getEndEntity().clone(eNum), getFilter());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TypedEndPattern)) return false;
        if (!super.equals(o)) return false;
        TypedEndPattern<?> that = (TypedEndPattern<?>) o;
        return Objects.equals(endEntity, that.endEntity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), endEntity);
    }
}
