package com.yangdb.fuse.client.export;

/*-
 * #%L
 * fuse-core
 * %%
 * Copyright (C) 2016 - 2019 The Fuse Graph Database Project
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

import com.yangdb.fuse.client.export.graphml.GraphMLWriter;
import com.yangdb.fuse.model.transport.cursor.LogicalGraphCursorRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.yangdb.fuse.client.export.graphml.GraphMLTokens.GRAPHML_XMLNS;
import static com.yangdb.fuse.client.export.graphml.GraphMLTokens.LABEL;
import static java.util.Collections.emptyMap;

public class GraphWriterStrategy {
    private Map<LogicalGraphCursorRequest.GraphFormat, GraphWriter> writerMap;

    public GraphWriterStrategy() {
        this.writerMap = new HashMap<>();
        this.writerMap.put(LogicalGraphCursorRequest.GraphFormat.XML,
                new GraphMLWriter(true, emptyMap(), emptyMap(), GRAPHML_XMLNS, LABEL, LABEL));

    }

    public Optional<GraphWriter> writer(LogicalGraphCursorRequest.GraphFormat format) {
        return Optional.ofNullable(writerMap.get(format));
    }
}
