package com.yangdb.fuse.model.execution.plan.entity;

/*-
 * #%L
 * EntityJoinOp.java - fuse-model - yangdb - 2,016
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

import com.yangdb.fuse.model.asgQuery.AsgEBase;
import com.yangdb.fuse.model.execution.plan.composite.Plan;
import javaslang.collection.Stream;

/**
 * Created by mordechaic on 11/14/2017.
 */
public class EntityJoinOp extends EntityOp {
    public EntityJoinOp() {
        super(new AsgEBase<>());
    }

    public EntityJoinOp(Plan leftBranch, Plan rightBranch) {
        // Find last EntityOp in the left hand branch. A Join assumes both branches end at an entity.
        EntityOp entityOp = (EntityOp) leftBranch.getOps().stream().filter(op -> EntityOp.class.isAssignableFrom(op.getClass())).reduce((a, b) -> b).get();
        this.setAsgEbase(entityOp.getAsgEbase());
        this.leftBranch = leftBranch;
        this.rightBranch = rightBranch;
    }

    public EntityJoinOp(Plan leftBranch, Plan rightBranch, boolean isComplete) {
        this(leftBranch, rightBranch);
        this.isComplete = isComplete;
    }

    public Plan getLeftBranch() {
        return leftBranch;
    }

    public Plan getRightBranch() {
        return rightBranch;
    }

    public boolean isComplete(){
        return this.isComplete;
    }

    public static boolean isComplete(EntityJoinOp entityJoinOp){
        EntityOp entityOp = Stream.ofAll(entityJoinOp.getRightBranch().getOps()).filter(op -> EntityOp.class.isAssignableFrom(op.getClass())).map(op -> (EntityOp)op).last();
        return entityOp.getAsgEbase().geteNum() == entityJoinOp.getAsgEbase().geteNum();
    }

    public void setComplete(boolean complete) {
        isComplete = complete;
    }

    private Plan leftBranch;
    private Plan rightBranch;

    private boolean isComplete = false;
}
