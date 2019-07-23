package com.yangdb.fuse.model.resourceInfo;

/*-
 * #%L
 * StoreResourceInfo.java - fuse-model - yangdb - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
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
 * Created by lior.perry on 09/03/2017.
 */
public class StoreResourceInfo extends ResourceInfoBase {
    //region Constructors
    public StoreResourceInfo() {}

    public StoreResourceInfo(String resourceUrl,String resourceId, Iterable<String> resourceUrls) {
        super(resourceUrl,resourceId);
        this.resourceUrls = resourceUrls;
    }
    //endregion

    //region Properties
    public Iterable<String> getResourceUrls() {
        return this.resourceUrls;
    }

    public void setResourceUrls(Iterable<String> resourceUrls) {
        this.resourceUrls = resourceUrls;
    }
    //endregion

    //region Fields
    private Iterable<String> resourceUrls;
    //endregion
}
