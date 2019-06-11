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

import java.util.function.Supplier;

/**
 * Created by Roman on 6/23/2018.
 */
public class ProbabilisticCompositeSupplier<T> extends RandomDataSupplier<T> {
    //region Constructors
    public ProbabilisticCompositeSupplier(Supplier<T> supplier1, double pSupplier1, Supplier<T> supplier2) {
        this(supplier1, pSupplier1, supplier2, 0);
    }

    public ProbabilisticCompositeSupplier(Supplier<T> supplier1, double pSupplier1, Supplier<T> supplier2, long seed) {
        super(seed);
        this.supplier1 = supplier1;
        this.supplier2 = supplier2;
        this.pSupplier1 = pSupplier1;
    }
    //endregion

    //region Supplier Implementation
    @Override
    public T get() {
        return this.random.nextDouble() < pSupplier1 ?
                this.supplier1.get() :
                this.supplier2.get();
    }
    //endregion

    //region Fields
    private Supplier<T> supplier1;
    private double pSupplier1;
    private Supplier<T> supplier2;

    //endregion
}
