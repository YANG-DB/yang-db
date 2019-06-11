package com.kayhut.fuse.model.ontology;

/*-
 * #%L
 * EPair.java - fuse-model - kayhut - 2,016
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

/**
 * Created by benishue on 22-Feb-17.
 */

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class EPair {
    public EPair() {
    }

    public EPair(String eTypeA, String eTypeB) {
        this.eTypeA = eTypeA;
        this.eTypeB = eTypeB;
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

    @Override
    public String toString()
    {
        return "EPair [eTypeB = "+eTypeB+", eTypeA = "+eTypeA+"]";
    }

    //region Fields
    private String eTypeA;
    private String eTypeB;
    //endregion

    public static final class EPairBuilder {
        private String eTypeA;
        private String eTypeB;

        private EPairBuilder() {
        }

        public static EPairBuilder anEPair() {
            return new EPairBuilder();
        }

        public EPair with(String eTypeA,String eTypeB) {
            return new EPair(eTypeA,eTypeB);
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
            EPair ePair = new EPair();
            ePair.eTypeA = this.eTypeA;
            ePair.eTypeB = this.eTypeB;
            return ePair;
        }


    }


}
