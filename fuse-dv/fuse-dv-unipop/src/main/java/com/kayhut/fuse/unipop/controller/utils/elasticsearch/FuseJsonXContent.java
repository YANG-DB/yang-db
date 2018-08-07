package com.kayhut.fuse.unipop.controller.utils.elasticsearch;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.io.FastStringReader;
import org.elasticsearch.common.xcontent.*;
import org.elasticsearch.common.xcontent.json.JsonXContentGenerator;
import org.elasticsearch.common.xcontent.json.JsonXContentParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.Set;

public class FuseJsonXContent implements XContent {

    public static XContentBuilder contentBuilder() throws IOException {
        return XContentBuilder.builder(fuseJsonXContent);
    }

    private static final JsonFactory jsonFactory;
    public static final String JSON_ALLOW_UNQUOTED_FIELD_NAMES = "elasticsearch.json.allow_unquoted_field_names";
    public static final FuseJsonXContent fuseJsonXContent;
    public static final boolean unquotedFieldNamesSet;

    static {
        jsonFactory = new JsonFactory();
        // TODO: Remove the system property configuration for this in Elasticsearch 6.0.0
        String jsonUnquoteProp = System.getProperty(JSON_ALLOW_UNQUOTED_FIELD_NAMES);
        if (jsonUnquoteProp == null) {
            unquotedFieldNamesSet = false;
            jsonFactory.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, false);
        } else {
            unquotedFieldNamesSet = true;
            switch (jsonUnquoteProp) {
                case "true":
                    jsonFactory.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
                    break;
                case "false":
                    jsonFactory.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, false);
                    break;
                default:
                    throw new IllegalArgumentException("invalid value for [" + JSON_ALLOW_UNQUOTED_FIELD_NAMES + "]: " + jsonUnquoteProp);
            }
        }
        jsonFactory.configure(JsonGenerator.Feature.QUOTE_FIELD_NAMES, true);
        jsonFactory.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        jsonFactory.configure(JsonFactory.Feature.FAIL_ON_SYMBOL_HASH_OVERFLOW, false); // this trips on many mappings now...
        // Do not automatically close unclosed objects/arrays in com.fasterxml.jackson.core.json.UTF8JsonGenerator#close() method
        jsonFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT, false);
        fuseJsonXContent = new FuseJsonXContent();
    }

    private FuseJsonXContent() {
    }

    @Override
    public XContentType type() {
        return XContentType.JSON;
    }

    @Override
    public byte streamSeparator() {
        return '\n';
    }

    @Override
    public XContentGenerator createGenerator(OutputStream os, Set<String> includes, Set<String> excludes) throws IOException {
        return new JsonXContentGenerator(jsonFactory.createGenerator(os, JsonEncoding.UTF8), os, includes, excludes);
    }

    @Override
    public XContentParser createParser(NamedXContentRegistry xContentRegistry, String content) throws IOException {
        return new FuseJsonXContentParser(xContentRegistry, jsonFactory.createParser(new FastStringReader(content)));
    }

    @Override
    public XContentParser createParser(NamedXContentRegistry xContentRegistry, InputStream is) throws IOException {
        return new FuseJsonXContentParser(xContentRegistry, jsonFactory.createParser(is));
    }

    @Override
    public XContentParser createParser(NamedXContentRegistry xContentRegistry, byte[] data) throws IOException {
        return new FuseJsonXContentParser(xContentRegistry, jsonFactory.createParser(data));
    }

    @Override
    public XContentParser createParser(NamedXContentRegistry xContentRegistry, byte[] data, int offset, int length) throws IOException {
        return new FuseJsonXContentParser(xContentRegistry, jsonFactory.createParser(data, offset, length));
    }

    @Override
    public XContentParser createParser(NamedXContentRegistry xContentRegistry, BytesReference bytes) throws IOException {
        return createParser(xContentRegistry, bytes.streamInput());
    }

    @Override
    public XContentParser createParser(NamedXContentRegistry xContentRegistry, Reader reader) throws IOException {
        return new FuseJsonXContentParser(xContentRegistry, jsonFactory.createParser(reader));
    }
}
