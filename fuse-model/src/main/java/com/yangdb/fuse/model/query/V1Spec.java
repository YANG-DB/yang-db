package com.yangdb.fuse.model.query;

/*-
 * #%L
 * fuse-model
 * %%
 * Copyright (C) 2016 - 2026 The YangDb Graph Database Project
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

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * The published V1 query specification, as shipped in this jar. The JSON Schema's only copy is
 * docs/spec/v1/query.schema.json; the fuse-model build copies it to {@value #QUERY_SCHEMA_RESOURCE}.
 */
public final class V1Spec {
    public static final String QUERY_SCHEMA_RESOURCE = "spec/v1/query.schema.json";
    public static final String SCHEMA_MEDIA_TYPE = "application/schema+json";

    private static volatile String querySchema;

    private V1Spec() {
    }

    /** the V1 query JSON Schema (draft 2020-12) */
    public static String querySchema() {
        String schema = querySchema;
        if (schema == null) {
            try (InputStream in = V1Spec.class.getClassLoader().getResourceAsStream(QUERY_SCHEMA_RESOURCE)) {
                if (in == null) {
                    throw new IllegalStateException(QUERY_SCHEMA_RESOURCE + " is not on the classpath");
                }
                schema = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            querySchema = schema;
        }
        return schema;
    }
}
