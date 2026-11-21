package com.yangdb.dragons;

/*-
 * #%L
 * fuse-domain-dragons-ext
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

import com.yangdb.dragons.playground.DragonsPlaygroundRegistrar;
import com.yangdb.dragons.services.DragonsExtensionRegistrar;
import com.yangdb.fuse.extension.YangDbExtension;
import com.yangdb.fuse.services.appRegistrars.AppRegistrar;

import java.util.Arrays;
import java.util.List;

/**
 * The Dragons example as a YANG-DB extension, discovered through
 * {@code META-INF/services/com.yangdb.fuse.extension.YangDbExtension}: the playground ({@code /dragons/swagger},
 * {@code POST /fuse/dragons/tutorial/load}) and the extension query routes ({@code /ext}, {@code /fuse/query/clause},
 * {@code /fuse/query/clause/run}, {@code /fuse/query/cypher/logical/run}, {@code /fuse/query/query/v1/logical/run}).
 * <p>
 * {@link DragonsModule} stays in the assembly's config ({@code modules.<profile>}): it is a config-gated
 * {@code ModuleBase} that reads assembly settings, so it is not contributed here (that would bind it twice).
 */
public class DragonsExtension implements YangDbExtension {
    @Override
    public String name() {
        return "dragons";
    }

    @Override
    public List<AppRegistrar> registrars() {
        return Arrays.asList(new DragonsPlaygroundRegistrar(), new DragonsExtensionRegistrar());
    }
}
