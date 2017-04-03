package com.kayhut.fuse.unipop.converter;

import com.google.common.collect.Lists;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.elasticsearch.search.SearchHit;
import org.unipop.structure.UniGraph;

import java.util.*;

@SuppressWarnings("Duplicates")
public class SearchHitIterableElement implements ElementWrapper<Map.Entry<String, Iterable<SearchHit>>> {
    //region Constructor
    public SearchHitIterableElement(Map.Entry<String, Iterable<SearchHit>> searchHitEntry, UniGraph graph) {
        this.searchHitEntry = searchHitEntry;
        flatDocumentMap = flattenDocuments();

        this.graph = graph;
    }
    //endregion

    //region Wrapper Implementation
    @Override
    public Element wrap(Map.Entry<String, Iterable<SearchHit>> searchHitEntry) {
        this.searchHitEntry = searchHitEntry;
        flatDocumentMap = flattenDocuments();
        return this;
    }

    @Override
    public Map.Entry<String, Iterable<SearchHit>> unwrap() {
        return this.searchHitEntry;
    }
    //endregion

    //region Element Implementation
    @Override
    public Object id() {
        return this.searchHitEntry.getKey();
    }

    @Override
    public String label() {
        return "entity";
    }

    @Override
    public Graph graph() {
        return this.graph;
    }

    @Override
    public Set<String> keys() {

        if (flatDocumentMap != null) {
            return flatDocumentMap.keySet();
        }
        return Collections.emptySet();
    }

    protected Map<String, Collection<Object>> flattenDocuments() {
        if (searchHitEntry == null) {
            return null;
        }
        Map<String, Collection<Object>> finalMap = new HashMap<>();

        Lists.newArrayList(this.searchHitEntry.getValue()).stream()
                .map(SearchHit::sourceAsMap)
                .forEach(entry -> entry.forEach((String key, Object vals) -> {
                    Collection<Object> insertData = new HashSet<>();

                    if (finalMap.containsKey(key)) {
                        Collection<Object> existingData = finalMap.get(key);
                        insertData.addAll(existingData);
                        existingData.forEach(data -> insertData.add(data));
                        if (vals instanceof Iterable) {
                            ((Iterable) vals).forEach(value -> insertData.add(value));
                        }
                    }
                    insertData.add(vals);
                    finalMap.put(key, insertData);
                }));

        return finalMap;
    }

    @Override
    public <V> Property<V> property(String key) {
        final Element thisElement = this;
        return new Property<V>() {
            @Override
            public String key() {
                return key;
            }

            @Override
            public V value() throws NoSuchElementException {
                if (flatDocumentMap != null) {
                    return (V) flatDocumentMap.get(key);
                }
                return null;
            }

            @Override
            public boolean isPresent() {
                if (flatDocumentMap != null) {
                    return flatDocumentMap.containsKey(key);
                }
                return false;
            }

            @Override
            public Element element() {
                return thisElement;
            }

            @Override
            public void remove() {
                throw Exceptions.propertyRemovalNotSupported();
            }
        };
    }

    @Override
    public <V> Property<V> property(String key, V value) {
        throw Element.Exceptions.propertyAdditionNotSupported();
    }

    @Override
    public <V> V value(String key) throws NoSuchElementException {
        if (this.flatDocumentMap != null) {
            return (V) this.flatDocumentMap.get(key);
        }

        return null;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <V> Iterator<? extends Property<V>> properties(String... strings) {
        Iterable<String> propKeys = strings.length > 0 ? Arrays.asList(strings) :
                this.flatDocumentMap != null ? this.flatDocumentMap.keySet() :
                        Collections.emptyList();

        ArrayList<Property<V>> properties = new ArrayList<>();
        for (String propKey : propKeys) {
            properties.add(this.<V>property(propKey));
        }

        return properties.iterator();
    }
    //endregion

    //region Fields
    private final Graph graph;
    private Map.Entry<String, Iterable<SearchHit>> searchHitEntry;
    private Map<String, Collection<Object>> flatDocumentMap;
    //endregion
}
