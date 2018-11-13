package com.kayhut.fuse.model.results;

/*-
 * #%L
 * AttachedProperty.java - fuse-model - kayhut - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2016 - 2018 kayhut
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

/**
 * Created by benishue on 21-Feb-17.
 */

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AttachedProperty {

    //region Properties
    public void setPName (String pName)
    {
        this.pName = pName;
    }

    public Object getValue ()
    {
        return value;
    }

    public void setValue (Object value)
    {
        this.value = value;
    }

    public String getpName() {
        return pName;
    }

    public void setpName(String pName) {
        this.pName = pName;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
    //endregion

    //region Override Methods
    @Override
    public String toString()
    {
        return "AttachedProperty [pName = "+pName+", value = "+value+"]";
    }
    //endregion

    //region Fields
    private String pName;
    private Object value;
    private String tag;
    //endregion

    public static final class AttachedPropertyBuilder {
        private String pName;
        private Object value;
        private String tag;

        private AttachedPropertyBuilder() {
        }

        public static AttachedPropertyBuilder anAttachedProperty() {
            return new AttachedPropertyBuilder();
        }

        public AttachedPropertyBuilder withPName(String pName) {
            this.pName = pName;
            return this;
        }

        public AttachedPropertyBuilder withValue(Object value) {
            this.value = value;
            return this;
        }

        public AttachedPropertyBuilder withTag(String tag) {
            this.tag = tag;
            return this;
        }

        public AttachedProperty build() {
            AttachedProperty attachedProperty = new AttachedProperty();
            attachedProperty.setPName(pName);
            attachedProperty.setValue(value);
            attachedProperty.setTag(tag);
            return attachedProperty;
        }
    }


}
