package com.kayhut.fuse.asg.validation;

/*-
 * #%L
 * fuse-asg
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

import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgQueryUtil;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.quant.QuantBase;
import com.kayhut.fuse.model.validation.ValidationResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.kayhut.fuse.model.validation.ValidationResult.OK;
import static java.util.stream.Collectors.groupingBy;

public class AsgEntityDuplicateETagValidatorStrategy implements AsgValidatorStrategy {
    public static final String ERROR_1 = "ETag appears in more than one entity";

    @Override
    public ValidationResult apply(AsgQuery query, AsgStrategyContext context) {
        List<String> errors = new ArrayList<>();

        Map<String, List<String>> collect = AsgQueryUtil.eTags(query)
                            .stream()
                            .filter(Objects::nonNull)
                            .collect(groupingBy(Function.identity()));

        List<Map.Entry<String, List<String>>> inValid = collect.entrySet().stream().filter(v -> v.getValue().size() > 1)
                .collect(Collectors.toList());

        if (inValid.isEmpty())
            return OK;

        errors.add(ERROR_1 + ":" + inValid.stream().map(Map.Entry::getKey).collect(Collectors.toList()));

        return new ValidationResult(false, this.getClass().getSimpleName(), errors.toArray(new String[errors.size()]));
    }
    //endregion
}
