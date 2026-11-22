package com.yangdb.fuse.epb.plan.statistics.provider;

/*-
 * #%L
 * fuse-dv-epb
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

import com.yangdb.fuse.epb.plan.statistics.configuration.StatConfig;
import com.yangdb.fuse.opensearch.client.ClientFactory;
import org.opensearch.client.Client;
import org.opensearch.common.settings.Settings;

/**
 * Created by benishue on 24-May-17.
 * <p>
 * The statistics client is a REST bridge (fuse-opensearch-client) on the HTTP port {@link StatConfig#getStatHttpPort()}.
 */
public class ElasticClientProvider {

    //region Ctrs
    public ElasticClientProvider(StatConfig config) {
        this.config = config;
    }
    //endregion

    //region Public Methods
    public Client getStatClient() {
        Settings settings = Settings.builder().put("cluster.name", config.getStatClusterName()).build();
        return ClientFactory.remote(config.getStatNodesHosts(), config.getStatHttpPort(), settings);
    }
    //endregion

    //region Fields
    private final StatConfig config;
    //endregion

}
