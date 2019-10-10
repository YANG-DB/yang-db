package com.fuse.domain.knowledge.datagen.dataSuppliers;

/*-
 *
 * fuse-domain-knowledge-datagen
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
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
 *
 */

import com.yangdb.fuse.unipop.controller.search.DefaultSearchOrderProvider;
import com.yangdb.fuse.unipop.converter.SearchHitScrollIterable;
import javaslang.collection.Stream;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.SearchHit;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.elasticsearch.index.query.QueryBuilders.*;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

/**
 * Created by Roman on 6/23/2018.
 */
public class UniformDataTextSupplier extends RandomDataSupplier<String> {
    //region Constructors
    public UniformDataTextSupplier(Client client, String type, String fieldName, String index, int maxNumWords) {
        this(client, type, fieldName, null, index, maxNumWords, 0);
    }

    public UniformDataTextSupplier(Client client, String type, String fieldName, String index, int maxNumWords, long seed) {
        this(client, type, fieldName, null, index, maxNumWords, seed);
    }

    public UniformDataTextSupplier(Client client, String type, String fieldName, String context, String index, int maxNumWords) {
        this(client, type, fieldName, index, maxNumWords, 0);
    }

    public UniformDataTextSupplier(Client client, String type, String fieldName, String context, String index, int maxNumWords, long seed) {
        super(seed);
        SearchHitScrollIterable hits = new SearchHitScrollIterable(
                client,
                context != null ?
                        client.prepareSearch().setIndices(index)
                        .setQuery(boolQuery().filter(
                                boolQuery()
                                        .must(termQuery("type", type))
                                        .must(existsQuery(fieldName))
                                        .mustNot(existsQuery("deleteTime"))))
                        .setFetchSource(fieldName, null) :
                        client.prepareSearch().setIndices(index)
                                .setQuery(boolQuery().filter(
                                        boolQuery()
                                                .must(termQuery("type", type))
                                                .must(termQuery("context", context))
                                                .must(existsQuery(fieldName))
                                                .mustNot(existsQuery("deleteTime"))))
                                .setFetchSource(fieldName, null),
                new DefaultSearchOrderProvider().build(null),
                1000000000,
                1000,
                60000);

        Set<String> wordSet = new HashSet<>();
        for (SearchHit hit : hits) {
            String fieldValue = (String) hit.getSourceAsMap().get(fieldName);
            List<String> words = Stream.of(fieldValue.split(" ")).map(String::trim).toJavaList();

            for (String word : words) {
                wordSet.add(word);
                if (wordSet.size() == maxNumWords) {
                    break;
                }
            }

            if (wordSet.size() == maxNumWords) {
                break;
            }
        }

        this.words = Stream.ofAll(wordSet).toJavaList();
    }
    //endregion

    //region RandomDataSupplier Implementation
    @Override
    public String get() {
        return this.words.get(this.random.nextInt(this.words.size()));
    }
    //endregion

    //region Fields
    private List<String> words;
    //endregion
}
