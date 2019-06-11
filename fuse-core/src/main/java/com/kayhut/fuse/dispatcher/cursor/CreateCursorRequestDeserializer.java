package com.kayhut.fuse.dispatcher.cursor;

/*-
 * #%L
 * fuse-core
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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.google.inject.Inject;
import com.kayhut.fuse.model.transport.cursor.CreateCursorRequest;
import javaslang.Tuple2;
import javaslang.collection.Stream;

import java.io.IOException;
import java.util.Map;

/**
 * Created by Roman on 7/7/2018.
 */
public class CreateCursorRequestDeserializer extends StdDeserializer<CreateCursorRequest> {
    //region Constructors
    @Inject
    public CreateCursorRequestDeserializer(Iterable<CompositeCursorFactory.Binding> cursorBindings) {
        super((Class)null);

        this.cursorClasses = Stream.ofAll(cursorBindings)
                .toJavaMap(binding -> new Tuple2<>(binding.getType(), binding.getKlass()));

        this.mapper = new ObjectMapper();
    }
    //endregion

    //region StdDeserializer Imlementation
    @Override
    public CreateCursorRequest deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        String cursorType = node.get("cursorType").asText();
        Class<? extends CreateCursorRequest> cursorClass = this.cursorClasses.get(cursorType);
        if (cursorClass == null) {
            throw new Exception(String.format("Unregistered cursorType: %s", cursorType));
        }

        return this.mapper.readValue(this.mapper.writeValueAsString(node), cursorClass);
    }
    //endregion

    //region Fields
    private Map<String, Class<? extends CreateCursorRequest>> cursorClasses;
    private ObjectMapper mapper;
    //endregion

    //region Excetion
    public static class Exception extends JsonProcessingException {
        //region Constructors
        protected Exception(String msg) {
            super(msg);
        }
        //endregion
    }
    //endregion
}
