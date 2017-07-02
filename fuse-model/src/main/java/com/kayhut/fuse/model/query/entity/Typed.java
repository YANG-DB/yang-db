package com.kayhut.fuse.model.query.entity;

/**
 * Created by liorp on 4/26/2017.
 */
public interface Typed {

    interface eTyped extends Typed{
        void seteType(String eType);

        String geteType();
    }

    interface rTyped extends Typed{
        void setrType(String rType);

        String getrType();
    }
}
