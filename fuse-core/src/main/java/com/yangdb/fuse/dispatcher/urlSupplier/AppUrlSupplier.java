package com.yangdb.fuse.dispatcher.urlSupplier;

/*-
 * #%L
 * fuse-core
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

/**
 * Created by lior.perry on 08/03/2017.
 */
public interface AppUrlSupplier {
    String resourceUrl(String queryId);
    String resourceUrl(String queryId, String cursorId);
    String resourceUrl(String queryId, String cursorId, String pageId);

    String baseUrl();
    String queryStoreUrl();
    String cursorStoreUrl(String queryId);
    String pageStoreUrl(String queryId, String cursorId);

    String catalogStoreUrl();
}
