package com.yangdb.fuse.executor.opensearch;

/*-
 * #%L
 * fuse-dv-core
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

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.yangdb.fuse.executor.mock.opensearch.MockClient;
import com.yangdb.fuse.opensearch.client.ClientFactory;
import com.yangdb.fuse.unipop.controller.OpensearchGraphConfiguration;
import org.opensearch.client.Client;
import org.opensearch.common.settings.Settings;

/**
 * Created by roman.margolis on 04/01/2018.
 * <p>
 * The remote client is a REST bridge (fuse-opensearch-client) on the cluster's HTTP port: {@code opensearch.http.port},
 * else (deprecated) {@code opensearch.port - 100}.
 */
public class ClientProvider implements Provider<Client> {
    public static final String createMockParameter = "ClientProvider.@createMock";

    @Inject
    //region Constructors
    public ClientProvider(
            @Named(createMockParameter) boolean createMock,
            OpensearchGraphConfiguration configuration) {
        this.createMock = createMock;
        this.configuration = configuration;
    }
    //endregion

    //region Provider Implementation
    @Override
    public Client get() {
        if (this.createMock) {
            System.out.println("Using mock opensearch client!");
            return new MockClient();
        }

        Settings settings = Settings.builder()
                .put("cluster.name", this.configuration.getClusterName())
                .put("client.transport.ignore_cluster_name", this.configuration.getClientTransportIgnoreClusterName())
                .build();
        Integer httpPort = this.configuration.getClusterHttpPort();
        return ClientFactory.remote(this.configuration.getClusterHosts(),
                httpPort != null ? httpPort : ClientFactory.httpPortFromTransportPort(this.configuration.getClusterPort()),
                settings);
    }
    //endregion

    //region Fields
    private boolean createMock;
    private OpensearchGraphConfiguration configuration;
    //endregion
}
