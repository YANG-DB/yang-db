package com.yangdb.fuse.assembly.knowledge;

/*-
 * #%L
 * fuse-domain-knowledge-ext
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
import com.yangdb.fuse.assembly.knowledge.cursor.KnowledgeGraphHierarchyTraversalCursor;
import com.yangdb.fuse.assembly.knowledge.cursor.KnowledgeLogicalGraphCursor;
import com.yangdb.fuse.assembly.knowledge.parser.FolderBasedTypeProvider;
import com.yangdb.fuse.assembly.knowledge.parser.model.BusinessTypesProvider;
import com.yangdb.fuse.core.driver.BasicIdGenerator;
import com.yangdb.fuse.dispatcher.cursor.CompositeCursorFactory;
import com.yangdb.fuse.dispatcher.driver.IdGeneratorDriver;
import com.yangdb.fuse.dispatcher.modules.ModuleBase;
import com.yangdb.fuse.ext.driver.ExtensionQueryDriver;
import com.yangdb.fuse.model.Range;
import com.yangdb.fuse.model.transport.cursor.LogicalGraphCursorRequest;
import com.yangdb.fuse.services.KnowledgeExtensionQueryController;
import org.jooby.Env;
import org.jooby.scope.RequestScoped;

import java.net.URISyntaxException;

import static com.google.inject.name.Names.named;

public class KnowledgeModule extends ModuleBase {
    //region ModuleBase Implementation
    @Override
    protected void configureInner(Env env, Config conf, Binder binder) throws Throwable {
        Multibinder<CompositeCursorFactory.Binding> bindingMultibinder = Multibinder.newSetBinder(binder, CompositeCursorFactory.Binding.class);
        //KnowledgeGraphHierarchyCursor
        bindingMultibinder.addBinding().toInstance(new CompositeCursorFactory.Binding(
                KnowledgeGraphHierarchyCursorRequest.CursorType,
                KnowledgeGraphHierarchyCursorRequest.class,
                new KnowledgeGraphHierarchyTraversalCursor.Factory()));

        //KnowledgeGraphHierarchyCursor
        bindingMultibinder.addBinding().toInstance(new CompositeCursorFactory.Binding(
                LogicalGraphCursorRequest.CursorType,
                LogicalGraphCursorRequest.class,
                new KnowledgeLogicalGraphCursor.Factory()));

        binder.bind(BusinessTypesProvider.class).toInstance(provider(conf));
        binder.bind(ExtensionQueryDriver.class).in(RequestScoped.class);
        binder.bind(KnowledgeExtensionQueryController.class).in(RequestScoped.class);
    }


    private BusinessTypesProvider provider(Config conf) throws URISyntaxException {
        try {
            return new FolderBasedTypeProvider(conf.getString("Knowledge.business_type_provider_dir"));
        }catch (ConfigException.Missing missing) {
            return new FolderBasedTypeProvider("ontology");
        }

    }
    //endregion
}
