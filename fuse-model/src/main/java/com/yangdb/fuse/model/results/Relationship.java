package com.yangdb.fuse.model.results;

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
 * Relationship.java - fuse-model - yangdb - 2,016
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


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.yangdb.fuse.model.logical.Edge;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by benishue on 21-Feb-17.
 */

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Relationship implements Edge , Comparable<Relationship>{
    //region Constructors
    public Relationship() {
        this.properties = Collections.emptyList();
        this.attachedProperties = Collections.emptyList();
    }
    //endregion

    //region Properties
    public String getrID() {
        return rID;
    }

    public void setrID(String rID) {
        this.rID = rID;
    }

    public boolean isAgg() {
        return agg;
    }

    public void setAgg(boolean agg) {
        this.agg = agg;
    }

    public String getrType() {
        return rType;
    }

    public void setrType(String rType) {
        this.rType = rType;
    }

    public boolean isDirectional() {
        return directional;
    }

    public void setDirectional(boolean directional) {
        this.directional = directional;
    }

    public String geteID1() {
        return eID1;
    }

    public void seteID1(String eID1) {
        this.eID1 = eID1;
    }

    public String geteTag1() {
        return eTag1;
    }

    public void seteTag1(String eTag1) {
        this.eTag1 = eTag1;
    }

    public String geteTag2() {
        return eTag2;
    }

    public void seteTag2(String eTag2) {
        this.eTag2 = eTag2;
    }

    public String geteID2() {
        return eID2;
    }

    public void seteID2(String eID2) {
        this.eID2 = eID2;
    }

    public List<Property> getProperties() {
        return properties;
    }

    public Optional<Property> getProperty(String key) {
        return getProperties().stream().filter(p->p.getpType().equals(key)).findAny();
    }

    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }

    public List<AttachedProperty> getAttachedProperties() {
        return attachedProperties;
    }

    public void setAttachedProperties(List<AttachedProperty> attachedProperties) {
        this.attachedProperties = attachedProperties;
    }
    //endregion

    @Override
    public int compareTo(Relationship relationship) {
        return this.getrID().compareTo(relationship.getrID());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Relationship that = (Relationship) o;
        return isAgg() == that.isAgg() &&
                isDirectional() == that.isDirectional() &&
                getrID().equals(that.getrID()) &&
                getrType().equals(that.getrType()) &&
                geteID1().equals(that.geteID1()) &&
                geteID2().equals(that.geteID2()) &&
                geteTag1().equals(that.geteTag1()) &&
                geteTag2().equals(that.geteTag2()) &&
                Objects.equals(getProperties(), that.getProperties()) &&
                Objects.equals(getAttachedProperties(), that.getAttachedProperties());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getrID(), isAgg(), getrType(), isDirectional(), geteID1(), geteID2(), geteTag1(), geteTag2(), getProperties(), getAttachedProperties());
    }

    //region Override Methods
    @Override
    public String toString()
    {
        return "Relationship [eID1 = "+eID1+", rType = "+rType+", attachedProperties = "+attachedProperties+", eID2 = "+eID2+", directional = "+directional+", agg = "+agg+", properties = "+properties+", rID = "+rID+"]";
    }
    //endregion

    //region Fields
    private String rID;
    private boolean agg;
    private String rType;
    private boolean directional;
    private String eID1;
    private String eID2;
    private String eTag1;
    private String eTag2;
    private List<Property> properties;
    private List<AttachedProperty> attachedProperties;

    @Override
    public String source() {
        return eID1;
    }

    @Override
    public String target() {
        return eID2;
    }

    @Override
    public String id() {
        return rID;
    }

    @Override
    public String label() {
        return rType;
    }

    @Override
    public Map<String,Object> metadata() {
        return getProperties().stream().collect(
                Collectors.toMap(Property::getpType, Property::getValue));
    }

    @Override
    public Map<String,Object> fields() {
        return getAttachedProperties().stream().collect(
                Collectors.toMap(AttachedProperty::getpName, AttachedProperty::getValue));

    }
    //endregion


    public static final class Builder {
        //region Constructors
        private Builder() {
            this.properties = Collections.emptyList();
            this.attachedProperties = Collections.emptyList();
        }
        //endregion

        //region Static
        public static Builder instance() {
            return new Builder();
        }
        //endregion

        //region Public Methods
        public Builder withRID(String rID) {
            this.rID = rID;
            return this;
        }

        public Builder withAgg(boolean agg) {
            this.agg = agg;
            return this;
        }

        public Builder withRType(String rType) {
            this.rType = rType;
            return this;
        }

        public Builder withDirectional(boolean directional) {
            this.directional = directional;
            return this;
        }

        public Builder withEID1(String eID1) {
            this.eID1 = eID1;
            return this;
        }

        public Builder withEID2(String eID2) {
            this.eID2 = eID2;
            return this;
        }

        public Builder withETag1(String eTag1) {
            this.eTag1 = eTag1;
            return this;
        }

        public Builder withETag2(String eTag2) {
            this.eTag2 = eTag2;
            return this;
        }

        public Builder withProperties(List<Property> properties) {
            this.properties = properties;
            return this;
        }

        public Builder withAttachedProperties(List<AttachedProperty> attachedProperties) {
            this.attachedProperties = attachedProperties;
            return this;
        }

        public Relationship build() {
            Relationship relationship = new Relationship();
            relationship.setAgg(agg);
            relationship.setDirectional(directional);
            relationship.setProperties(properties);
            relationship.setAttachedProperties(attachedProperties);
            relationship.eID1 = this.eID1;
            relationship.rID = this.rID;
            relationship.rType = this.rType;
            relationship.eID2 = this.eID2;
            relationship.eTag1 = this.eTag1;
            relationship.eTag2 = this.eTag2;
            return relationship;
        }
        //endregion

        //region Fields
        private String rID;
        private boolean agg;
        private String rType;
        private boolean directional;
        private String eID1;
        private String eID2;
        private String eTag1;
        private String eTag2;
        private List<Property> properties;
        private List<AttachedProperty> attachedProperties;
        //endregion
    }


}
