package com.fuse.domain.knowledge.datagen.dataSuppliers;

/*-
 * #%L
 * fuse-domain-knowledge-datagen
 * %%
 * Copyright (C) 2016 - 2018 kayhut
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

import java.io.BufferedReader;
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
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
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
