package com.kayhut.fuse.asg.validation;

/*-
 * #%L
 * fuse-asg
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

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.entity.Typed;
import com.kayhut.fuse.model.validation.ValidationResult;

import java.util.ArrayList;
import java.util.List;

import static com.kayhut.fuse.model.asgQuery.AsgQueryUtil.nextDescendants;
import static com.kayhut.fuse.model.validation.ValidationResult.OK;

public class AsgOntologyEntityValidatorStrategy implements AsgValidatorStrategy {
    public static final String ERROR_1 = "Ontology not containing Entity type ";
    public static final String ERROR_2 = "Ontology not containing Relation type ";

    @Override
    public ValidationResult apply(AsgQuery query, AsgStrategyContext context) {
        List<String> errors = new ArrayList<>();
        Ontology.Accessor accessor = context.getOntologyAccessor();

        List<AsgEBase<EBase>> list = nextDescendants(query.getStart(),
                e -> EEntityBase.class.isAssignableFrom(e.geteBase().getClass()) || e.geteBase() instanceof Rel,
                asgEBase -> true);

        list.forEach(e-> {
            EBase eBase = e.geteBase();
            if(Typed.eTyped.class.isAssignableFrom(eBase.getClass())) {
                if (!accessor.$entity(((Typed.eTyped) eBase).geteType()).isPresent())   errors.add(ERROR_1 + "-" + ((Typed.eTyped) eBase).geteType());
            } else if(Typed.rTyped.class.isAssignableFrom(eBase.getClass())) {
                if (!accessor.$relation(((Typed.rTyped) eBase).getrType()).isPresent()) errors.add(ERROR_2 + ":" + ((Typed.rTyped) eBase).getrType());
            }
        });
        if(errors.isEmpty())
            return OK;

        return new ValidationResult(false, this.getClass().getSimpleName(),errors.toArray(new String[errors.size()]));
    }
    //endregion
}
