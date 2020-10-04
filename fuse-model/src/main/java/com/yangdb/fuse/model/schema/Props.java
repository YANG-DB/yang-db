package com.yangdb.fuse.model.schema;

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

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "values"
})
public class Props {


    @JsonProperty("values")
    private List<String> values = null;
    @JsonProperty("partition.field")
    private String partitionField;
    @JsonProperty("prefix")
    private String prefix;
    @JsonProperty("index.format")
    private String indexFormat;
    @JsonProperty("date.format")
    private String dateFormat;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public Props() {}

    public Props(List<String> values) {
        this.values = values;
    }

    public Props(List<String> values, String partitionField, String prefix, String indexFormat, String dateFormat, Map<String, Object> additionalProperties) {
        this.values = values;
        this.partitionField = partitionField;
        this.prefix = prefix;
        this.indexFormat = indexFormat;
        this.dateFormat = dateFormat;
        this.additionalProperties = additionalProperties;
    }

    @JsonProperty("values")
    public List<String> getValues() {
        return values;
    }

    @JsonProperty("values")
    public void setValues(List<String> values) {
        this.values = values;
    }

    @JsonProperty("partition.field")
    public String getPartitionField() {
        return partitionField;
    }

    @JsonProperty("partition.field")
    public void setPartitionField(String partitionField) {
        this.partitionField = partitionField;
    }

    @JsonProperty("prefix")
    public String getPrefix() {
        return prefix;
    }

    @JsonProperty("prefix")
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @JsonProperty("index.format")
    public String getIndexFormat() {
        return indexFormat;
    }

    @JsonProperty("index.format")
    public void setIndexFormat(String indexFormat) {
        this.indexFormat = indexFormat;
    }

    @JsonProperty("date.format")
    public String getDateFormat() {
        return dateFormat;
    }

    @JsonProperty("date.format")
    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
}
