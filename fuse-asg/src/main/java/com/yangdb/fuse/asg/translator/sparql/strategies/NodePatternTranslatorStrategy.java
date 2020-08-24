package com.yangdb.fuse.asg.translator.sparql.strategies;

/*-
 * #%L
 * fuse-asg
 * %%
 * Copyright (C) 2016 - 2020 The YangDb Graph Database Project
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

import com.yangdb.fuse.model.asgQuery.AsgEBase;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.asgQuery.AsgQueryUtil;
import com.yangdb.fuse.model.ontology.Ontology;
import com.yangdb.fuse.model.ontology.OntologyNameSpace;
import com.yangdb.fuse.model.ontology.Property;
import com.yangdb.fuse.model.query.EBase;
import com.yangdb.fuse.model.query.Rel;
import com.yangdb.fuse.model.query.entity.EConcrete;
import com.yangdb.fuse.model.query.entity.EEntityBase;
import com.yangdb.fuse.model.query.entity.ETyped;
import com.yangdb.fuse.model.query.entity.EUntyped;
import com.yangdb.fuse.model.query.properties.EProp;
import com.yangdb.fuse.model.query.properties.EPropGroup;
import com.yangdb.fuse.model.query.properties.constraint.Constraint;
import com.yangdb.fuse.model.query.properties.constraint.ConstraintOp;
import com.yangdb.fuse.model.query.properties.projection.IdentityProjection;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import javaslang.Tuple2;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.algebra.StatementPattern;
import org.eclipse.rdf4j.query.algebra.TupleExpr;
import org.eclipse.rdf4j.query.algebra.Var;
import org.semanticweb.owlapi.model.IRI;

import java.util.Optional;
import java.util.function.Function;

import static com.yangdb.fuse.model.query.properties.constraint.Constraint.of;

/**
 * Todo - Work in progress ...
 */
public class NodePatternTranslatorStrategy implements SparqlElementTranslatorStrategy {

    public static final String THING = "http://www.w3.org/2002/07/owl#Thing";

    @Override
    public void apply(TupleExpr element, AsgQuery query, SparqlStrategyContext context) {
        //todo verify node creation (type & entity)
        if (StatementPattern.class.isAssignableFrom(element.getClass())) {
            Var subjectVar = ((StatementPattern) element).getSubjectVar();
            if (subjectVar != null) {
                //subject represents the entity - if not anonymous - tag will identify the subject
                subjectEval(subjectVar, query, context, o -> o);
            }

            Var objectVar = ((StatementPattern) element).getObjectVar();
            Var predicateVar = ((StatementPattern) element).getPredicateVar();
            if (predicateVar != null) {
                //preidate represents the semantic relation  between the subject & object
                predicateEval(predicateVar, objectVar, query, context, o -> o);
            }

            Var contextVar = ((StatementPattern) element).getContextVar();
            if (contextVar != null) {
//                eval(contextVar, query, context, o -> o);
            }
        }
    }

    /**
     * predicate represents the indication of the semantics for the abstract node - such as
     * - node's type
     * - node's property
     * - node's relationship
     *
     * @param predicate
     * @param object
     * @param query
     * @param context
     * @param function
     */
    private void predicateEval(Var predicate, Var object, AsgQuery query, SparqlStrategyContext context, Function function) {
        if (predicate.isAnonymous()) {
            //blank node
            //todo
            Value value = predicate.getValue();
            Optional<Tuple2<Ontology.Accessor.NodeType, String>> result = context.getOntology().matchNameToType(value.stringValue());
            if (!result.isPresent()) {
                result = context.getOntology().matchNameToType(IRI.create(value.stringValue()).getRemainder()
                        .or(value.stringValue()));

            }
            if (!result.isPresent()) {
                //verify if value belongs to ontology namespaces
                if (OntologyNameSpace.inside(value.stringValue())) {
                    String name = IRI.create(value.stringValue()).getRemainder().get();
                    //check ontology properties for matching property pType
                    Optional<Property> property = context.getOntology().properties().stream().filter(prop -> OntologyNameSpace.reminder(prop.getpType()).equals(name)).findFirst();
                    if (property.isPresent()) {
                        result = Optional.of(new Tuple2<>(Ontology.Accessor.NodeType.PROPERTY, property.get().getpType()));
                    }
                }
                if (!result.isPresent())
                    //todo - warning no appropriate ontology type was found for the predicate ...
                    throw new FuseError.FuseErrorException(new FuseError("Schema query mismatch", "No predicate found for " + value + " in ontology  " + context.getOntology().name()));

            } else {
                String type = result.get()._2;
                switch (result.get()._1) {
                    case ENTITY:
                        entity(object, query, context, type);
                        break;
                    case RELATION:
                        relation(object, query, context, type);
                        break;
                    case PROPERTY:
                        property(object, query, context, type);
                        break;
                }
            }
        } else {
            if (predicate.isConstant()) {
                //value node
                //todo
                Value value = predicate.getValue();
            } else {
                //parameterized node
                String name = predicate.getName();
                //set current working scope
                context.scope(AsgQueryUtil.getByTag(query.getStart(), name).orElseGet(() -> {
                    //generate new element - currently unTyped
                    int index = AsgQueryUtil.maxEntityNum(context.getQuery()) + 1;
                    //default type is the RDF basic Thing type
                    EEntityBase element = new ETyped(index, name, context.getOntology().eType$(THING), -1, -1);
                    context.getScope().addNext(new AsgEBase<>(element));
                    //return newly generated element
                    return AsgQueryUtil.getByTag(query.getStart(), name).get();
                }));
            }
        }


    }

