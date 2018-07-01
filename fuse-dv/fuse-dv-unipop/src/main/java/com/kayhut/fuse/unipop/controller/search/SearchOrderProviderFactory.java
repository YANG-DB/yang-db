package com.kayhut.fuse.unipop.controller.search;

import com.kayhut.fuse.unipop.controller.common.context.CompositeControllerContext;

public interface SearchOrderProviderFactory  {
    SearchOrderProvider build(CompositeControllerContext context);
}
