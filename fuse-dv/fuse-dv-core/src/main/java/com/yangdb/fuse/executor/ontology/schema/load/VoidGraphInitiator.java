package com.yangdb.fuse.executor.ontology.schema.load;

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

import java.io.IOException;

public class VoidGraphInitiator implements GraphInitiator {
    //region GraphDataLoader Implementation
    @Override
    public long init(String ontology)  {
        return 0;
    }

    @Override
    public long init()  {
        return 0;
    }


    @Override
    public long drop(String ontology)  {
        return 0;
    }

    @Override
    public long drop() throws IOException {
        return 0;
    }

    @Override
    public long createTemplate(String ontology, String schemaProvider) {
        return 0;
    }

    @Override
    public long createIndices(String ontology, String schemaProvider)  {
        return 0;
    }
    //endregion
}
