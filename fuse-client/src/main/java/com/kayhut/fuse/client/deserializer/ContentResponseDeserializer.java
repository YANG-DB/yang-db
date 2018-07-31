package com.kayhut.fuse.client.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.type.ReferenceType;
import com.google.inject.Inject;
import com.kayhut.fuse.dispatcher.cursor.CompositeCursorFactory;
import com.kayhut.fuse.model.transport.ContentResponse;
import com.kayhut.fuse.model.transport.ExternalMetadata;
import com.kayhut.fuse.model.transport.cursor.CreateCursorRequest;
import javaslang.Tuple2;
import javaslang.collection.Stream;
import org.jooby.Status;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public class ContentResponseDeserializer extends StdDeserializer<ContentResponse> {
    //region Constructors
    @Inject
    public ContentResponseDeserializer(Map<String, Class> classMap) {
        super(ContentResponse.class);
        this.classMap = classMap;
    }
    //endregion

    //region StdDeserializer Imlementation
    @Override
    public ContentResponse deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        String requestId = null;
        Status status = Status.OK;
        long elapsed = 0;
        long renderElapsed = 0;
        long totalElapsed = 0;
        ExternalMetadata externalMetadata = null;
        Exception error;
        String dataType = null;
        ContentResponse.Builder builder = null;

        String fieldName = jsonParser.nextFieldName();
        while(jsonParser.currentToken() != JsonToken.END_OBJECT) {
            switch (fieldName) {
                case "requestId":
                    requestId = jsonParser.nextTextValue();
                    break;

                case "status":
                    status = Status.valueOf(jsonParser.nextTextValue());
                    break;

                case "elapsed":
                    elapsed = Long.parseLong(jsonParser.nextTextValue());
                    break;

                case "renderElapsed":
                    renderElapsed = Long.parseLong(jsonParser.nextTextValue());
                    break;

                case "totalElapsed":
                    totalElapsed = Long.parseLong(jsonParser.nextTextValue());
                    break;

                case "external":
                    jsonParser.nextToken();
                    externalMetadata = jsonParser.readValueAs(ExternalMetadata.class);
                    break;

                case "dataType":
                    dataType = jsonParser.nextTextValue();
                    break;

                case "data":
                    jsonParser.nextToken();
                    Class dataClass = this.classMap.get(dataType);
                    if (dataClass == null) {
                        // warning
                        dataClass = Map.class;
                    }

                    Object data = jsonParser.readValueAs(dataClass);

                    builder = ContentResponse.Builder.builder(status, status)
                            .requestId(requestId)
                            .elapsed(elapsed)
                            .renderElapsed(renderElapsed)
                            .totalElapsed(totalElapsed)
                            .external(externalMetadata)
                            .data(Optional.of(data));
                    break;

                default:
                    jsonParser.nextValue();
            }

            fieldName = jsonParser.nextFieldName();
        }

        return builder.compose();
    }
    //endregion

    //region Fields
    private Map<String, Class> classMap;
    //endregion
}
