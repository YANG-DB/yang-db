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


import com.fasterxml.jackson.annotation.JsonInclude;
import com.yangdb.fuse.model.query.EBase;

/**
 * Created by lior.perry on 16-Feb-17.
 */

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ETyped extends EEntityBase implements Typed.eTyped {
    //region Constructors
    public ETyped() {}

    public ETyped(int eNum, String eTag, String eType, int next) {
        this(eNum,eTag,eType,next,-1);
    }

    public ETyped(int eNum, String eTag, String eType, int next, int b) {
        super(eNum, eTag, next, b);
        this.eType = eType;
    }

    public ETyped(EEntityBase base,String eType) {
        this(base.geteNum(),base.geteTag(),eType,base.getNext());
    }

    //endregion

    //region Properties
    public String geteType() {
        return eType;
    }

    public void seteType(String eType) {
        this.eType = eType;
    }
    //endregion

    //region Override Methods
    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) return false;
        ETyped eTyped = (ETyped) o;

        return eType.equals(eTyped.eType);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + eType.hashCode();
        return result;
    }

    @Override
    public String getTyped() {
        return geteType();
    }

    @Override
    public String[] getParentTyped() {
        return parentType;
    }

    public void setParentType(String[] parentType) {
        this.parentType = parentType;
    }

    @Override
    public EBase clone() {
        return clone(geteNum());
    }

    @Override
    public ETyped clone(int eNum) {
        return propClone(eNum, new ETyped());
    }

    protected ETyped propClone(int eNum, ETyped clone) {
        clone.seteNum(eNum);
        clone.seteTag(geteTag());
        clone.eType = eType;
        return clone;
    }
    //endregion

    //region Fields
    private String	eType;
    private String[]	parentType;
    //endregion
}
