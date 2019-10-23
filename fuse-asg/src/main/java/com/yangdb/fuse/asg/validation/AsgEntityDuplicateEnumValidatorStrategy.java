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



import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.asgQuery.AsgQueryUtil;
import com.yangdb.fuse.model.asgQuery.AsgStrategyContext;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.entity.EEntityBase;
import com.yangdb.fuse.model.query.quant.QuantBase;
import com.yangdb.fuse.model.validation.ValidationResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.yangdb.fuse.model.asgQuery.AsgQueryUtil.nextDescendants;
import static com.yangdb.fuse.model.validation.ValidationResult.OK;
import static java.util.stream.Collectors.groupingBy;

public class AsgEntityDuplicateEnumValidatorStrategy implements AsgValidatorStrategy {
    public static final String ERROR_1 = "Enum appears in more than one entity";

    @Override
    public ValidationResult apply(AsgQuery query, AsgStrategyContext context) {
        List<String> errors = new ArrayList<>();

        Map<Integer, List<Integer>> collect = AsgQueryUtil.eNums(query, asgEBase ->
                EEntityBase.class.isAssignableFrom(asgEBase.geteBase().getClass()) ||
                        Rel.class.isAssignableFrom(asgEBase.geteBase().getClass()) ||
                        QuantBase.class.isAssignableFrom(asgEBase.geteBase().getClass()))
                            .stream().collect(groupingBy(Function.identity()));

        List<Map.Entry<Integer, List<Integer>>> inValid = collect.entrySet().stream().filter(v -> v.getValue().size() > 1)
                .collect(Collectors.toList());

        if (inValid.isEmpty())
            return OK;

        errors.add(ERROR_1 + ":" + inValid.stream().map(Map.Entry::getKey).collect(Collectors.toList()));

        return new ValidationResult(false, this.getClass().getSimpleName(), errors.toArray(new String[errors.size()]));
    }
    //endregion
}
