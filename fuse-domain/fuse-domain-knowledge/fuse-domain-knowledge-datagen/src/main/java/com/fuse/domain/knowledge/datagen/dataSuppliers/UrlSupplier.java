package com.fuse.domain.knowledge.datagen.dataSuppliers;

import javaslang.collection.Stream;

import java.util.List;
import java.util.UUID;

/**
 * Created by Roman on 6/23/2018.
 */
public class UrlSupplier extends RandomDataSupplier<String> {
    //region Constructors
    public UrlSupplier() {}

    public UrlSupplier(long seed) {
        super(seed);
    }
    //endregion

    //region RandomDataSupplier Implementation
    @Override
    public String get() {
        String uuid1 = UUID.randomUUID().toString();
        String uuid2 = UUID.randomUUID().toString();
        String domain = this.domains.get(this.random.nextInt(this.domains.size()));

        return String.format("http://%s.%s/%s", uuid1, domain, uuid2);
    }
    //endregion

    //region Fields
    private List<String> domains = Stream.of("com", "org", "gov", "uk", "us", "nth", "mi", "ru", "fr").toJavaList();
    //endregion
}
