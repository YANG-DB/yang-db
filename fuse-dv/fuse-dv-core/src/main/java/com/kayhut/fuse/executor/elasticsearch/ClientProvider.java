package com.kayhut.fuse.executor.elasticsearch;

/*-
 * #%L
 * fuse-dv-core
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

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.kayhut.fuse.executor.mock.elasticsearch.MockClient;
import com.kayhut.fuse.unipop.controller.ElasticGraphConfiguration;
import javaslang.collection.Stream;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by roman.margolis on 04/01/2018.
 */
public class ClientProvider implements Provider<Client> {
    public static final String createMockParameter = "ClientProvider.@createMock";

    @Inject
    //region Constructors
    public ClientProvider(
            @Named(createMockParameter) boolean createMock,
            ElasticGraphConfiguration configuration) {
        this.createMock = createMock;
        this.configuration = configuration;
    }
    //endregion

    //region Provider Implementation
    @Override
    public Client get() {
        if (this.createMock) {
            System.out.println("Using mock elasticsearch client!");
            return new MockClient();
        }

        Settings settings = Settings.builder()
                .put("cluster.name", this.configuration.getClusterName())
                .put("client.transport.ignore_cluster_name", this.configuration.getClientTransportIgnoreClusterName())
                .build();
        TransportClient client = new PreBuiltTransportClient(settings);
        Stream.of(this.configuration.getClusterHosts()).forEach(host -> {
            try {
                client.addTransportAddress(new TransportAddress(InetAddress.getByName(host), this.configuration.getClusterPort()));
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        });

        return client;
    }
    //endregion

    //region Fields
    private boolean createMock;
    private ElasticGraphConfiguration configuration;
    //endregion
}
