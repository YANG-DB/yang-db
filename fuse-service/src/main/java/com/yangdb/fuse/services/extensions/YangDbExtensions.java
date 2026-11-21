package com.yangdb.fuse.services.extensions;

/*-
 * #%L
 * fuse-service
 * %%
 * Copyright (C) 2016 - 2026 The YangDb Graph Database Project
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

import com.google.inject.Module;
import com.yangdb.fuse.dispatcher.urlSupplier.AppUrlSupplier;
import com.yangdb.fuse.extension.YangDbExtension;
import com.yangdb.fuse.services.appRegistrars.AppRegistrar;
import org.jooby.Jooby;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * Wires {@link YangDbExtension}s into the service: discovery through {@link ServiceLoader}
 * ({@code META-INF/services/com.yangdb.fuse.extension.YangDbExtension}), their Guice modules into the injector and their
 * route registrars into the app.
 */
public final class YangDbExtensions {
    private static final Logger logger = LoggerFactory.getLogger(YangDbExtensions.class);

    private YangDbExtensions() {
    }

    /** every extension on the class loader's classpath, one instance per class */
    public static List<YangDbExtension> load(ClassLoader classLoader) {
        List<YangDbExtension> extensions = new ArrayList<>();
        Set<Class<?>> seen = new HashSet<>();
        for (YangDbExtension extension : ServiceLoader.load(YangDbExtension.class, classLoader)) {
            if (seen.add(extension.getClass())) {
                logger.info("Loaded YANG-DB extension {}", extension.name());
                extensions.add(extension);
            }
        }
        return extensions;
    }

    /** installs the extensions' Guice and Jooby modules into the app's injector */
    public static void installModules(Jooby app, Collection<YangDbExtension> extensions) {
        if (extensions.isEmpty()) {
            return;
        }
        app.use((env, conf, binder) -> {
            for (YangDbExtension extension : extensions) {
                for (Module module : extension.modules()) {
                    binder.install(module);
                }
                for (Jooby.Module module : extension.joobyModules()) {
                    module.configure(env, conf, binder);
                }
            }
        });
    }

    /**
     * Registers the extensions' route registrars, skipping any registrar class already in {@code registered} (and
     * adding the ones it registers to it), so nothing is registered twice.
     *
     * @return the registrars it registered, in order
     */
    public static List<AppRegistrar> registerRoutes(Jooby app, AppUrlSupplier urlSupplier,
                                                    Collection<YangDbExtension> extensions, Set<Class<?>> registered) {
        List<AppRegistrar> done = new ArrayList<>();
        for (YangDbExtension extension : extensions) {
            for (AppRegistrar registrar : extension.registrars()) {
                if (registered.add(registrar.getClass())) {
                    registrar.register(app, urlSupplier);
                    done.add(registrar);
                } else {
                    logger.info("Extension {}: registrar {} is already registered, skipped", extension.name(),
                            registrar.getClass().getName());
                }
            }
        }
        return done;
    }
}
