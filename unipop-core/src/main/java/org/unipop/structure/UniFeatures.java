package org.unipop.structure;

/*-
 * #%L
 * UniFeatures.java - unipop-core - kayhut - 2,016
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

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.util.StringFactory;

public class UniFeatures implements Graph.Features {

    ElasticVertexFeatures elasticVertexFeatures = new ElasticVertexFeatures();
    ElasticEdgeFeatures elasticEdgeFeatures = new ElasticEdgeFeatures();
    ElasticGraphFeatures elasticGraphFeatures = new ElasticGraphFeatures();

    @Override
    public String toString() {
        return StringFactory.featureString(this);
    }

    @Override
    public VertexFeatures vertex() {
        return elasticVertexFeatures;
    }

    @Override
    public EdgeFeatures edge() {
        return elasticEdgeFeatures;
    }

    @Override
    public GraphFeatures graph() {
        return elasticGraphFeatures;
    }

    private class ElasticGraphFeatures implements GraphFeatures {
        ElasticVariableFeatures elasticVariableFeatures = new ElasticVariableFeatures();

        @Override
        public VariableFeatures variables() {
            return elasticVariableFeatures;
        }

        @Override
        public boolean supportsComputer() {
            return false;
        }

        @Override
        public boolean supportsTransactions() {
            return false;
        }

        @Override
        public boolean supportsThreadedTransactions() {
            return false;
        }
    }

    private class ElasticVariableFeatures implements VariableFeatures {
        @Override
        public boolean supportsVariables() {
            return false;
        }

        @Override
        public boolean supportsBooleanValues() {
            return false;
        }

        @Override
        public boolean supportsByteValues() {
            return false;
        }

        @Override
        public boolean supportsDoubleValues() {
            return false;
        }

        @Override
        public boolean supportsFloatValues() {
            return false;
        }

        @Override
        public boolean supportsIntegerValues() {
            return false;
        }

        @Override
        public boolean supportsLongValues() {
            return false;
        }

        @Override
        public boolean supportsMapValues() {
            return false;
        }

        @Override
        public boolean supportsMixedListValues() {
            return false;
        }

        @Override
        public boolean supportsBooleanArrayValues() {
            return false;
        }

        @Override
        public boolean supportsByteArrayValues() {
            return false;
        }

        @Override
        public boolean supportsDoubleArrayValues() {
            return false;
        }

        @Override
        public boolean supportsFloatArrayValues() {
            return false;
        }

        @Override
        public boolean supportsIntegerArrayValues() {
            return false;
        }

        @Override
        public boolean supportsStringArrayValues() {
            return false;
        }

        @Override
        public boolean supportsLongArrayValues() {
            return false;
        }

        @Override
        public boolean supportsSerializableValues() {
            return false;
        }

        @Override
        public boolean supportsStringValues() {
            return false;
        }

        @Override
        public boolean supportsUniformListValues() {
            return false;
        }
    }

    private class ElasticEdgeFeatures implements EdgeFeatures {
        @Override
        public boolean supportsNumericIds() {
            return false;
        }

        @Override
        public boolean supportsAnyIds() {
            return false;
        }

        @Override
        public boolean supportsUuidIds() {
            return false;
        }

        @Override
        public boolean supportsCustomIds() {
            return false;
        }

        @Override
        public boolean willAllowId(Object id) {
            return true;
        }
    }

    private class ElasticVertexFeatures implements VertexFeatures {
        ElasticVertexPropertyFeatures elasticVertexPropertyFeatures = new ElasticVertexPropertyFeatures();

        @Override
        public boolean supportsMultiProperties() {
            return true;
        }

        @Override
        public boolean supportsNumericIds() {
            return false;
        }

        @Override
        public boolean supportsUuidIds() {
            return false;
        }

        @Override
        public boolean supportsCustomIds() {
            return false;
        }

        @Override
        public boolean supportsAnyIds() {
            return false;
        }

        @Override
        public boolean supportsMetaProperties() {
            return false;
        }

        @Override
        public VertexPropertyFeatures properties() {
            return elasticVertexPropertyFeatures;
        }

        @Override
        public boolean willAllowId(Object id) {
            return true;
        }
    }

    private class ElasticVertexPropertyFeatures implements VertexPropertyFeatures {
        @Override
        public boolean supportsUserSuppliedIds() {
            return false;
        }

        @Override
        public boolean supportsNumericIds() {
            return true;
        }

        @Override
        public boolean supportsStringIds() {
            return false;
        }

        @Override
        public boolean supportsUuidIds() {
            return false;
        }

        @Override
        public boolean supportsCustomIds() {
            return false;
        }

        @Override
        public boolean supportsAnyIds() {
            return false;
        }
    }
}
