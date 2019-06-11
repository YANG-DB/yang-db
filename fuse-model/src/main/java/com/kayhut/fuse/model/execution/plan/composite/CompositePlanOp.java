package com.kayhut.fuse.model.execution.plan.composite;

/*-
 * #%L
 * CompositePlanOp.java - fuse-model - kayhut - 2,016
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

import com.kayhut.fuse.model.execution.plan.PlanOp;
import com.kayhut.fuse.model.execution.plan.composite.descriptors.CompositePlanOpDescriptor;
import com.kayhut.fuse.model.execution.plan.composite.descriptors.IterablePlanOpDescriptor;
import javaslang.collection.Stream;

import java.util.Collections;
import java.util.List;

/**
 * Created by Roman on 24/04/2017.
 */
public abstract class CompositePlanOp extends PlanOp implements Cloneable {

    //region Empty
    public static class Empty extends CompositePlanOp {

        //region CompositePlanOp Implementation
        @Override
        public List<PlanOp> getOps() {
            return Collections.emptyList();
        }
        //endregion
    }

    public static Empty empty() {
        return empty;
    }

    private static Empty empty = new Empty();
    //endregion

    //region Constructors
    private CompositePlanOp() {}

    public CompositePlanOp(Iterable<PlanOp> ops) {
        this.ops = Stream.ofAll(ops).toJavaList();
    }

    public CompositePlanOp(PlanOp...ops) {
        this(Stream.of(ops));
    }
    //endregion

    //region Public Methods
    public <T extends CompositePlanOp> T  withOp(PlanOp op) {
        try {
            CompositePlanOp clone = (CompositePlanOp)clone();
            clone.getOps().add(op);
            return (T)clone;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public <T extends CompositePlanOp> T withoutOp(PlanOp op) {
        try {
            CompositePlanOp clone = (CompositePlanOp)clone();
            clone.getOps().remove(op);
            return (T)clone;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public <T extends CompositePlanOp> T  append(CompositePlanOp compositePlanOp) {
        try {
            CompositePlanOp clone = (CompositePlanOp)clone();
            clone.getOps().addAll(compositePlanOp.getOps());
            return (T)clone;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public <T extends CompositePlanOp> T  appendAll(List<T> compositePlanOps) {
        try {
            CompositePlanOp clone = (CompositePlanOp)clone();
            compositePlanOps.forEach(plan -> clone.getOps().addAll(plan.getOps()));
            return (T)clone;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }

    public <T extends CompositePlanOp> T from(PlanOp fromOp) {
        return fromTo(this.getOps().indexOf(fromOp), this.getOps().size());
    }

    public <T extends CompositePlanOp> T to(PlanOp toOp) {
        return fromTo(0, this.getOps().indexOf(toOp));
    }

    public <T extends CompositePlanOp> T fromTo(PlanOp fromOp, PlanOp toOp) {
        return fromTo(this.getOps().indexOf(fromOp), this.getOps().indexOf(toOp));
    }

    public <T extends CompositePlanOp> T fromTo(int indexFrom, int indexTo) {
        if (indexFrom < 0 || indexTo < 0) {
            return (T)empty();
        }

        try {
            CompositePlanOp clone = (CompositePlanOp)super.clone();
            clone.ops = Stream.ofAll(this.getOps()).drop(indexFrom).take(indexTo - indexFrom).toJavaList();
            return (T)clone;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }
    //endregion

    //region Properties
    public List<PlanOp> getOps() {
        return this.ops;
    }
    //endregion

    //region Override Methods
    @Override
    public String toString() {
        return new CompositePlanOpDescriptor(IterablePlanOpDescriptor.getFull()).describe(this);
    }

    public Object clone()throws CloneNotSupportedException{
        CompositePlanOp clone = (CompositePlanOp)super.clone();
        clone.ops = Stream.ofAll(this.getOps()).toJavaList();
        return clone;
    }
    //endregion

    //region Fields
    private List<PlanOp> ops;
    //endregion
}
