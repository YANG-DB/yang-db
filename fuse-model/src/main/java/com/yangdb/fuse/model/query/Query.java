package com.yangdb.fuse.model.query;

/*-
 * #%L
 * fuse-model
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


/*-
 *
 * Query.java - fuse-model - yangdb - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
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


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.yangdb.fuse.model.Next;
import com.yangdb.fuse.model.asgQuery.IQuery;
import com.yangdb.fuse.model.query.entity.EConcrete;
import com.yangdb.fuse.model.query.entity.EEntityBase;
import com.yangdb.fuse.model.query.entity.ETyped;
import com.yangdb.fuse.model.query.properties.EProp;
import com.yangdb.fuse.model.query.properties.EPropGroup;
import com.yangdb.fuse.model.query.properties.RelProp;
import com.yangdb.fuse.model.query.properties.constraint.Constraint;
import com.yangdb.fuse.model.query.properties.constraint.InnerQueryConstraint;
import com.yangdb.fuse.model.query.properties.projection.IdentityProjection;
import com.yangdb.fuse.model.query.quant.Quant1;
import com.yangdb.fuse.model.query.quant.QuantType;
import javaslang.Tuple2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by lior.perry on 19-Feb-17.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonDeserialize(builder = Query.Builder.class)
public class Query implements IQuery<EBase> {

    public String getOnt() {
        return ont;
    }

    public void setOnt(String ont) {
        this.ont = ont;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<EBase> getElements() {
        return elements;
    }

    public void setElements(List<EBase> elements) {
        this.elements = elements;
    }

    public List<List<String>> getNonidentical() {
        return nonidentical;
    }

    public void setNonidentical(List<List<String>> nonidentical) {
        this.nonidentical = nonidentical;
    }

    //region Fields
    private String ont;
    private String name;
    private List<List<String>> nonidentical;
    private List<EBase> elements = new ArrayList<>();
    //endregion

    @JsonPOJOBuilder(buildMethodName = "build", withPrefix = "with")
    public static final class Builder {
        private AtomicInteger sequence = new AtomicInteger(0);
        private int currentIndex = 0;

        private String ont;
        private String name;
        private List<Wrapper<? extends EBase>> elements;
        private List<List<String>> nonidentical;

        private Builder() {
        }

        public static Builder instance() {
            return new Builder();
        }

        public static Builder instance(AtomicInteger sequence) {
            Builder instance = instance();
            instance.sequence = sequence;
            return instance;
        }

        public Builder withOnt(String ont) {
            this.ont = ont;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withElement(EBase element) {
            return withElements(Collections.singletonList(element));
        }

        public Builder withElements(List<EBase> elements) {
            this.elements = elements.stream().map(Wrapper::new).collect(Collectors.toList());
            return this;
        }

        public Builder appendElements(EBase element) {
            return appendElements(Collections.singletonList(element));
        }

        public Builder appendElements(List<EBase> elements) {
            getElements().addAll(elements.stream().map(Wrapper::new).collect(Collectors.toList()));
            return this;
        }

        public Builder start() {
            getElements().add(new Wrapper<>(new Start(sequence.get())));
            currentIndex = sequence.get();
            return this;
        }

        public Builder eType(String type, String tag) {
            populateNext();
            getElements().add(new Wrapper<>(new ETyped(sequence.get(), tag, type, 0), current()));
            currentIndex = sequence.get();
            return this;
        }

        public Builder concrete(String id, String name, String type, String tag) {
            getElements().add(new Wrapper<>(new EConcrete(sequence.get(), tag, type, id, name, sequence.incrementAndGet()), current()));
            return this;
        }

        public Builder rel(String rType, Rel.Direction dir, String tag) {
            populateNext();
            getElements().add(new Wrapper<>(new Rel(sequence.get(), rType, dir, tag, 0), current()));
            currentIndex = sequence.get();
            return this;
        }

        public Builder eProp(String pType) {
            populateNext();
            getElements().add(new Wrapper<>(new EProp(sequence.get(), pType, new IdentityProjection()), current()));
            //current index remain the same since property has no "next"
            return this;
        }

        public Builder eProp(String pType, Constraint constraint) {
            populateNext();
            getElements().add(new Wrapper<>(new EProp(sequence.get(), pType, constraint), current()));
            //current index remain the same since property has no "next"
            return this;
        }

        public Builder ePropGroup(List<Tuple2<String, Optional<Constraint>>> pTypes, QuantType type) {
            populateNext();
            getElements().add(new Wrapper<>(new EPropGroup(sequence.get(), type, pTypes.stream()
                    .map(p -> p._2.map(constraint -> new EProp(sequence.get(), p._1, constraint)).orElseGet(()
                            -> new EProp(sequence.get(), p._1, new IdentityProjection())))
                    .collect(Collectors.toList())), current()));
            //current index remain the same since property has no "next"
            return this;
        }

        public Builder rProp(String pType) {
            populateNext();
            getElements().add(new Wrapper<>(new RelProp(sequence.get(), pType, new IdentityProjection()), current()));
            //current index remain the same since property has no "next"
            return this;
        }

        public Builder rProp(String pType, Constraint constraint) {
            populateNext();
            getElements().add(new Wrapper<>(new RelProp(sequence.get(), pType, constraint), current()));
            //current index remain the same since property has no "next"
            return this;
        }

        public Builder quant(QuantType type) {
            populateNext();
            getElements().add(new Wrapper<>(new Quant1(sequence.get(), type, new ArrayList<>()), current(sequence.get() - 1)));
            currentIndex = sequence.get();
            return this;
        }

        public Builder withNonidentical(List<List<String>> nonidentical) {
            this.nonidentical = nonidentical;
            return this;
        }

        private void populateNext() {
            if (current() instanceof Next) {
                if (((Next) current()).getNext() instanceof List) {
                    ((List) ((Next) current()).getNext()).add(sequence.incrementAndGet());
                } else {
                    ((Next) current()).setNext(sequence.incrementAndGet());
                }
            }
        }

        public int currentIndex() {
            return currentIndex;
        }

        public int currentIndex(int newCurrent) {
            currentIndex = newCurrent;
            return newCurrent;
        }

        public EBase current() {
            return elements.get(currentIndex).getCurrent();
        }

        public Wrapper<? extends EBase> currentWrapper() {
            return elements.get(currentIndex);
        }

        public Wrapper<? extends EBase> currentWrapper(int index) {
            return elements.get(index);
        }

        public EBase pop() {
            return currentWrapper().getParent();
        }

        public Optional<EBase> pop(int index) {
            return Optional.ofNullable(currentWrapper(index).getParent());
        }

        public Optional<EBase> pop(Predicate<EBase> predicate) {
            int currentIndex = currentIndex();
            while (currentIndex != -1 && !predicate.test(current(currentIndex))) {
                if (pop(currentIndex).isPresent())
                    currentIndex = pop(currentIndex).get().geteNum();
                else
                    currentIndex = -1;
            }

            if(currentIndex==-1)
                return Optional.empty();
            else
                return Optional.ofNullable(getElements().get(currentIndex).getCurrent());

        }

        public Wrapper<? extends EBase> popWrapper() {
            return elements.get(currentWrapper().getParent().geteNum());
        }

        public EBase current(int index) {
            return elements.get(index).getCurrent();
        }

        public Query build() {
            Query query = new Query();
            query.setOnt(ont);
            query.setName(name);
            if (elements != null)
                query.setElements(elements.stream().map(Wrapper::getCurrent).collect(Collectors.toList()));
            if (nonidentical != null)
                query.setNonidentical(nonidentical);
            return query;
        }

        private List<Wrapper<? extends EBase>> getElements() {
            if (elements == null) {
                this.elements = new ArrayList<>();
            }
            return elements;
        }
    }

    private static class Wrapper<T> {
        private T parent;
        private T current;

        public Wrapper(T current) {
            this.current = current;
        }

        public Wrapper(T current, T parent) {
            this(current);
            this.parent = parent;
        }

        public T getParent() {
            return parent;
        }

        public T getCurrent() {
            return current;
        }
    }

    public static class QueryUtils {
        /**
         * find query element by its enum
         *
         * @param query
         * @param eNum
         * @return
         */
        public static Optional<? extends EBase> findByEnum(IQuery<EBase> query, int eNum) {
            return query.getElements().stream().filter(p -> p.geteNum() == eNum).findFirst();
        }

        /**
         * find any inner query
         *
         * @param query
         * @return
         */
        public static boolean innerQuery(IQuery<EBase> query) {
            return query.getElements().stream()
                    .filter(p -> p.getClass().isAssignableFrom(EProp.class))
                    .anyMatch(p -> ((EProp) p).getCon().getExpr().getClass().isAssignableFrom(InnerQueryConstraint.class));
        }

        /**
         * look for inner query with given name
         *
         * @param query
         * @param name
         * @return
         */
        public static Optional<Query> innerQuery(IQuery<EBase> query, String name) {
            return query.getElements().stream()
                    .filter(p -> p.getClass().isAssignableFrom(EProp.class))
                    .filter(p -> ((EProp) p).getCon().getExpr().getClass().isAssignableFrom(InnerQueryConstraint.class))
                    .map(p -> ((InnerQueryConstraint) ((EProp) p).getCon().getExpr()).getInnerQuery())
                    .filter(q -> q.getName().equals(name))
                    .findFirst();
        }

        /**
         * @param query
         * @return
         */
        public static <T extends EBase> List<EBase> findByClass(IQuery<EBase> query, Class<T> klass) {
            return query.getElements().stream().filter(p -> klass.isAssignableFrom(p.getClass())).collect(Collectors.toList());
        }

        /**
         * find element by tag name
         *
         * @param query
         * @param tag
         * @return
         */
        public static Optional<? extends EBase> findByTag(IQuery<EBase> query, String tag) {
            return query.getElements().stream()
                    .filter(p -> p.getClass().isAssignableFrom(EEntityBase.class))
                    .filter(p -> ((EEntityBase) p).geteTag().equals(tag))
                    .findFirst();
        }

        public static Query clone(Query origin, List<EBase> elements) {
            return Builder.instance()
                    .withName(origin.name)
                    .withOnt(origin.ont)
                    .withElements(elements)
                    .build();
        }
    }
}
