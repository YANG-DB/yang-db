package com.kayhut.fuse.model.queryAsg;

import com.kayhut.fuse.model.query.EBase;
import javaslang.collection.Stream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by benishue on 23-Feb-17.
 */
public class EBaseAsg<T extends EBase>{
    //region EBaseAsgBuilder
    public static final class EBaseAsgBuilder<T extends EBase> {
        private T eBase;
        private List<EBaseAsg<? extends EBase>> next;
        private List<EBaseAsg<? extends EBase>> b;
        private List<EBaseAsg<? extends EBase>> parent;

        private EBaseAsgBuilder() {
        }

        public static <S extends EBase> EBaseAsgBuilder<S> anEBaseAsg() {
            return new EBaseAsgBuilder<>();
        }

        public EBaseAsgBuilder<T> withEBase(T eBase) {
            this.eBase = eBase;
            return this;
        }

        public EBaseAsgBuilder<T> withNext(List<EBaseAsg<? extends EBase>> next) {
            this.next = next;
            return this;
        }

        public EBaseAsgBuilder<T> withNext(EBaseAsg<? extends EBase> next) {
            this.next = new ArrayList<>();
            this.next.add(next);
            return this;
        }

        public EBaseAsgBuilder<T> withB(List<EBaseAsg<? extends EBase>> b) {
            this.b = b;
            return this;
        }

        public EBaseAsgBuilder<T> withB(EBaseAsg<? extends EBase> b) {
            this.b = new ArrayList<>();
            this.b.add(b);
            return this;
        }

        public EBaseAsgBuilder<T> withParents(List<EBaseAsg<? extends EBase>> parents) {
            this.parent = parents;
            return this;
        }

        public EBaseAsg<T> build() {
            return new EBaseAsg<>(this.eBase, this.next, this.b, this.parent);
        }
    }
    //endregion

    //region Constructors
    public EBaseAsg() {
    }

    public EBaseAsg(T eBase,
                    List<EBaseAsg<? extends EBase>> next,
                    List<EBaseAsg<? extends EBase>> b,
                    List<EBaseAsg<? extends EBase>> parent) {
        this.eBase = eBase;
        this.next = next == null ? new ArrayList<>() : new ArrayList<>(next);
        this.b = b == null ? new ArrayList<>() : new ArrayList<>(b);
        this.parent = parent == null ? new ArrayList<>() : new ArrayList<>(parent);
    }
    //endregion

    //region Properties
    public List<EBaseAsg<? extends EBase>> getNext() {
        return Collections.unmodifiableList(this.next);
    }

    public List<EBaseAsg<? extends EBase>> getB() {
        return Collections.unmodifiableList(this.b);
    }

    public T geteBase() {
        return this.eBase;
    }

    public List<EBaseAsg<? extends EBase>> getParents() {
        return Collections.unmodifiableList(this.parent);
    }

    public int geteNum() {
        return this.eBase.geteNum();
    }
    //endregion

    //region Public Methods
    public void addNextChild(EBaseAsg<? extends EBase> eBaseAsg)
    {
        if (!this.next.contains(eBaseAsg)) {
            this.next.add(eBaseAsg);
        }

        eBaseAsg.addToParents(this);
    }

    public void addBChild(EBaseAsg<? extends EBase> eBaseAsg)
    {
        if (!this.b.contains(eBaseAsg)) {
            this.b.add(eBaseAsg);
        }

        eBaseAsg.addToParents(this);
    }
    //endregion

    //region Private Methods
    private void addToParents(EBaseAsg<? extends EBase> eBaseAsg) {
        if (!this.parent.contains(eBaseAsg)) {
            this.parent.add(eBaseAsg);
        }
    }
    //endregion

    //region Fields
    private T eBase;
    private List<EBaseAsg<? extends EBase>> next;
    private List<EBaseAsg<? extends EBase>> b;
    private List<EBaseAsg<? extends EBase>> parent;
    //endregion
}
