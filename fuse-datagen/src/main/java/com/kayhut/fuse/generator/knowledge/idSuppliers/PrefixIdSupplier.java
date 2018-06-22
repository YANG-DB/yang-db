package com.kayhut.fuse.generator.knowledge.idSuppliers;

import com.kayhut.fuse.generator.knowledge.LightSchema;

import java.util.function.Supplier;

/**
 * Created by Roman on 6/22/2018.
 */
public class PrefixIdSupplier implements Supplier<String> {
    //region Constructors
    public PrefixIdSupplier(String prefix, int current, int limit, LightSchema schema) {
        this.prefix = prefix;
        this.current = current;
        this.limit = limit;
        this.schema = schema;

        this.format = prefix + schema.getIdFormat();
    }
    //endregion

    //region Supplier Implementation
    @Override
    public String get() {
        if (this.current == this.limit) {
            throw new RuntimeException("PrefixIdSupplier has reached its limit");
        }

        return String.format(this.format, this.current++);
    }
    //endregion

    //region Fields
    private String prefix;
    private String format;

    private int current;
    private int limit;

    private LightSchema schema;
    //endregion
}
