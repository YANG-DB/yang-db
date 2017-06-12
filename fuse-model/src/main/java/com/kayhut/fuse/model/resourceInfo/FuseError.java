package com.kayhut.fuse.model.resourceInfo;

/**
 * Created by liorp on 6/11/2017.
 */
public class FuseError {
    private String errorCode;
    private String errorDescription;

    public FuseError() {}

    public FuseError(String errorCode, String errorDescription) {
        this.errorCode = errorCode;
        this.errorDescription = errorDescription;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

}
