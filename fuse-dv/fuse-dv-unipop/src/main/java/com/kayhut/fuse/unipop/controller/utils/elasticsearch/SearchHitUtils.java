package com.kayhut.fuse.unipop.controller.utils.elasticsearch;

import org.elasticsearch.ElasticsearchParseException;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.collect.Tuple;
import org.elasticsearch.common.compress.Compressor;
import org.elasticsearch.common.compress.CompressorFactory;
import org.elasticsearch.common.xcontent.*;
import org.elasticsearch.search.SearchHit;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;

public class SearchHitUtils {
    public static Map<String, Object> convertToMap(SearchHit searchHit) {
        return convertToMap(searchHit.sourceRef(), false, XContentType.JSON).v2();
    }

    public static Tuple<XContentType, Map<String, Object>> convertToMap(BytesReference bytes, boolean ordered, XContentType xContentType)
            throws ElasticsearchParseException {
        try {
            final XContentType contentType;
            InputStream input;
            Compressor compressor = CompressorFactory.compressor(bytes);
            if (compressor != null) {
                InputStream compressedStreamInput = compressor.streamInput(bytes.streamInput());
                if (compressedStreamInput.markSupported() == false) {
                    compressedStreamInput = new BufferedInputStream(compressedStreamInput);
                }
                input = compressedStreamInput;
            } else {
                input = bytes.streamInput();
            }
            contentType = xContentType != null ? xContentType : XContentFactory.xContentType(input);
            return new Tuple<>(Objects.requireNonNull(contentType), convertToMap(FuseJsonXContent.fuseJsonXContent, input, ordered));
        } catch (IOException e) {
            throw new ElasticsearchParseException("Failed to parse content to map", e);
        }
    }

    public static Map<String, Object> convertToMap(XContent xContent, InputStream input, boolean ordered)
            throws ElasticsearchParseException {
        // It is safe to use EMPTY here because this never uses namedObject
        try (XContentParser parser = xContent.createParser(NamedXContentRegistry.EMPTY, input)) {
            return ordered ? parser.mapOrdered() : parser.map();
        } catch (IOException e) {
            throw new ElasticsearchParseException("Failed to parse content to map", e);
        }
    }
}
