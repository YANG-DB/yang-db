package org.slf4j.impl;

/*-
 * #%L
 * logging
 * %%
 * Copyright (C) 2016 - 2022 The YangDb Graph Database Project
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

import com.yangdb.logging.log4j2.slf4j.Log4jExtendedMDCAdapter;
import com.yangdb.logging.slf4j.api.ExtendedMDCAdapter;

public class StaticExtendedMDCBinder {
    /**
     * The unique instance of this class.
     */
    public static final StaticExtendedMDCBinder SINGLETON = new StaticExtendedMDCBinder();

    public static StaticExtendedMDCBinder getSingleton() {
        return SINGLETON;
    }

    private StaticExtendedMDCBinder() {
    }

    /**
     * Currently this method always returns an instance of {@link StaticMDCBinder}.
     * @return an  extended MDC adapter
     */
    public ExtendedMDCAdapter getEMDCA() {
        return new Log4jExtendedMDCAdapter();
    }

    /**
     * Retrieve the adapter class name.
     * @return The adapter class name.
     */
    public String getMDCAdapterClassStr() {
        return Log4jExtendedMDCAdapter.class.getName();
    }
}
