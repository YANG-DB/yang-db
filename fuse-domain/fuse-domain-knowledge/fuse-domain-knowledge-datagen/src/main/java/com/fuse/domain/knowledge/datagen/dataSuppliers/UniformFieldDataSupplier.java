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

import com.kayhut.fuse.unipop.controller.search.DefaultSearchOrderProvider;
import com.kayhut.fuse.unipop.converter.SearchHitScrollIterable;
import javaslang.collection.Stream;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.SearchHit;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.existsQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

/**
 * Created by Roman on 6/23/2018.
 */
public class UniformFieldDataSupplier<T> extends RandomDataSupplier<T> {
    //region Constructors
    public UniformFieldDataSupplier(Client client, String type, String fieldId, String context, String index, int maxNumItems) {
        this(client, type, fieldId, context, index, maxNumItems, 0);
    }

    public UniformFieldDataSupplier(Client client, String type, String fieldId, String context, String index, int maxNumItems, long seed) {
        super(seed);
        SearchHitScrollIterable hits = new SearchHitScrollIterable(
                client,
                client.prepareSearch().setIndices(index)
                        .setQuery(boolQuery().filter(
                                boolQuery()
                                        .must(termQuery("type", type))
                                        .must(termQuery("fieldId", fieldId))
                                        .must(termQuery("context", context))
                                        .mustNot(existsQuery("deleteTime"))))
                        .setFetchSource(new String[] { "stringValue", "intValue", "dateValue" }, null),
                new DefaultSearchOrderProvider().build(null),
                1000000000,
                1000,
                60000);

        Set<T> itemSet = new HashSet<>();
        for (SearchHit hit : hits) {
            T fieldValue = (T)Stream.of(
                    hit.getSourceAsMap().get("stringValue"),
                    hit.getSourceAsMap().get("intValue"),
                    hit.getSourceAsMap().get("dateValue"))
                    .filter(Objects::nonNull)
                    .toJavaOptional().get();
            itemSet.add(fieldValue);

            if (itemSet.size() >= maxNumItems) {
                break;
            }
        }

        this.items = Stream.ofAll(itemSet).toJavaList();
    }
    //endregion

    //region RandomDataSupplier Implementation
    @Override
    public T get() {
        return this.items.get(this.random.nextInt(this.items.size()));
    }
    //endregion

    //region Fields
    private List<T> items;
    //endregion
}
