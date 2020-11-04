package com.yangdb.fuse.model.ontology;

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
 * EPair.java - fuse-model - yangdb - 2,016
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
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;

import static com.yangdb.fuse.model.GlobalConstants.EdgeSchema.DEST_ID;
import static com.yangdb.fuse.model.GlobalConstants.EdgeSchema.SOURCE_ID;

/**
 * Epair represents the connection between two entities in the ontology
 *  - TypeA states the side-A entity type
 *  - sideAIdField states the side-A related (FK) field name as it appears in the connecting table
 *      (the actual field name on the Side-A entity if stated according to that entity's own fieldID - PK )
 *  - TypeB states the side-B entity type
 *  - sideBIdField states the side-B related(FK)  field name as it appears in the connecting table
 *      (the actual field name on the Side-B entity if stated according to that entity's own fieldID - PK )
 */

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class EPair {
    public EPair() {
    }

    public EPair(String eTypeA, String eTypeB) {
        this(String.format("%s->%s",eTypeA,eTypeB),eTypeA,eTypeB);
    }

    public EPair(String name, String eTypeA, String eTypeB) {
        this.eTypeA = eTypeA;
        this.eTypeB = eTypeB;
        this.name = name;
    }

    public EPair(String name, String eTypeA, String sideAIdField, String eTypeB, String sideBIdField) {
        this.name = name;
        this.eTypeA = eTypeA;
        this.sideAIdField = sideAIdField;
        this.eTypeB = eTypeB;
        this.sideBIdField = sideBIdField;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String geteTypeA() {
        return eTypeA;
    }

    public void seteTypeA(String eTypeA) {
        this.eTypeA = eTypeA;
    }

    public String geteTypeB() {
        return eTypeB;
    }

    public void seteTypeB(String eTypeB) {
        this.eTypeB = eTypeB;
    }

    public String getSideAIdField() {
        return sideAIdField;
    }

    public void setSideAIdField(String sideAIdField) {
        this.sideAIdField = sideAIdField;
    }

    public String getSideBIdField() {
        return sideBIdField;
    }

    public void setSideBIdField(String sideBIdField) {
        this.sideBIdField = sideBIdField;
    }

    @JsonIgnore
    public EPair withSideAIdField(String sideAIdField) {
        this.sideAIdField = sideAIdField;
        return this;
    }

    @JsonIgnore
    public EPair withSideBIdField(String sideBIdField) {
        this.sideBIdField = sideBIdField;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EPair ePair = (EPair) o;
        return
                Objects.equals(name, ePair.name) &&
                Objects.equals(eTypeA, ePair.eTypeA) &&
                Objects.equals(sideAIdField, ePair.sideAIdField) &
                        Objects.equals(eTypeB, ePair.eTypeB) &
                        Objects.equals(sideBIdField, ePair.sideBIdField);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name,eTypeA, sideAIdField, eTypeB, sideBIdField);
    }

    @Override
    public String toString() {
        return "EPair [name= " + name + ",eTypeA= " + eTypeA + ",sideAId= " + sideAIdField + ", eTypeB = " + eTypeB + ", sideAId = " + sideBIdField + "]";
    }

    //region Fields
    private String name;
    private String eTypeA;
    private String sideAIdField = SOURCE_ID;
    private String eTypeB;
    private String sideBIdField = DEST_ID;

    //endregion

    public static final class EPairBuilder {
        private String name;
        private String eTypeA;
        private String eTypeB;

        private EPairBuilder() {
        }

        public static EPairBuilder anEPair() {
            return new EPairBuilder();
        }

        public EPair with(String eTypeA, String eTypeB) {
            return new EPair(eTypeA, eTypeB);
        }

        public EPairBuilder withName(String name) {
            this.name = name;
            return this;
        }
        public EPairBuilder withETypeA(String eTypeA) {
            this.eTypeA = eTypeA;
            return this;
        }

        public EPairBuilder withETypeB(String eTypeB) {
            this.eTypeB = eTypeB;
            return this;
        }

        public EPair build() {
            if(name!=null)
                return new EPair(name,eTypeA,eTypeB);

            return new EPair(eTypeA,eTypeB);
        }


    }


}
