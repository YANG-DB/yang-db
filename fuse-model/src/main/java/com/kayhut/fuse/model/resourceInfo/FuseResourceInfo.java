package com.kayhut.fuse.model.resourceInfo;

/**
 * Created by User on 09/03/2017.
 */
public class FuseResourceInfo extends ResourceInfoBase {
    //region Constructors
    public FuseResourceInfo() {}

    public FuseResourceInfo(String resourceUrl, String healthUrl, String queryStoreUrl, String searchStoreUrl, String catalogStoreUrl) {
        super(resourceUrl,null);
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
    private String queryStoreUrl;
    private String searchStoreUrl;
    private String catalogStoreUrl;
    //endregion
}
