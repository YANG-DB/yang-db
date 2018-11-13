package org.unipop.common.util;

/*-
 * #%L
 * PredicatesTranslator.java - unipop-core - kayhut - 2,016
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

import org.unipop.query.predicates.PredicatesHolder;

/**
 * @author GurRo
 * @since 6/13/2016
 *
 * Given an input of type {@link org.unipop.query.predicates.PredicatesHolder},
 * return T that represents the required resulting query format needed for the appropiate controller.
 *
 */
@FunctionalInterface
public interface PredicatesTranslator<T> {
    T translate(PredicatesHolder holder) ;

}
