package com.kayhut.fuse.model.queryAsg;

import com.kayhut.fuse.model.query.EBase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by benishue on 23-Feb-17.
 */
public class EBaseAsg{
    //region EBaseAsgBuilder
    public static final class EBaseAsgBuilder {
        private EBase eBase;
        private List<EBaseAsg> next;
        private List<EBaseAsg> b;
        private List<EBaseAsg> parent;

        private EBaseAsgBuilder() {
        }

        public static EBaseAsgBuilder anEBaseAsg() {
            return new EBaseAsgBuilder();
        }

        public EBaseAsgBuilder withEBase(EBase eBase) {
            this.eBase = eBase;
            return this;
        }

        public EBaseAsgBuilder withNext(List<EBaseAsg> next) {
            this.next = next;
            return this;
        }

        public EBaseAsgBuilder withB(List<EBaseAsg> b) {
            this.b = b;
            return this;
        }

        public EBaseAsgBuilder withParents(List<EBaseAsg> parents) {
            this.parent = parents;
            return this;
        }

        public EBaseAsg build() {
            EBaseAsg eBaseAsg = new EBaseAsg(this.eBase, this.next, this.b, this.parent);
            return eBaseAsg;
        }
    }
    //endregion

    //region Constructors
    public EBaseAsg(EBase eBase, List<EBaseAsg> next, List<EBaseAsg> b, List<EBaseAsg> parent) {
        this.eBase = eBase;
        this.next = next == null ? new ArrayList<>() : new ArrayList<>(next);
        this.b = b == null ? new ArrayList<>() : new ArrayList<>(b);
        this.parent = parent == null ? new ArrayList<>() : new ArrayList<>(parent);
    }
    //endregion

    //region Properties
    public List<EBaseAsg> getNext() {
        return Collections.unmodifiableList(this.next);
    }

    public List<EBaseAsg> getB() {
        return Collections.unmodifiableList(this.b);
    }

    public EBase geteBase() {
        return this.eBase;
    }

    public List<EBaseAsg> getParents() {
        return Collections.unmodifiableList(this.parent);
    }
    //endregion

    //region Public Methods
    public void addNextChild(EBaseAsg eBaseAsg)
    {
        if (!this.next.contains(eBaseAsg)) {
            this.next.add(eBaseAsg);
        }

        eBaseAsg.addToParents(this);
    }

    public void addBChild(EBaseAsg eBaseAsg)
    {
        if (!this.b.contains(eBaseAsg)) {
            this.b.add(eBaseAsg);
        }

        eBaseAsg.addToParents(this);
    }
    //endregion

    //region Private Methods
    private void addToParents(EBaseAsg eBaseAsg) {
        if (!this.parent.contains(eBaseAsg)) {
            this.parent.add(eBaseAsg);
        }
    }
    //endregion

    //region Fields
    private EBase eBase;
    private List<EBaseAsg> next;
    private List<EBaseAsg> b;
    private List<EBaseAsg> parent;
    //endregion




}
