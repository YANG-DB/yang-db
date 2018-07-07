package com.kayhut.fuse.dispatcher.cursor;

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
