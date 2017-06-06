package com.kayhut.fuse.model.query.entity;

/**
 * Created by liorp on 4/26/2017.
 */
public interface Typed {

    interface eTyped extends Typed{
        void seteType(int eType);

        int geteType();
    }

    interface rTyped extends Typed{
        void setrType(int rType);

        int getrType();
    }
}
