package com.kayhut.fuse.model.queryAsg;

import com.kayhut.fuse.model.query.EBase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by benishue on 23-Feb-17.
 */
public class EBaseAsg{

    public List<EBaseAsg> getNext() {
        return next;
    }

    public void setNext(List<EBaseAsg> next) {
        this.next = next;
    }

    public List<EBaseAsg> getB() {
        return b;
    }

    public void setB(List<EBaseAsg> b) {
        this.b = b;
    }

    public EBase geteBase() {
        return eBase;
    }

    public void seteBase(EBase eBase) {
        this.eBase = eBase;
    }

    public List<EBaseAsg> getParents() {
        return parents;
    }

    public void setParents(List<EBaseAsg> parents) {
        this.parents = parents;
    }

    public void AddToNextList(EBaseAsg eBaseAsg)
    {
        if (this.next == null)
            this.next = new ArrayList<EBaseAsg>();
        this.next.add(eBaseAsg);
    }

    public void AddToParentsList(EBaseAsg eBaseAsg)
    {
        if (this.parents == null)
            this.parents = new ArrayList<EBaseAsg>();
        this.parents.add(eBaseAsg);
    }

    public static final class EBaseAsgBuilder {
        private EBase eBase;
        private List<EBaseAsg> next;
        private List<EBaseAsg> b;
        private List<EBaseAsg> parents;

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
            this.parents = parents;
            return this;
        }

        public EBaseAsg build() {
            EBaseAsg eBaseAsg = new EBaseAsg();
            eBaseAsg.setNext(next);
            eBaseAsg.setB(b);
            eBaseAsg.setParents(parents);
            eBaseAsg.eBase = this.eBase;
            return eBaseAsg;
        }
    }



    //region Fields
    private EBase eBase;
    private List<EBaseAsg> next;
    private List<EBaseAsg> b;
    private List<EBaseAsg> parents;
    //endregion




}
