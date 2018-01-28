package com.kayhut.fuse.unipop.process.traversal.traverser;

import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.bytes.ByteArraySet;
import it.unimi.dsi.fastutil.objects.*;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.process.traversal.Pop;
import org.javatuples.Pair;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

/**
 * Created by Roman on 1/26/2018.
 */
public class ThinPath implements Path {
    private static ObjectArraySet emptySet = new ObjectArraySet(0);

    //region Constructors
    public ThinPath(StringOrdinalDictionary stringOrdinalDictionary) {
        this.objects = Collections.emptyList();
        this.ordinals = Collections.emptyList();
        this.ordinalToObject = null;

        this.stringOrdinalDictionary = stringOrdinalDictionary;
    }

    public ThinPath(StringOrdinalDictionary stringOrdinalDictionary, byte maxCapacity) {
        this(stringOrdinalDictionary);
        this.maxCapacity = maxCapacity;
    }
    //endregion

    //region Path Implementation
    @Override
    public int size() {
        return this.objects.size();
    }

    @Override
    public boolean isEmpty() {
        return this.objects.isEmpty();
    }

    @Override
    public <A> A head() {
        if (this.objects.isEmpty()) {
            return null;
        }

        return (A)this.objects.get(this.objects.size() - 1);
    }

    @Override
    public Path extend(Object o, Set<String> set) {
        if (this.objects.isEmpty()) {
            this.objects = new ArrayList<>(this.maxCapacity);
            this.ordinals = new ArrayList<>(this.maxCapacity);
        }

        this.objects.add(o);
        this.extend(set);
        return this;
    }

    @Override
    public Path extend(Set<String> set) {
        if (this.objects.isEmpty()) {
            throw new UnsupportedOperationException("path has not objects");
        }

        if (set.isEmpty()) {
            return this;
        }

        if (this.ordinalToObject == null) {
            this.ordinalToObject = new Byte2ObjectOpenHashMap<>(this.maxCapacity, 1f);
        }

        if (this.ordinals.size() == this.objects.size() - 1) {
            this.ordinals.add(new ByteArraySet());
        }

        Object head = this.objects.get(this.objects.size() - 1);
        ByteArraySet labelOrdinals = this.ordinals.get(this.ordinals.size() - 1);
        for(String label : set) {
            byte labelOrdinal = this.stringOrdinalDictionary.getOrCreateOrdinal(label);
            labelOrdinals.add(labelOrdinal);

            ObjectArraySet<Object> ordinalObjects = this.ordinalToObject.get(labelOrdinal);
            if (ordinalObjects == null) {
                ordinalObjects = new ObjectArraySet<>();
                this.ordinalToObject.put(labelOrdinal, ordinalObjects);
            }
            ordinalObjects.add(head);
        }
        return this;
    }

    @Override
    public Path retract(Set<String> set) {
        if (this.objects.isEmpty()) {
            return this;
        }

        Object head = this.objects.get(this.objects.size() - 1);
        ByteArraySet labelOrdinals = this.ordinals.get(this.ordinals.size() - 1);
        for(String label : set) {
            byte ordinal = this.stringOrdinalDictionary.getOrdinal(label);
            if (ordinal == 0) {
                continue;
            }

            labelOrdinals.remove(ordinal);
            Set<Object> ordinalObjects = this.ordinalToObject.get(ordinal);
            if (ordinalObjects != null) {
                ordinalObjects.remove(head);
            }
        }
        return this;
    }

    @Override
    public <A> A get(String label) throws IllegalArgumentException {
        if (this.ordinalToObject.isEmpty()) {
            return (A)emptySet;
        }

        byte labelOrdinal = this.stringOrdinalDictionary.getOrdinal(label);
        if (labelOrdinal == 0) {
            return (A)emptySet;
        }

        return (A)this.ordinalToObject.get(labelOrdinal);
    }

    @Override
    public <A> A get(Pop pop, String label) throws IllegalArgumentException {
        ObjectArraySet<Object> objects = this.get(label);

        if (objects.isEmpty() || pop.equals(Pop.all)) {
            return (A)objects;
        }

        ObjectIterator<Object> iterator = objects.iterator();
        if (pop.equals(Pop.first)) {
            return (A)iterator.next();
        }

        iterator.skip(objects.size() - 1);
        return (A)iterator.next();
    }

    @Override
    public <A> A get(int index) {
        return (A)this.objects.get(index);
    }

    @Override
    public boolean hasLabel(String label) {
        if (this.ordinalToObject.isEmpty()) {
            return false;
        }

        byte labelOrdinal = this.stringOrdinalDictionary.getOrdinal(label);
        if (labelOrdinal == 0) {
            return false;
        }

        return this.ordinalToObject.containsKey(labelOrdinal);
    }

    @Override
    public List<Object> objects() {
        return Collections.unmodifiableList(this.objects);
    }

    @Override
    public List<Set<String>> labels() {
        List<Set<String>> labels = new ArrayList<>();
        for(ByteArraySet objectOrdinals : this.ordinals) {
            ObjectArraySet<String> objectLabels = new ObjectArraySet<>();
            for(byte objectOrdinal : objectOrdinals) {
                objectLabels.add(this.stringOrdinalDictionary.getString(objectOrdinal));
            }
            labels.add(Collections.unmodifiableSet(objectLabels));
        }

        return Collections.unmodifiableList(labels);
    }

    @Override
    public Path clone() {
        ThinPath clone = null;
        try {
            clone = (ThinPath)super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        clone.objects = this.objects.isEmpty() ? Collections.emptyList() : new ArrayList<>(this.objects);
        clone.ordinals = this.ordinals.isEmpty() ? Collections.emptyList() : new ArrayList<>(this.ordinals);
        clone.ordinalToObject = this.ordinalToObject == null ? null : new Byte2ObjectOpenHashMap<>(this.ordinalToObject);
        clone.maxCapacity = this.maxCapacity;

        clone.stringOrdinalDictionary = this.stringOrdinalDictionary;

        return clone;
    }

    @Override
    public boolean isSimple() {
        return true;
    }

    @Override
    public Iterator<Object> iterator() {
        return this.objects.iterator();
    }

    @Override
    public void forEach(BiConsumer<Object, Set<String>> consumer) {

    }

    @Override
    public Stream<Pair<Object, Set<String>>> stream() {
        return Stream.empty();
    }

    @Override
    public boolean popEquals(Pop pop, Object other) {
        return false;
    }

    @Override
    public Path subPath(String fromLabel, String toLabel) {
        return null;
    }
    //endregion

    //region Fields
    private Byte2ObjectMap<ObjectArraySet<Object>> ordinalToObject;
    private List<Object> objects;
    private List<ByteArraySet> ordinals;

    private StringOrdinalDictionary stringOrdinalDictionary;
    private byte maxCapacity;
    //endregion
}
