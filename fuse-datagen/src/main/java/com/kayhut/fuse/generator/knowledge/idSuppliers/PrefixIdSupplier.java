package com.kayhut.fuse.generator.knowledge.idSuppliers;

import com.kayhut.fuse.generator.knowledge.LightSchema;

import java.util.function.Supplier;

/**
 * Created by Roman on 6/22/2018.
 */
public class LogicalIdSupplier implements Supplier<String> {
    //region Constructors
    public LogicalIdSupplier(int current, int limit, LightSchema schema) {
        this.current = current;
        this.limit = limit;
        this.schema = schema;
    }
    //endregion

    //region Supplier Implementation
    @Override
    public String get() {
        if (this.current == this.limit) {
            throw new RuntimeException("LogicalIdSupplier has reached its limit");
        }

        return String.format("e" + this.schema.getIdFormat(), this.current++);
    }
    //endregion

    //region Fields
    private int current;
    private int limit;

    private LightSchema schema;
    //endregion
}