    private void entity(Var object, AsgQuery query, SparqlStrategyContext context, String type) {
        String eType = type;
        //add OR update untyped to typed to context entity
        if (EUntyped.class.isAssignableFrom(context.getScope().geteBase().getClass())) {
            AsgQueryUtil.replace(context.getScope(),
                    new AsgEBase<>(new ETyped((EEntityBase) context.getScope().geteBase(), eType)));
        } else if (ETyped.class.isAssignableFrom(context.getScope().geteBase().getClass())) {
            //set the type
            ((ETyped) context.getScope().geteBase()).seteType(eType);
        }
        return;
    }

    private void relation(Var object, AsgQuery query, SparqlStrategyContext context, String type) {
        //add relationship to context entity
        // if current context is not a quant - first add quant and than add the property
        String rType = type;
        //add relation to context entity:
        // ** - if current context is not a quant - first add quant and than add the relation
        AsgEBase<? extends EBase> quant = SparqlUtils.quant(context.getScope(), Optional.empty(), query, context);
        int current = Math.max(quant.getNext().stream().mapToInt(p -> p.geteNum()).max().orElse(0), quant.geteNum());
        //create relationship & add the PropGroup with the property
        AsgEBase<Rel> node = new AsgEBase<>(new Rel(current + 1, rType, Rel.Direction.R, null, -1));
        quant.addNext(node);
        context.scope(node);

        //relation object (other side) will be evaluated as a subject
        subjectEval(object, query, context, o -> o);
        return;
    }

    private void property(Var object, AsgQuery query, SparqlStrategyContext context, String type) {
        AsgEBase<? extends EBase> quant;
        int current;
        String pType = type;
        //add property to context entity:
        // ** - if current context is not a quant - first add quant and than add the property
        quant = SparqlUtils.quant(context.getScope(), Optional.empty(), query, context);
        current = Math.max(quant.getNext().stream().mapToInt(p -> p.geteNum()).max().orElse(0), quant.geteNum());
        //create the property
        EProp eProp = new EProp(current + 2, pType, new IdentityProjection());
        //add the PropGroup with the property
        if (!AsgQueryUtil.nextAdjacentDescendant(quant, EPropGroup.class).isPresent()) {
            quant.addNext(new AsgEBase<>(new EPropGroup(current + 1, eProp)));
        } else {
            //add the property to the group
            ((EPropGroup) AsgQueryUtil.nextAdjacentDescendant(quant, EPropGroup.class).get().geteBase())
                    .getProps().add(eProp);
        }

        //check object to verify its tagged name
        if (!object.isAnonymous()) {
            //if object is a node - set its name on property
            eProp.setpTag(object.getName());
        } else {
            //if object is constant it should be compared with property
            Value objValue = object.getValue();
            eProp.setCon(new Constraint(ConstraintOp.eq, objValue.stringValue()));
        }
        return;
    }

    /**
     * subject represents the abstract node element
     *
     * @param var
     * @param query
     * @param context
     * @param function
     */
    public void subjectEval(Var var, AsgQuery query, SparqlStrategyContext context, Function function) {
        if (var.isAnonymous()) {
            //blank node  a concrete entity
            Value value = var.getValue();
            //set current working scope
            context.scope(AsgQueryUtil.getByTag(query.getStart(), value.stringValue()).orElseGet(() -> {
                //generate new element - currently unTyped
                int index = AsgQueryUtil.maxEntityNum(context.getQuery()) + 1;
                // type not determined yet - this is the RDF nature...
                EEntityBase element = new EConcrete(index, value.stringValue(), context.getOntology().eType$(THING),
                        value.stringValue(), value.stringValue(), -1, -1);
                context.getScope().addNext(new AsgEBase<>(element));
                //return newly generated element
                return AsgQueryUtil.getByTag(query.getStart(), value.stringValue()).get();
            }));

            //todo
        } else {
            if (var.isConstant()) {
                //value node
                Value value = var.getValue();
                //todo
            } else {
                //parameterized node
                String name = var.getName();
                //set current working scope
                context.scope(AsgQueryUtil.getByTag(query.getStart(), name).orElseGet(() -> {
                    //generate new element - currently unTyped
                    int index = AsgQueryUtil.maxEntityNum(context.getQuery()) + 1;
                    // type not determined yet - this is the RDF nature...
                    EEntityBase element = new ETyped(index, name, context.getOntology().eType$(THING), -1, -1);
                    context.getScope().addNext(new AsgEBase<>(element));
                    //return newly generated element
                    return AsgQueryUtil.getByTag(query.getStart(), name).get();
                }));
            }
        }
    }
}
