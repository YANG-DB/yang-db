package com.yangdb.fuse.unipop.controller.common.appender;

/*-
 * #%L
 * fuse-dv-unipop
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

import com.yangdb.fuse.unipop.controller.search.SearchBuilder;
import javaslang.collection.Stream;

/**
 * Created by lior.perry on 28/03/2017.
 */
public class CompositeSearchAppender<TContext> implements SearchAppender<TContext> {
    public enum Mode {
        first,
        all
    }

    //region Constructors
    @SafeVarargs
    public CompositeSearchAppender(Mode mode, SearchAppender<TContext>...searchAppenders) {
        this(mode, Stream.of(searchAppenders));
    }

    public CompositeSearchAppender(Mode mode, Iterable<SearchAppender<TContext>> searchAppenders) {
        this.searchAppenders = Stream.ofAll(searchAppenders).toList();
        this.mode = mode;
    }
    //endregion

    //region SearchAppender Implementation
    @Override
    public boolean append(SearchBuilder searchBuilder, TContext context) {
        boolean innerAppenderResult = false;

        for(SearchAppender<TContext> searchAppender : this.searchAppenders) {
            innerAppenderResult = searchAppender.append(searchBuilder, context);

            if (innerAppenderResult && this.mode == Mode.first) {
                return true;
            }
        }

        return innerAppenderResult;
    }
    //endregion

    //region Fields
    private Iterable<SearchAppender<TContext>> searchAppenders;
    private Mode mode;
    //endregion
}
