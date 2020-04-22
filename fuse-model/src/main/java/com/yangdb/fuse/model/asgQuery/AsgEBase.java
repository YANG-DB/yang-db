package com.yangdb.fuse.model.asgQuery;

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
 * AsgEBase.java - fuse-model - yangdb - 2,016
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.yangdb.fuse.model.Next;
import com.yangdb.fuse.model.query.EBase;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by benishue on 23-Feb-17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
//@JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="id")
public class AsgEBase<T extends EBase> implements Next<List<AsgEBase<? extends EBase>>>{



    //region Builder
    public static final class Builder<T extends EBase> {
        private T eBase;
        private List<AsgEBase<? extends EBase>> next;
        private List<AsgEBase<? extends EBase>> b;

        private Builder() {
        }

        public static <S extends EBase> Builder<S> get() {
            return new Builder<>();
        }

        public Builder<T> withEBase(T eBase) {
            this.eBase = eBase;
            return this;
        }

        public Builder<T> withNext(AsgEBase<? extends EBase> next) {
            if (this.next == null) {
                this.next = new ArrayList<>();
            }
            if(next!=null)
                this.next.add(next);
            return this;
        }

        public Builder<T> withB(List<AsgEBase<? extends EBase>> b) {
            this.b = b.stream().filter(Objects::nonNull).collect(Collectors.toList());
            return this;
        }

        public Builder<T> withB(AsgEBase<? extends EBase> b) {
            this.b = new ArrayList<>();
            if(b!=null) {
                this.b.add(b);
            }
            return this;
        }

        public AsgEBase<T> build() {
            AsgEBase<T> asg = new AsgEBase(this.eBase);
            if (this.next != null) this.next.forEach(asg::addNextChild);
            if (this.b != null) this.b.forEach(asg::addBChild);
            return asg;
        }
    }
    //endregion

    //region Constructors
    public AsgEBase() {}

    public AsgEBase(T eBase,
                    List<AsgEBase<? extends EBase>> next,
                    List<AsgEBase<? extends EBase>> b,
                    List<AsgEBase<? extends EBase>> parent) {
        this.eBase = eBase;
        this.next = next == null ? new ArrayList<>() : new ArrayList<>(next);
        this.b = b == null ? new ArrayList<>() : new ArrayList<>(b);
        this.parent = parent == null ? new ArrayList<>() : new ArrayList<>(parent);
    }

    public AsgEBase(T eBase) {
        this.eBase = eBase;
        this.parent = new ArrayList<>();
        this.next = new ArrayList<>();
        this.b = new ArrayList<>();
    }

    @Override
    public AsgEBase<T> clone() {
        return clone(geteNum());
    }

    public AsgEBase<T> clone(int eNum) {
        AsgEBase<T> clone = new AsgEBase<>();
        clone.seteBase((T) eBase.clone(eNum));
        clone.setParent(new ArrayList<>(parent));
        clone.setNext(new ArrayList<>(next));
        clone.setB(new ArrayList<>(b));
        return clone;
    }

    //endregion

    //region Properties
    public List<AsgEBase<? extends EBase>> getNext() {
        return Collections.unmodifiableList(this.next);
    }

    public AsgEBase<? extends EBase> addNext(AsgEBase<? extends EBase> node) {
        this.next.add(node);
        node.setParent(new ArrayList<>(Arrays.asList(this)));
        return this;
    }

    public AsgEBase<? extends EBase> addNext(List<AsgEBase<? extends EBase>> nodes) {
        this.next.addAll(nodes);
        nodes.stream().forEach(e->e.setParent(new ArrayList<>(Arrays.asList(this))));
        return this;
    }

    @Override
    public boolean hasNext() {
        return !this.next.isEmpty();
    }

    @Override
    public void setNext(List<AsgEBase<? extends EBase>> next) {
        this.next = next;
        next.stream().forEach(e->e.setParent(new ArrayList<>(Arrays.asList(this))));
    }

    public List<AsgEBase<? extends EBase>> getB() {
        return Collections.unmodifiableList(this.b);
    }

    public T geteBase() {
        return this.eBase;
    }

    public void seteBase(T eBase) {
        this.eBase = eBase;
    }

    public void setB(List<AsgEBase<? extends EBase>> b) {
        this.b = b;
    }

    public void setParent(List<AsgEBase<? extends EBase>> parent) {
        this.parent = parent;
    }

    @JsonIgnore
    @JsonIgnoreProperties
    public List<AsgEBase<? extends EBase>> getParents() {
        return Collections.unmodifiableList(this.parent);
    }

    public int geteNum() {
        if(this.eBase!=null)
            return this.eBase.geteNum();
        return -1;
    }
    //endregion

    //region Public Methods
    public AsgEBase<T> next(AsgEBase<? extends EBase> asgEBase) {
        addNextChild(asgEBase);
        return this;
    }


    public void addNextChild(AsgEBase<? extends EBase> asgEBase) {
        if (!this.next.contains(asgEBase)) {
            this.next.add(asgEBase);
        }
        asgEBase.addToParents(this);
    }

    public void addNextChild(List<AsgEBase<? extends EBase> >elements) {
        elements.forEach(this::addNextChild);
    }

    public AsgEBase<T> below(AsgEBase<? extends EBase> asgEBase) {
        addBChild(asgEBase);
        return this;
    }

    public AsgEBase<T> below(List<AsgEBase<? extends EBase>> elements) {
        elements.forEach(this::addBChild);
        return this;
    }

    public void addBChild(AsgEBase<? extends EBase> asgEBase) {
        if (!this.b.contains(asgEBase)) {
            this.b.add(asgEBase);
        }

        asgEBase.addToParents(this);
    }

    public void removeNextChildren() {
        this.next.clear();
    }

    public void removeNextChild(AsgEBase<? extends EBase> asgEBase) {
        this.next.remove(asgEBase);
        asgEBase.parent.remove(this);
    }

    public void removeBChild(AsgEBase<? extends EBase> asgEBase) {
        this.b.remove(asgEBase);
        asgEBase.parent.remove(this);
    }
    //endregion


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AsgEBase<?> asgEBase = (AsgEBase<?>) o;

        if (!eBase.equals(asgEBase.eBase)) return false;
        if (!next.equals(asgEBase.next)) return false;
        return b.equals(asgEBase.b);
    }

    @Override
    public int hashCode() {
        int result = eBase.hashCode();
        result = 31 * result + next.hashCode();
        result = 31 * result + b.hashCode();
        return result;
    }

    //region Override Methods
    @Override
    public String toString() {
        //some 'non-educated-developers' recklessly create AsgEBasePlanOp (during tests) without giving them appropriate AsgEbase
        // therefore NPE - why ????
        if(eBase!=null)
            return "Asg(" + this.eBase.toString() + ")";
        return "";
    }
    //endregion

    //region Private Methods
    private void addToParents(AsgEBase<? extends EBase> asgEBase) {
        if (!this.parent.contains(asgEBase)) {
            this.parent.add(asgEBase);
        }
    }
    //endregion

    //region Fields
    private T eBase;
    private List<AsgEBase<? extends EBase>> next;
    private List<AsgEBase<? extends EBase>> b;

    @JsonIgnore
    @JsonIgnoreProperties
    private List<AsgEBase<? extends EBase>> parent;
    //endregion
}
