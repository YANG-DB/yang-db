package com.fuse.domain.knowledge.datagen.idSuppliers;

/*-
 * #%L
 * fuse-domain-knowledge-datagen
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
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
