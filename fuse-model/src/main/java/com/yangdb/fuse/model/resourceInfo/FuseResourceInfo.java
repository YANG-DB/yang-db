package com.yangdb.fuse.model.resourceInfo;

/*-
 *
 * FuseResourceInfo.java - fuse-model - yangdb - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
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

/**
 * Created by lior.perry on 09/03/2017.
 */
public class FuseResourceInfo extends ResourceInfoBase {
    //region Constructors
    public FuseResourceInfo() {
    }

    public FuseResourceInfo(String resourceUrl, String internalUrl, String healthUrl, String queryStoreUrl, String searchStoreUrl, String catalogStoreUrl) {
        super(resourceUrl, null);
        this.healthUrl = healthUrl;
        this.internal = internalUrl;
        this.queryStoreUrl = queryStoreUrl;
        this.searchStoreUrl = searchStoreUrl;
        this.catalogStoreUrl = catalogStoreUrl;
    }
    //endregion

    //region Properties
    public String getHealthUrl() {
        return this.healthUrl;
    }

    public String getInternal() {
        return internal;
    }

    public String getQueryStoreUrl() {
        return this.queryStoreUrl;
    }

    public String getSearchStoreUrl() {
        return this.searchStoreUrl;
    }

    public String getCatalogStoreUrl() {
        return this.catalogStoreUrl;
    }

    public void setHealthUrl(String healthUrl) {
        this.healthUrl = healthUrl;
    }

    public void setQueryStoreUrl(String queryStoreUrl) {
        this.queryStoreUrl = queryStoreUrl;
    }

    public void setSearchStoreUrl(String searchStoreUrl) {
        this.searchStoreUrl = searchStoreUrl;
    }

    public void setCatalogStoreUrl(String catalogStoreUrl) {
        this.catalogStoreUrl = catalogStoreUrl;
    }

    //endregion

    //region Fields
    private String healthUrl;
    private String internal;
    private String queryStoreUrl;
    private String searchStoreUrl;
    private String catalogStoreUrl;
    //endregion
}
