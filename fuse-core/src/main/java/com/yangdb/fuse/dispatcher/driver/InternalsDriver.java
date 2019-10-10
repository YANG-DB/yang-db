package com.yangdb.fuse.dispatcher.driver;

/*-
 *
 * fuse-core
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
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
 *
 */

import java.util.Optional;

/**
 * Created by lior.perry on 21/02/2017.
 */
public interface InternalsDriver {
    Optional<com.typesafe.config.Config> getConfig();
    Optional<String> getStatisticsProviderName();
    Optional<String> getStatisticsProviderSetup();
    Optional<String> refreshStatisticsProviderSetup();
}
