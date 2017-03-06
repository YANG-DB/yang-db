package com.kayhut.fuse.model.transport;

import com.kayhut.fuse.model.process.CursorResourceResult;
import com.kayhut.fuse.model.process.QueryResourceResult;

/**
 * Created by User on 06/03/2017.
 */
public class CursorResourceContent extends BaseContent<CursorResourceResult> {
    public CursorResourceContent(String id, CursorResourceResult data) {
        super(id, data);
    }
}
