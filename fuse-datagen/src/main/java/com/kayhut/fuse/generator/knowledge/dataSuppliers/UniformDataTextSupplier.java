package com.kayhut.fuse.generator.knowledge.dataSuppliers;

import com.kayhut.fuse.unipop.controller.search.DefaultSearchOrderProvider;
import com.kayhut.fuse.unipop.converter.SearchHitScrollIterable;
import javaslang.collection.Stream;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

import java.util.ArrayList;
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
