package com.yangdb.fuse.pgql;

/*-
 * #%L
 * fuse-dv-lang
 * %%
 * Copyright (C) 2016 - 2021 The YangDb Graph Database Project
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

import com.yangdb.fuse.dispatcher.validation.QueryValidator;
import com.yangdb.fuse.model.validation.ValidationResult;
import oracle.pgql.lang.ir.PgqlStatement;

/**
 * The purpose of the PGQL query validator is to make sure all the needed fields / aliases / keys and such exist in the
 * query for the purpose of transforming it into a valid Yang.DB query - may it be a DDL or DML statements
 */
public class PgqlQueryValidator implements QueryValidator<PgqlStatement> {

    @Override
    public ValidationResult validate(PgqlStatement query) {
        //todo add the following validations
        // DDL statements :
        //   make sure all column keys exist and not infered by the underlying schema
        //   make sure all properties has valid types

        return ValidationResult.OK;
    }
}
