package com.yangdb.fuse.extension;

/*-
 * #%L
 * fuse-extension-api
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
import com.yangdb.fuse.services.appRegistrars.AppRegistrar;
import org.jooby.Jooby;

import java.util.Collections;
import java.util.List;

/**
 * The entry point of a YANG-DB extension jar.
 * <p>
 * The service discovers implementations with {@link java.util.ServiceLoader}: list the implementing class (one fully
 * qualified name per line) in {@code META-INF/services/com.yangdb.fuse.extension.YangDbExtension} inside the jar, and
 * put the jar on the service's classpath. No configuration change is needed.
 * <p>
 * At startup the service
 * <ol>
 *     <li>installs every module from {@link #modules()} and {@link #joobyModules()} into its injector, and</li>
 *     <li>calls every registrar from {@link #registrars()} once, after the core routes are registered.</li>
 * </ol>
 * To add a query language, bind its transformer from one of the modules with
 * {@link QueryLanguages#register(com.google.inject.Binder, String)}; it is then reachable at
 * {@code POST /fuse/query/<language>} and {@code GET|POST /fuse/query/<language>/run}.
 * <p>
 * The implementing class needs a public no-argument constructor.
 */
public interface YangDbExtension {

    /** a short name for logs; defaults to the class name */
    default String name() {
        return getClass().getName();
    }

    /**
     * Plain Guice modules to install into the service injector.
     * <p>
     * Don't return a {@code ModuleBase} (fuse-core) here: it only configures itself when it is listed under
     * {@code modules.<profile>} in the application config, which defeats the point of an extension.
     */
    default List<Module> modules() {
        return Collections.emptyList();
    }

    /**
     * Modules that need the application config or environment ({@code configure(env, config, binder)}). The same
     * {@code ModuleBase} caveat as {@link #modules()} applies.
     */
    default List<Jooby.Module> joobyModules() {
        return Collections.emptyList();
    }

    /**
     * Route registrars, each called once with the app and its URL supplier. A registrar class that the service already
     * registers (for example a core {@code AppControllerRegistrarBase} subclass under {@code com.yangdb.fuse.services})
     * is not registered again.
     */
    default List<AppRegistrar> registrars() {
        return Collections.emptyList();
    }
}
