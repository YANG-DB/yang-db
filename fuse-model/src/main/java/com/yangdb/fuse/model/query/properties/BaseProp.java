package com.yangdb.fuse.model.query.properties;

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
 * BaseProp.java - fuse-model - yangdb - 2,016
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
import com.yangdb.fuse.model.query.EBase;
import com.yangdb.fuse.model.query.properties.constraint.Constraint;
import com.yangdb.fuse.model.query.properties.projection.Projection;

/**
 * Created by moti on 5/17/2017.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public abstract class BaseProp extends EBase{
    //region Consructors
    public BaseProp() {

    }

    public BaseProp(int eNum, String pType, Constraint con) {
        super(eNum);
        this.pType = pType;
        this.con = con;
    }

    public BaseProp(int eNum, String pType, Projection proj) {
        super(eNum);
        this.pType = pType;
        this.proj = proj;
    }
    //endregion


    //region Override Methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        BaseProp eProp = (BaseProp) o;

        if (pType == null) {
            if (eProp.pType != null) {
                return false;
            }
        } else {
            if (!pType.equals(eProp.pType)) {
                return false;
            }
        }

        if (pTag == null) {
            if (eProp.pTag != null) {
                return false;
            }
        } else {
            if (!pTag.equals(eProp.pTag)) {
                return false;
            }
        }

        if (con == null) {
            if (eProp.con != null) {
                return false;
            }
        } else {
            if (!con.equals(eProp.con)) {
                return false;
            }
        }

        return f != null ? f.equals(eProp.f) : eProp.f == null;
    }

    @Override
    public abstract EBase clone();

    @Override
    public int hashCode() {
        int result = super.hashCode();

        result = 31 * result + (pType!=null ? pType.hashCode() : 0);
        result = 31 * result + (pTag!=null ? pTag.hashCode() : 0);
        result = 31 * result + (con!=null ? con.hashCode() : 0);
        result = 31 * result + (proj!=null ? proj.hashCode() : 0);
        result = 31 * result + (f != null ? f.hashCode() : 0);
        return result;
    }
    //endregion

    //region Properties
    public String getpType() {
        return pType;
    }

    public void setpType(String pType) {
        this.pType = pType;
    }

    public String getpTag() {
        return pTag;
    }

    public void setpTag(String pTag) {
        this.pTag = pTag;
    }

    public Constraint getCon() {
        return con;
    }

    /**
     * set constraint (projection & constraints are exclusives)
     * @param con
     */
    public void setCon(Constraint con) {
        this.con = con;
        this.proj = null;
    }

    public Projection getProj() {
        return proj;
    }

    /**
     * state is this property constraint an aggregation
     * @return
     */
    @JsonIgnore
    public boolean isAggregation() {
        return getCon()!=null && getCon().getCountOp()!=null;
    }
    /**
     * set projection (projection & constraints are exclusives)
     * @param proj
     */
    public void setProj(Projection proj) {
        this.proj = proj;
        this.con = null;
    }

    public String getF() {
        return f;
    }

    public void setF(String f) {
        this.f = f;
    }

    public boolean isProjection() {
        return getProj()!=null;
    }

    public boolean isConstraint() {
        return getCon()!=null;
    }
    //endregion

    //region Fields
    private String pType;
    private String pTag;
    private Constraint con;
    private Projection proj;
    private String f;
    //endregion
}
