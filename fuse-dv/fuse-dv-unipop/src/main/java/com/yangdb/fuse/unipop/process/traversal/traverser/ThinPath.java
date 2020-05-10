package com.yangdb.fuse.unipop.process.traversal.traverser;

/*-
 * #%L
 * fuse-dv-unipop
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
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
    //region Constructors
    public ThinPath(StringOrdinalDictionary stringOrdinalDictionary) {
        this.objects = Collections.emptyList();
        this.ordinals = Collections.emptyList();

        this.stringOrdinalDictionary = stringOrdinalDictionary;
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
            this.objects = new ArrayList<>();
            this.ordinals = new ArrayList<>();
        }

        this.objects.add(o);
        this.extend(set);
        return this;
    }

    @Override
    public Path extend(Set<String> set) {
        if (this.objects.isEmpty()) {
            throw new UnsupportedOperationException("path has no objects");
        }

        if (set.isEmpty()) {
            return this;
        }

        if (this.ordinals.size() == this.objects.size() - 1) {
            this.ordinals.add((byte)0);
        }

        for(String label : set) {
            byte labelOrdinal = this.stringOrdinalDictionary.getOrCreateOrdinal(label);
            this.ordinals.set(this.ordinals.size() - 1, labelOrdinal);
            break;
        }

        return this;
    }

    @Override
    public Path retract(Set<String> set) {
        if (this.objects.isEmpty()) {
            return this;
        }

        if (this.ordinals.size() == this.objects.size() - 1) {
            return this;
        }

        byte currentLabelOrdinal = this.ordinals.get(this.ordinals.size() - 1);
        for(String label : set) {
            byte ordinal = this.stringOrdinalDictionary.getOrdinal(label);
            if (ordinal == 0 || ordinal != currentLabelOrdinal) {
                continue;
            }

            this.ordinals.set(this.ordinals.size() - 1, (byte)0);
            break;
        }

        return this;
    }

    @Override
    public <A> A get(String label) throws IllegalArgumentException {
        byte labelOrdinal = this.stringOrdinalDictionary.getOrdinal(label);
        if (labelOrdinal == 0) {
            return null;
        }

        for(int index = 0 ; index < this.objects.size() ; index++) {
            if (this.ordinals.get(index) == labelOrdinal) {
                return (A)this.objects.get(index);
            }
        }

        return null;
    }

    @Override
    public <A> A get(Pop pop, String label) throws IllegalArgumentException {
        return this.get(label);
    }

    @Override
    public <A> A get(int index) {
        return (A)this.objects.get(index);
    }

    @Override
    public boolean hasLabel(String label) {
        byte labelOrdinal = this.stringOrdinalDictionary.getOrdinal(label);
        if (labelOrdinal == 0) {
            return false;
        }

        for(Byte ordinal : this.ordinals) {
            if (ordinal == labelOrdinal) {
                return true;
            }
        }

        return false;
    }

    @Override
    public List<Object> objects() {
        return Collections.unmodifiableList(this.objects);
    }

    @Override
    public List<Set<String>> labels() {
        List<Set<String>> labels = new ArrayList<>(this.ordinals.size());
        for(Byte ordinal : this.ordinals) {
            String label = this.stringOrdinalDictionary.getString(ordinal);
            labels.add(label == null ?
                    Collections.emptySet() :
                    Collections.singleton(this.stringOrdinalDictionary.getString(ordinal)));
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
        if (fromLabel == null && toLabel == null) {
            return this;
        }

        //TODO: add implementation for subpath
        throw new UnsupportedOperationException("subpath on ThinPath temporarily not supported");
    }
    //endregion

    //region Fields
    private List<Object> objects;
    private List<Byte> ordinals;

    private StringOrdinalDictionary stringOrdinalDictionary;
    //endregion
}
