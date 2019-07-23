package com.yangdb.test;

/*-
 * #%L
 * fuse-test-framework
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
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

import com.yangdb.fuse.test.framework.index.ElasticEmbeddedNode;
import com.yangdb.fuse.test.framework.index.GlobalElasticEmbeddedNode;
import org.elasticsearch.client.transport.TransportClient;

public abstract class TestSetupBase {
    protected ElasticEmbeddedNode instance;

    public void init() throws Exception {
        instance = GlobalElasticEmbeddedNode.getInstance();
        loadData(instance.getClient());
    }

    public void cleanup(){
        cleanData(instance.getClient());
    }


    protected abstract void loadData(TransportClient client) throws Exception;
    protected abstract void cleanData(TransportClient client);
}
