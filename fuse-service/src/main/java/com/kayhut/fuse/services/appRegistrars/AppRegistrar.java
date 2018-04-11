package com.kayhut.fuse.services.appRegistrars;

import com.kayhut.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import org.jooby.Jooby;

public interface AppRegistrar {
    void register(Jooby app, AppUrlSupplier appUrlSupplier);
}
