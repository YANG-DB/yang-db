package com.yangdb.fuse.executor.logging;

/*-
 * #%L
 * fuse-dv-core
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
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

import com.yangdb.fuse.dispatcher.logging.*;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.slf4j.Logger;
import org.unipop.process.Profiler;

import java.util.List;

import static com.yangdb.fuse.dispatcher.logging.LogMessage.Level.error;
import static com.yangdb.fuse.dispatcher.logging.LogMessage.Level.trace;
import static com.yangdb.fuse.dispatcher.logging.LogType.*;
import static org.unipop.process.Profiler.PROFILER;

/**
 * Created by roman.margolis on 07/01/2018.
 */
public class LoggingTraversal<S, E> implements Traversal<S, E> {
    //region Constructors
    public LoggingTraversal(
            Traversal<S, E> traversal,
            Logger logger) {
        this.traversal = traversal;
        this.logger = logger;
    }
    //endregion

    //region GraphTraversal Implementation
    @Override
    public Traversal.Admin<S, E> asAdmin() {
        return this.traversal.asAdmin();
    }

    @Override
    public boolean hasNext() {
        return this.traversal.hasNext();
    }

    @Override
    public E next() {
        return this.traversal.next();
    }

    @Override
    public List<E> next(int amount) {
        boolean thrownExcpetion = false;

        try {
            new LogMessage.Impl(this.logger, trace, "start next", sequence, LogType.of(start), next, ElapsedFrom.now()).log();
            return this.traversal.next(amount);
        } catch (Exception ex) {
            thrownExcpetion = true;
            new LogMessage.Impl(this.logger, error, "failed next", sequence, LogType.of(failure), next, ElapsedFrom.now())
                    .with(ex).log();
            throw ex;
        } finally {
            if (!thrownExcpetion) {
                new LogMessage.Impl(this.logger, trace, "finish next", sequence, LogType.of(success), next, ElapsedFrom.now()).log();
            }
        }
    }

    @Override
    public List<E> toList() {
        boolean thrownExcpetion = false;

        try {
            new LogMessage.Impl(this.logger, trace, "start toList", sequence, LogType.of(start), toList, ElapsedFrom.now()).log();
            return this.traversal.toList();
        } catch (Exception ex) {
            thrownExcpetion = true;
            new LogMessage.Impl(this.logger, error, "failed toList", sequence, LogType.of(failure), toList, ElapsedFrom.now())
                    .with(ex).log();
            throw ex;
        } finally {
            if (!thrownExcpetion) {
                new LogMessage.Impl(this.logger, trace, "finish toList", sequence, LogType.of(success), toList, ElapsedFrom.now()).log();
            }
        }
    }
    //endregion

    //region Fields
    private Traversal<S, E> traversal;
    private Logger logger;

    private static LogMessage.MDCWriter profile = MethodName.of("profile");
    private static LogMessage.MDCWriter next = MethodName.of("next");
    private static LogMessage.MDCWriter toList = MethodName.of("toList");

    private static LogMessage.MDCWriter sequence = Sequence.incr();
    //endregion
}
