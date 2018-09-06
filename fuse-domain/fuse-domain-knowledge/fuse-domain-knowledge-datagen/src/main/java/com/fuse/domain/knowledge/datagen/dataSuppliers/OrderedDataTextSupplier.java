package com.fuse.domain.knowledge.datagen.dataSuppliers;

import com.kayhut.fuse.unipop.controller.search.DefaultSearchOrderProvider;
import com.kayhut.fuse.unipop.converter.SearchHitScrollIterable;
import javaslang.collection.Stream;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.SearchHit;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Supplier;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.existsQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;

public class OrderedDataTextSupplier implements Supplier<String> {
    //region Constructors
    public OrderedDataTextSupplier(Client client, String type, String fieldName, String index, int maxNumWords) {
        this(client, type, fieldName, null, index, maxNumWords);
    }

    public OrderedDataTextSupplier(Client client, String type, String fieldName, String context, String index, int maxNumWords) {
        SearchHitScrollIterable hits = new SearchHitScrollIterable(
                client,
                context == null ?
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
            String fieldValue = (String) hit.sourceAsMap().get(fieldName);
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
        this.currentIndex = 0;
    }
    //endregion

    //region RandomDataSupplier Implementation
    @Override
    public String get() {
        if (this.currentIndex < this.words.size()) {
            return this.words.get(this.currentIndex++);
        }

        throw new NoSuchElementException();
    }
    //endregion

    //region Fields
    private List<String> words;
    private int currentIndex;
    //endregion
}
