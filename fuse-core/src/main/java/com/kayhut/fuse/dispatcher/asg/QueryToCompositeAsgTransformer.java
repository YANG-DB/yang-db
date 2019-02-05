package com.kayhut.fuse.dispatcher.asg;

/*-
 * #%L
 * fuse-core
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

import com.google.inject.Inject;
import com.kayhut.fuse.dispatcher.asg.builder.BNextFactory;
import com.kayhut.fuse.dispatcher.asg.builder.NextEbaseFactory;
import com.kayhut.fuse.dispatcher.ontology.OntologyProvider;
import com.kayhut.fuse.dispatcher.query.QueryTransformer;
import com.kayhut.fuse.model.asgQuery.AsgCompositeQuery;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.ontology.Property;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.ParameterizedQuery;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import com.kayhut.fuse.model.query.properties.constraint.InnerQueryConstraint;
import com.kayhut.fuse.model.query.properties.constraint.NamedParameter;
import com.kayhut.fuse.model.query.properties.constraint.ParameterizedConstraint;
import javaslang.collection.Stream;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.kayhut.fuse.model.asgQuery.AsgQueryUtil.getEprops;
import static com.kayhut.fuse.model.asgQuery.AsgQueryUtil.getRelProps;

/**
 * Created by liorp on 12/15/2017.
 */
public class QueryToCompositeAsgTransformer extends QueryToAsgTransformer {
    private OntologyProvider ontologyProvider;

    //region Constructors
    @Inject
    public QueryToCompositeAsgTransformer(OntologyProvider ontologyProvider) {
        super();
        this.ontologyProvider = ontologyProvider;
    }
    //endregion

    //region QueryTransformer Implementation
    @Override
    public AsgCompositeQuery transform(Query query) {
        AsgCompositeQuery asgQuery = new AsgCompositeQuery(super.transform(query));
        Optional<Ontology> ontology = ontologyProvider.get(query.getOnt());
        apply(asgQuery, new AsgStrategyContext(new Ontology.Accessor(ontology.get())));
        return asgQuery;
    }
    //endregion


    public void apply(AsgQuery query, AsgStrategyContext context) {
        getEprops(query).stream()
                .filter(prop -> prop.getCon() != null)
                .forEach(eProp -> applyExpressionTransformation(query, context, eProp, EProp.class));

        getRelProps(query).stream()
                .filter(prop -> prop.getCon() != null)
                .forEach(relProp -> applyExpressionTransformation(query, context, relProp, RelProp.class));
    }

    //region Private Methods

    private void applyExpressionTransformation(AsgQuery query, AsgStrategyContext context, EBase eBase, Class klass) {
        if (klass == EProp.class) {
            EProp eProp = (EProp) eBase;
            Optional<Property> property = context.getOntologyAccessor().$property(eProp.getpType());

            Constraint con = eProp.getCon();
            if (property.isPresent() && isInnerQuery(con) && !ParameterizedConstraint.class.isAssignableFrom(eProp.getCon().getClass())) {
                Query innerQuery = ((InnerQueryConstraint) con.getExpr()).getInnerQuery();
                String name = innerQuery.getName();
                Constraint newCon = new ParameterizedConstraint(con.getOp(), new NamedParameter(name));
                eProp.setCon(newCon);
                //add inner query to chain
                AsgQuery innerAsgQuery = new AsgCompositeQuery(super.transform(innerQuery));
                ((AsgCompositeQuery) query).with(innerAsgQuery);
                apply(innerAsgQuery, context);
            }
        }
        if (klass == RelProp.class) {
            RelProp relProp = (RelProp) eBase;
            Optional<Property> property = context.getOntologyAccessor().$property(relProp.getpType());
            if (relProp.getCon() != null) {
                Constraint con = relProp.getCon();
                if (property.isPresent() && isInnerQuery(con) && !ParameterizedConstraint.class.isAssignableFrom(relProp.getCon().getClass())) {
                    Query innerQuery = ((InnerQueryConstraint) con.getExpr()).getInnerQuery();
                    String name = innerQuery.getName();
                    Constraint newCon = new ParameterizedConstraint(con.getOp(), new NamedParameter(name));
                    relProp.setCon(newCon);
                    //add inner query to chain
                    AsgQuery innerAsgQuery = new AsgCompositeQuery(super.transform(innerQuery));
                    ((AsgCompositeQuery) query).with(innerAsgQuery);
                    apply(innerAsgQuery, context);
                }
            }
        }
    }

    private boolean isInnerQuery(Constraint op) {
        return InnerQueryConstraint.class.isAssignableFrom(op.getExpr().getClass());
    }

}
