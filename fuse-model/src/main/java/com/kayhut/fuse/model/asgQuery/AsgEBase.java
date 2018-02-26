package com.kayhut.fuse.model.asgQuery;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kayhut.fuse.model.Next;
import com.kayhut.fuse.model.query.EBase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

            this.next.add(next);
            return this;
        }

        public Builder<T> withB(List<AsgEBase<? extends EBase>> b) {
            this.b = b;
            return this;
        }

        public Builder<T> withB(AsgEBase<? extends EBase> b) {
            this.b = new ArrayList<>();
            this.b.add(b);
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
    //endregion

    //region Properties
    public List<AsgEBase<? extends EBase>> getNext() {
        return Collections.unmodifiableList(this.next);
    }

    @Override
    public boolean hasNext() {
        return !this.next.isEmpty();
    }

    @Override
    public void setNext(List<AsgEBase<? extends EBase>> next) {
        this.next = next;
    }

    public List<AsgEBase<? extends EBase>> getB() {
        return Collections.unmodifiableList(this.b);
    }

    public T geteBase() {
        return this.eBase;
    }

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

    public AsgEBase<T> below(AsgEBase<? extends EBase> asgEBase) {
        addBChild(asgEBase);
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
    private List<AsgEBase<? extends EBase>> parent;
    //endregion
}
