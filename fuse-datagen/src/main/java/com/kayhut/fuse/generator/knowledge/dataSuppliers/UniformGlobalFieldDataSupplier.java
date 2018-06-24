package com.kayhut.fuse.generator.knowledge.dataSuppliers;

import com.kayhut.fuse.unipop.controller.search.DefaultSearchOrderProvider;
import com.kayhut.fuse.unipop.converter.SearchHitScrollIterable;
import javaslang.collection.Stream;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.SearchHit;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * Created by Roman on 6/24/2018.
 */
public class UniformGlobalFieldDataSupplier<T> extends RandomDataSupplier<T> {
    //region Constructors
    public UniformGlobalFieldDataSupplier(Client client, String type, String fieldId, List<String> logicalIds, String index, int maxNumItems) {
        this(client, type, fieldId, logicalIds, index, maxNumItems, 0);
    }

    public UniformGlobalFieldDataSupplier(Client client, String type, String fieldId, List<String> logicalIds, String index, int maxNumItems, long seed) {
        super(seed);
        SearchHitScrollIterable hits = new SearchHitScrollIterable(
                client,
                client.prepareSearch().setIndices(index)
                        .setQuery(boolQuery().filter(
                                boolQuery()
                                        .must(termQuery("type", type))
                                        .must(termQuery("fieldId", fieldId))
                                        .must(termQuery("context", "global"))
                                        .must(termsQuery("logicalId", logicalIds))
                                        .mustNot(existsQuery("deleteTime"))))
                        .setFetchSource(new String[] { "stringValue", "intValue", "dateValue" }, null),
                new DefaultSearchOrderProvider().build(null),
                1000000000,
                1000,
                60000);

        Set<T> itemSet = new HashSet<>();
        for (SearchHit hit : hits) {
            T fieldValue = (T) Stream.of(
                    hit.sourceAsMap().get("stringValue"),
                    hit.sourceAsMap().get("intValue"),
                    hit.sourceAsMap().get("dateValue"))
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
