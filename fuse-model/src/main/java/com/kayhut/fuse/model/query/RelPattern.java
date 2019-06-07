package com.kayhut.fuse.model.query;

/*-
 * #%L
 * Rel.java - fuse-model - kayhut - 2,016
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


import com.fasterxml.jackson.annotation.JsonInclude;
import com.kayhut.fuse.model.Below;
import com.kayhut.fuse.model.Next;
import com.kayhut.fuse.model.Range;
import com.kayhut.fuse.model.query.entity.Typed;

import java.util.Objects;

/**
 * Created by lior.perry on 16-Feb-17.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class RelPattern extends Rel{
    private Range length;

    public RelPattern() {
    }

    public RelPattern(int eNum, String rType,Range length, Direction dir) {
        super(eNum, rType, dir, null, 0 );
        this.length = length;
    }
    public RelPattern(int eNum, String rType,Range length, Direction dir, String wrapper, int next) {
        super(eNum, rType, dir, wrapper, next);
        this.length = length;
    }

    public RelPattern(int eNum, String rType,Range length, Direction dir, String wrapper, int next, int b) {
        super(eNum, rType, dir, wrapper, next, b);
        this.length = length;
    }

    public void setLength(Range length) {
        this.length = length;
    }

    public Range getLength() {
        return length;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RelPattern)) return false;
        if (!super.equals(o)) return false;
        RelPattern that = (RelPattern) o;
        return Objects.equals(length, that.length);
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), length);
    }
}
