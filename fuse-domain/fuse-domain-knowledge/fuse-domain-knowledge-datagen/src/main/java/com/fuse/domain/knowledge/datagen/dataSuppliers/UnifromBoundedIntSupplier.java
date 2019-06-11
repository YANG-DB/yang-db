package com.fuse.domain.knowledge.datagen.dataSuppliers;

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

import java.util.Date;

/**
 * Created by Roman on 6/23/2018.
 */
public class UnifromBoundedIntSupplier extends RandomDataSupplier<Integer> {
    //region Constructors
    public UnifromBoundedIntSupplier(int min, int max) {
        this(min, max, 0);
    }

    public UnifromBoundedIntSupplier(int min, int max, long seed) {
        super(seed);
        this.min = min;
        this.max = max;

        this.diff = this.max - this.min;
    }
    //endregion

    //region Supplier Implementation
    @Override
    public Integer get() {
        return this.min + this.random.nextInt(this.diff);
    }
    //endregion

    //region Fields
    private int min;
    private int max;

    private int diff;
    //endregion
}
