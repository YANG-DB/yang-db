package com.yangdb.fuse.asg;

/*-
 * #%L
 * fuse-asg
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



import com.google.inject.Inject;
import com.yangdb.fuse.asg.translator.cypher.CypherTranslator;
import com.yangdb.fuse.dispatcher.query.QueryTransformer;
import com.yangdb.fuse.model.asgQuery.AsgQuery;

/**
 * Created by Roman on 12/15/2017.
 */
public class AsgCypherTransformer implements QueryTransformer<String, AsgQuery> {
    //region Constructors
    @Inject
    public AsgCypherTransformer(CypherTranslator cypherTranslator) {
        this.cypherTranslator = cypherTranslator;
    }
    //endregion

    //region QueryTransformer Implementation
    @Override
    public AsgQuery transform(String query) {
        return cypherTranslator.translate(query);
    }
    //endregion

    //region Fields
    private CypherTranslator cypherTranslator;
    //endregion
}
