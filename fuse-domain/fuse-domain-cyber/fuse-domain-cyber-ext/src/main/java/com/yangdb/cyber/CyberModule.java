package com.yangdb.cyber;

/*-
 * #%L
 * fuse-domain-cyber-ext
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
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

import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.yangdb.cyber.cursor.LogicalGraphHierarchyTraversalCursor;
import com.yangdb.fuse.core.driver.BasicIdGenerator;
import com.yangdb.fuse.dispatcher.cursor.CompositeCursorFactory;
import com.yangdb.fuse.dispatcher.driver.IdGeneratorDriver;
import com.yangdb.fuse.dispatcher.modules.ModuleBase;
import com.yangdb.fuse.dispatcher.ontology.DirectoryIndexProvider;
import com.yangdb.fuse.dispatcher.ontology.IndexProviderFactory;
import com.yangdb.fuse.executor.ontology.schema.load.EntityTransformer;
import com.yangdb.fuse.model.Range;
import com.yangdb.fuse.model.transport.cursor.LogicalGraphCursorRequest;
import org.jooby.Env;

import java.net.URISyntaxException;

import static com.google.inject.name.Names.named;

public class CyberModule extends ModuleBase {
    @Override
    protected void configureInner(Env env, Config conf, Binder binder) throws Throwable {
        Multibinder<CompositeCursorFactory.Binding> bindingMultibinder = Multibinder.newSetBinder(binder, CompositeCursorFactory.Binding.class);
        bindingMultibinder.addBinding().toInstance(new CompositeCursorFactory.Binding(
                LogicalGraphCursorRequest.CursorType,
                LogicalGraphCursorRequest.class,
                new LogicalGraphHierarchyTraversalCursor.Factory()));

//        binder.bind(GraphLayoutProviderFactory.class).toInstance(new DragonsOntologyGraphLayoutProviderFactory(conf.getString("fuse.ontology_provider_dir")));
//        binder.bind(DragonsExtensionQueryController.class).in(RequestScoped.class);

    }



}
