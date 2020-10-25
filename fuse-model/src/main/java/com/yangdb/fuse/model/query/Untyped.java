package com.yangdb.fuse.model.query;

import java.util.Set;

public interface Untyped {
    //region Properties
    Set<String> getvTypes();

    void setvTypes(Set<String> vTypes);

    Set<String> getNvTypes();

    void setNvTypes(Set<String> nvTypes);
}
