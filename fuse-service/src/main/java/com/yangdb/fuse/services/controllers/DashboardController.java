package com.yangdb.fuse.services.controllers;

/*-
 * #%L
 * fuse-service
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

import com.yangdb.fuse.model.transport.ContentResponse;

import java.util.Map;

/**
 * Created by lior.perry on 19/02/2017.
 */
public interface DashboardController<C,D> extends Controller<C,D>{

    ContentResponse<Map> graphElementCount();

    ContentResponse<Map> graphElementCreatedOverTime();

    ContentResponse<Map> graphFieldValuesCount();

    ContentResponse<Map> cursorCount();
}
