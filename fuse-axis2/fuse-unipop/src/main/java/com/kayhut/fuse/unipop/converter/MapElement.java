package com.kayhut.fuse.unipop.converter;

import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.T;
import org.unipop.structure.UniGraph;

import java.util.*;

public class MapElement implements ElementWrapper<Map<String, Object>> {

    public MapElement(Map<String, Object> map, UniGraph graph) {
        this.wrap(map);
        this.graph = graph;

    }

    public MapElement(UniGraph graph) {
        this(null, graph);
    }

    @Override
    public Element wrap(Map<String, Object> map) {
        if (map != null) {
            this.innerId = map.get(T.id.getAccessor());
            this.innerLabel = (String) map.get(T.label.getAccessor());

            map.remove(T.id.getAccessor());
            map.remove(T.label.getAccessor());

            this.internalMap = map;
        } else {
            this.internalMap = new HashMap<>();
        }
        return this;
    }

    @Override
    public Map<String, Object> unwrap() {
        this.internalMap.put(T.id.getAccessor(), this.id());
        this.internalMap.put(T.label.getAccessor(), this.label());

        return this.internalMap;
    }

    @Override
    public Object id() {
        return this.innerId;
    }

    @Override
    public String label() {
        return this.innerLabel;
    }

    @Override
    public Graph graph() {
        return this.graph;
    }

    @Override
    public <V> Property<V> property(String key, V value) {
        throw Exceptions.propertyAdditionNotSupported();
    }

    @Override
    public void remove() {
        throw Property.Exceptions.propertyRemovalNotSupported();
    }

    @Override
    public <V> Iterator<? extends Property<V>> properties(String... propertyKeys) {
        Iterable<String> propKeys = propertyKeys.length > 0 ? Arrays.asList(propertyKeys) :
                this.internalMap != null ? this.internalMap.keySet() :
                        Collections.emptyList();

        ArrayList<Property<V>> properties = new ArrayList<>();
        for(String propKey : propKeys) {
            properties.add(this.<V>property(propKey));
        }

        return properties.iterator();
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
                Object value = internalMap.get(key);
                if (value != null) {
                    return (V) value;
                }
                return null;
            }

            @Override
            public boolean isPresent() {
                if (internalMap != null) {
                    return internalMap.containsKey(key);
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
    public Set<String> keys() {
        return this.internalMap.keySet();
    }

    private Object innerId;
    private String innerLabel;

    private Map<String, Object> internalMap;
    private UniGraph graph;
}
