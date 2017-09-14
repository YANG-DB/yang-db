package com.kayhut.fuse.unipop.controller.promise.appender;

import com.kayhut.fuse.unipop.controller.ElasticGraphConfiguration;
import com.kayhut.fuse.unipop.controller.common.appender.SearchAppender;
import com.kayhut.fuse.unipop.controller.promise.context.SizeAppenderContext;
import com.kayhut.fuse.unipop.controller.search.SearchBuilder;

/**
 * Created by Roman on 13/04/2017.
 */
public class SizeSearchAppender implements SearchAppender<SizeAppenderContext> {
    //region Constructors
    public SizeSearchAppender(ElasticGraphConfiguration configuration) {
        this.configuration = configuration;
    }
    //endregion

    //region SearchAppender Implementation
    @Override
    public boolean append(SearchBuilder searchBuilder, SizeAppenderContext context) {
        searchBuilder.setLimit(context.getSearchQuery().getLimit() < 0 ?
                configuration.getElasticGraphDefaultSearchSize() :
                Math.min(context.getSearchQuery().getLimit(), configuration.getElasticGraphMaxSearchSize()));

        searchBuilder.setScrollSize(configuration.getElasticGraphScrollSize());
        searchBuilder.setScrollTime(configuration.getElasticGraphScrollTime());

        return true;
    }
    //endregion

    //region Fields
    private ElasticGraphConfiguration configuration;
    //endregion
}
