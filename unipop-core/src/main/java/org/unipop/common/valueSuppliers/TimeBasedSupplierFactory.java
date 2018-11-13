package org.unipop.common.valueSuppliers;

/*-
 * #%L
 * TimeBasedSupplierFactory.java - unipop-core - kayhut - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2016 - 2018 kayhut
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

import java.util.function.Supplier;

/**
 * Created by Roman on 8/21/2018.
 */
public abstract class TimeBasedSupplierFactory implements Supplier<Supplier<Integer>> {
    //region Constructors
    public TimeBasedSupplierFactory() {
        this.clock = Clock.System.instance;
    }

    public TimeBasedSupplierFactory(Clock clock) {
        this.clock = clock;
    }
    //endregion

    //region Properties
    public Clock getClock() {
        return clock;
    }

    public void setClock(Clock clock) {
        this.clock = clock;
    }
    //endregion

    //region Fields
    protected Clock clock;
    //endregion
}
