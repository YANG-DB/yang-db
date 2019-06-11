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

import javaslang.collection.Stream;

import java.util.List;
import java.util.UUID;

/**
 * Created by Roman on 6/23/2018.
 */
public class UrlSupplier extends RandomDataSupplier<String> {
    //region Constructors
    public UrlSupplier() {}

    public UrlSupplier(long seed) {
        super(seed);
    }
    //endregion

    //region RandomDataSupplier Implementation
    @Override
    public String get() {
        String uuid1 = UUID.randomUUID().toString();
        String uuid2 = UUID.randomUUID().toString();
        String domain = this.domains.get(this.random.nextInt(this.domains.size()));

        return String.format("http://%s.%s/%s", uuid1, domain, uuid2);
    }
    //endregion

    //region Fields
    private List<String> domains = Stream.of("com", "org", "gov", "uk", "us", "nth", "mi", "ru", "fr").toJavaList();
    //endregion
}
