package org.unipop.query.controller;

/*-
 * #%L
 * ControllerManager.java - unipop-core - yangdb - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public interface ControllerManager {

    Set<UniQueryController> getControllers();

    default <T extends UniQueryController> List<T> getControllers(Class<? extends T> c){
        return getControllers().stream()
                .filter(controller -> c.isAssignableFrom(controller.getClass()))
                .map(controller -> (T) controller)
                .collect(Collectors.toList());
    }

    void close();
}
