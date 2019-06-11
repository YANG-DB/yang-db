package com.kayhut.fuse.model.query.entity;

/*-
 * #%L
 * ETyped.java - fuse-model - kayhut - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
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


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.kayhut.fuse.model.query.EBase;

import java.util.Objects;

/**
 * Created by lior.perry on 16-Feb-17.
 */

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class EndPattern<T extends EEntityBase> extends EEntityBase {
    private T endEntity;

    //region Constructors
    public EndPattern() {}

    public EndPattern(T endEntity) {
        super(endEntity.geteNum(),endEntity.geteTag(),endEntity.getNext(),endEntity.getB());
        this.endEntity = endEntity;
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

    @Override
    public EndPattern<T> clone() {
        return new EndPattern<>((T) getEndEntity().clone());
    }

    @Override
    public EndPattern<T > clone(int eNum) {
        return new EndPattern<>((T) getEndEntity().clone(eNum));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EndPattern)) return false;
        if (!super.equals(o)) return false;
        EndPattern<?> that = (EndPattern<?>) o;
        return Objects.equals(endEntity, that.endEntity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), endEntity);
    }
}
