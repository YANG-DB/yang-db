package com.yangdb.fuse.asg.validation;

/*-
 *
 * fuse-asg
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
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
 *
 */

import com.yangdb.fuse.model.asgQuery.AsgEBase;
import com.yangdb.fuse.model.asgQuery.AsgQueryUtil;
import com.yangdb.fuse.model.Utils;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.asgQuery.AsgStrategyContext;
import com.yangdb.fuse.model.query.EBase;
import com.yangdb.fuse.model.query.entity.EEntityBase;
import com.yangdb.fuse.model.query.properties.EPropGroup;
import com.yangdb.fuse.model.query.properties.RelPropGroup;
import com.yangdb.fuse.model.validation.ValidationResult;

import javax.management.relation.Relation;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.yangdb.fuse.model.validation.ValidationResult.OK;

public class AsgStepsValidatorStrategy implements AsgValidatorStrategy {

    public static final String ERROR_1 = "Ontology Contains two adjacent Entities without relation inside ";
    public static final String ERROR_2 = "Ontology Contains two adjacent Relations without Entity inside ";


    public static final String REL = Relation.class.getSimpleName();
    public static final String REL_PROP = RelPropGroup.class.getSimpleName();

    public static final String ENTITY = EEntityBase.class.getSimpleName();
    public static final String ENTITY_PROPS = EPropGroup.class.getSimpleName();


    public static final String FULL_STEP_DOUBLE_REL = REL+"((:"+REL_PROP+")?):"+REL+"((:"+REL_PROP+")?)";

    public static final String FULL_STEP_DOUBLE_ENTITY = ENTITY+"((:"+ENTITY_PROPS+")?):"+ENTITY+"((:"+ENTITY_PROPS+")?)";

    @Override
    public ValidationResult apply(AsgQuery query, AsgStrategyContext context) {
        List<String> errors = new ArrayList<>();
        List<List<AsgEBase<? extends EBase>>> lists = AsgQueryUtil.flattenQuery(query);
        List<String> patterns = AsgQueryUtil.patterns(lists);
        patterns.forEach(pattern -> {
            Optional<String> match1 = Utils.match(pattern, FULL_STEP_DOUBLE_ENTITY);
            if (match1.isPresent())
                errors.add(ERROR_1 + ":" + match1.get());

            Optional<String> match2 = Utils.match(pattern, FULL_STEP_DOUBLE_REL);
            if (match2.isPresent())
                errors.add(ERROR_2 + ":" + match2.get());
        });

        if (errors.isEmpty())
            return OK;

        return new ValidationResult(false, this.getClass().getSimpleName(),errors.toArray(new String[errors.size()]));
    }
    //endregion
}
