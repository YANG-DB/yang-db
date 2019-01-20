package com.kayhut.fuse.unipop.controller.search.translation;

/*-
 * #%L
 * fuse-dv-unipop
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

import java.util.Collections;

/**
 * Created by Roman on 18/05/2017.
 */
public class M1QueryTranslator extends CompositeQueryTranslator {
    //region Static
    public static M1QueryTranslator instance = new M1QueryTranslator();
    //endregion

    //region Constructors
    public M1QueryTranslator() {
        super(
                new HiddenQueryTranslator(
                        new CompareQueryTranslator(true),
                        new ExclusiveChainTranslator(
                                new ContainsGeoBoundsQueryTranslator("geoValue"),
                                new ContainsQueryTranslator()),
                        new ExistsQueryTranslator(),
                        new TextQueryTranslator(),
                        new AndPQueryTranslator(
                                new CompareQueryTranslator(true),
                                new ExclusiveChainTranslator(
                                        new ContainsGeoBoundsQueryTranslator("geoValue"),
                                        new ContainsQueryTranslator()),
                                new ExistsQueryTranslator(),
                                new TextQueryTranslator()
                        ),
                        new OrPQueryTranslator(
                                new CompareQueryTranslator(false),
                                new ExclusiveChainTranslator(
                                        new ContainsGeoBoundsQueryTranslator("geoValue"),
                                        new ContainsQueryTranslator()),
                                new ExistsQueryTranslator(),
                                new TextQueryTranslator()
                        )
                )
        );
    }
    //endregion
}
