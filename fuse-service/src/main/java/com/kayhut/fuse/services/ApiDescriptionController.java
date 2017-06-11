package com.kayhut.fuse.services;

import com.kayhut.fuse.model.resourceInfo.FuseResourceInfo;
import com.kayhut.fuse.model.transport.ContentResponse;

/**
 * Created by Roman on 11/06/2017.
 */
public interface ApiDescriptionController {
    ContentResponse<FuseResourceInfo> getInfo();
}
