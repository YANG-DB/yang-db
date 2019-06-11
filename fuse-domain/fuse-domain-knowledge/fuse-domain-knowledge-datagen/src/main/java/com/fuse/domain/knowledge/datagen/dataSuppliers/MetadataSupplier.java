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

import com.fuse.domain.knowledge.datagen.model.KnowledgeEntityBase;

import java.util.Collections;
import java.util.Date;
import java.util.function.Supplier;

/**
 * Created by Roman on 6/22/2018.
 */
public class MetadataSupplier implements Supplier<KnowledgeEntityBase.Metadata> {
    //region Constructors
    public MetadataSupplier(Supplier<Date> dateSupplier, Supplier<String> nameSupplier) {
        this(dateSupplier, nameSupplier, Collections::emptyList);
    }

    public MetadataSupplier(Supplier<Date> dateSupplier, Supplier<String> nameSupplier, Supplier<Iterable<String>> refsSupplier) {
        this.dateSupplier = dateSupplier;
        this.nameSupplier = nameSupplier;
        this.refsSupplier = refsSupplier;
    }
    //endregion

    //region Supplier<KnowledgeEntityBase.Metadata> Implementation
    @Override
    public KnowledgeEntityBase.Metadata get() {
        String name = this.nameSupplier.get();
        Date date = this.dateSupplier.get();
        Iterable<String> refs = this.refsSupplier.get();

        return new KnowledgeEntityBase.Metadata(name, date, name, date, refs);
    }
    //endregion

    //region Fields
    private Supplier<Date> dateSupplier;
    private Supplier<String> nameSupplier;
    private Supplier<Iterable<String>> refsSupplier;
    //endregion
}
