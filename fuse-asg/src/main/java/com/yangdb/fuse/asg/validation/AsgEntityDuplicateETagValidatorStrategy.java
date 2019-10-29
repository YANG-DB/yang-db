package com.yangdb.fuse.asg.validation;

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



import com.yangdb.fuse.model.Tagged;
import com.yangdb.fuse.model.asgQuery.AsgEBase;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.asgQuery.AsgQueryUtil;
import com.yangdb.fuse.model.asgQuery.AsgStrategyContext;
import com.yangdb.fuse.model.query.EBase;
import com.yangdb.fuse.model.query.entity.Typed;
import com.yangdb.fuse.model.validation.ValidationResult;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.yangdb.fuse.model.validation.ValidationResult.OK;
import static java.util.stream.Collectors.groupingBy;

public class AsgEntityDuplicateETagValidatorStrategy implements AsgValidatorStrategy {
    public static final String ERROR_1 = "ETag %s appears in more than one entity with a different type (label)";

    @Override
    public ValidationResult apply(AsgQuery query, AsgStrategyContext context) {
        List<String> errors = new ArrayList<>();

        Map<String, List<AsgEBase<EBase>>> map = AsgQueryUtil.groupByTags(query.getStart());

        map.entrySet().stream()
                .filter(e -> e.getValue().size() > 1)
                .forEach(e -> {
                    if(e.getValue()
                            .stream()
                            .filter(v->v.geteBase() instanceof Typed)
                            .collect(groupingBy(o -> ((Typed) o.geteBase()).getTyped())).size() > 1) {
                        errors.add(String.format(ERROR_1,e.getKey()));
                    }
                });

        if (errors.isEmpty())
            return OK;


        return new ValidationResult(false, this.getClass().getSimpleName(), errors.toArray(new String[errors.size()]));
    }
    //endregion
}
