package com.kayhut.fuse.unipop.controller.utils.elasticsearch;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentBuilderExtension;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

public class FuseXContentBuilderExtension implements XContentBuilderExtension {
    @Override
    public Map<Class<?>, XContentBuilder.Writer> getXContentWriters() {
        return Collections.singletonMap(Character.class, (builder, value) -> builder.value(value.toString()));
    }

    @Override
    public Map<Class<?>, XContentBuilder.HumanReadableTransformer> getXContentHumanReadableTransformers() {
        return Collections.emptyMap();
    }

    @Override
    public Map<Class<?>, Function<Object, Object>> getDateTransformers() {
        return Collections.emptyMap();
    }
}
