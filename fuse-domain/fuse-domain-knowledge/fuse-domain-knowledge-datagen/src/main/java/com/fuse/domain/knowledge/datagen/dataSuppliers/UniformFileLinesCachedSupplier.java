package com.fuse.domain.knowledge.datagen.dataSuppliers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Created by Roman on 6/24/2018.
 */
public class UniformFileLinesCachedSupplier implements Supplier<String> {
    //region Constructors
    public UniformFileLinesCachedSupplier(String file) throws IOException {
        List<String> lines = new ArrayList<>();
        if (new File(file).exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    lines.add(line);
                }
            }
        }

        this.cachedSuplier = new UnifromCachedSupplier<>(lines);
    }
    //endregion

    //region Supplier Implementation
    @Override
    public String get() {
        return this.cachedSuplier.get();
    }
    //endregion

    //region Fields
    private UnifromCachedSupplier<String> cachedSuplier;
    //endregion
}
