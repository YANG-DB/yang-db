package com.yangdb.fuse.pgql;

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
