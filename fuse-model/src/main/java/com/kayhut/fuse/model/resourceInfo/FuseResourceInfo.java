package com.kayhut.fuse.model.process;

/**
 * Created by User on 09/03/2017.
 */
public class FuseResourceInfo {
    //region Constructors
    public FuseResourceInfo(String healthUrl, String queryStoreUrl, String searchStoreUrl, String catalogStoreUrl) {
        this.healthUrl = healthUrl;
        this.queryStoreUrl = queryStoreUrl;
        this.searchStoreUrl = searchStoreUrl;
        this.catalogStoreUrl = catalogStoreUrl;
    }
    //endregion

    //region Properties
    public String getHealthUrl() {
        return this.healthUrl;
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
    //endregion

    //region Fields
    private String healthUrl;
    private String queryStoreUrl;
    private String searchStoreUrl;
    private String catalogStoreUrl;
    //endregion
}
