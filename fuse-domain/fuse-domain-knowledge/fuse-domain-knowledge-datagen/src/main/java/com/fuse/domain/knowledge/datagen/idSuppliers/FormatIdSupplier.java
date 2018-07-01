package com.fuse.domain.knowledge.datagen.idSuppliers;

import java.util.function.Supplier;

/**
 * Created by Roman on 6/22/2018.
 */
public class FormatIdSupplier implements Supplier<String> {
    //region Constructors
    public FormatIdSupplier(String format, int current, int limit) {
        this.current = current;
        this.limit = limit;

        this.format = format;
    }
    //endregion

    //region Supplier Implementation
    @Override
    public String get() {
        if (this.current == this.limit) {
            throw new RuntimeException("FormatIdSupplier has reached its limit");
        }

        return String.format(this.format, this.current++);
    }
    //endregion

    //region Fields
    private String format;

    private int current;
    private int limit;
    //endregion
}
