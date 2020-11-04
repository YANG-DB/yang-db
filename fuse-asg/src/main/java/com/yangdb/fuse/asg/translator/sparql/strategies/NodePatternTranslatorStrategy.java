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
import com.yangdb.fuse.model.ontology.DirectiveType;
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
import com.yangdb.fuse.model.query.quant.QuantBase;
import com.yangdb.fuse.model.query.quant.QuantType;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import javaslang.Tuple2;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.algebra.*;
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
        if (StatementPattern.class.isAssignableFrom(element.getClass())) {
            //parent context - Union is SOME, join is ALL
            QuantType parent = Union.class.isAssignableFrom(element.getParentNode().getClass()) ?
                    QuantType.some : QuantType.all;
            //actual statement
            Var subjectVar = ((StatementPattern) element).getSubjectVar();
            if (subjectVar != null) {
                //subject represents the entity - if not anonymous - tag will identify the subject
                subjectEval(subjectVar, query, context, parent, o -> o);
            }

            Var objectVar = ((StatementPattern) element).getObjectVar();
            Var predicateVar = ((StatementPattern) element).getPredicateVar();
            if (predicateVar != null) {
                //preidate represents the semantic relation  between the subject & object
                predicateEval(predicateVar, objectVar, query, context, parent, o -> o);
            }

            Var contextVar = ((StatementPattern) element).getContextVar();
            if (contextVar != null) {
                //todo - eval context & infer relevancy
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
    private void predicateEval(Var predicate, Var object, AsgQuery query, SparqlStrategyContext context, QuantType parentQuantType, Function function) {
        //named var
        if (predicate.isAnonymous()) {
            //blank node
            Value value = predicate.getValue();
            Optional<Tuple2<Ontology.Accessor.NodeType, String>> result = getNodeType(context, value);
            String type = result.get()._2;
            switch (result.get()._1) {
                case ENTITY:
                    //add query with entity type element
                    entity(predicate,object, query, context, parentQuantType, type);
                    break;
                //add query with relationship type element
                case RELATION:
                    relation(predicate,object, query, context, parentQuantType, type);
                    break;
                case PROPERTY:
                    //add query with property type element
                    property(predicate,object, query, context, parentQuantType, type);
                    break;
            }
        } else {
            //constant value
            if (predicate.isConstant()) {
                //todo - value node
            } else {
                //parameterized node
                String name = predicate.getName();
                //set current working scope
                context.scope(getOrCreateQueryEntityContext(query, context, name));
            }
        }


    }

    /**
     * extract node class-type and named types
     * @param context
     * @param value
     * @return
     */
    private Optional<Tuple2<Ontology.Accessor.NodeType, String>> getNodeType(SparqlStrategyContext context, Value value) {
        Optional<Tuple2<Ontology.Accessor.NodeType, String>> result = context.getOntology().matchNameToType(value.stringValue());
        //fetch relevant node type & name
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
                //check directives - this are the basic directives which are not part of the domain but a language definition specs
                if (context.getOntology().$directive(name).isPresent()) {
                    DirectiveType directiveType = context.getOntology().$directive$(name);
                    switch (directiveType.getType()) {
                        case RESOURCE:
                            break;
                        case PROPERTY:
                            //these are graph vertices properties
                            result = Optional.of(new Tuple2<>(Ontology.Accessor.NodeType.PROPERTY, name));
                            break;
                    }
                }
            }
            if (!result.isPresent())
                //todo - warning no appropriate ontology type was found for the predicate ...
                throw new FuseError.FuseErrorException(new FuseError("Schema query mismatch", "No predicate found for " + value + " in ontology  " + context.getOntology().name()));

        }
        return result;
    }

    /**
     * add entity node to query
     *
     * @param predicate
     * @param object
     * @param query
     * @param context
     * @param parentQuantType
     * @param type
     */
    private void entity(Var predicate, Var object, AsgQuery query, SparqlStrategyContext context, QuantType parentQuantType, String type) {
        String eType = type;
        //add OR update untyped to typed to context entity
        if (EUntyped.class.isAssignableFrom(context.getScope().geteBase().getClass())) {
            AsgQueryUtil.replace(context.getScope(),
                    new AsgEBase<>(new ETyped((EEntityBase) context.getScope().geteBase(), eType)));
        } else if (ETyped.class.isAssignableFrom(context.getScope().geteBase().getClass())) {
            //set the type
            ((ETyped) context.getScope().geteBase()).seteType(eType);
        }
    }

    /**
     * add relationship node to query
     *
     * @param predicate
     * @param object
     * @param query
     * @param context
     * @param parentQuantType
     * @param type
     */
    private void relation(Var predicate, Var object, AsgQuery query, SparqlStrategyContext context, QuantType parentQuantType, String type) {
        //add relationship to context entity
        String rType = type;
        //create relationship & add the PropGroup with the property
        //set current working scope (Econcrete type)
        AsgEBase<EBase> node = AsgQueryUtil.getByTag(query.getStart(), object.getName())
                .orElseGet(() -> {
                    //add relation to context entity:
                    // ** - if current context is not a quant - first add quant and than add the relation
                    AsgEBase<? extends EBase> quant = SparqlUtils.quant(context.getScope(), query, context, parentQuantType);
                    //todo verify the quant
                    ((QuantBase) quant.geteBase()).setqType(parentQuantType);
                    //if relation does not exist - create it & add to quant
                    quant.addNext(new AsgEBase<>(new Rel(AsgQueryUtil.max(query) + 1, rType, Rel.Direction.R, predicate.getName(), -1)));
                    //return newly generated element
                    return AsgQueryUtil.getByTag(query.getStart(), predicate.getName()).get();
                });
        context.scope(node);

        //relation object (other side) will be evaluated as a subject
        subjectEval(object, query, context, QuantType.all, o -> o);
    }

    /**
     * add property node to query
     *
     * @param predicate
     * @param object
     * @param query
     * @param context
     * @param parentQuantType
     * @param type
     */
    private void property(Var predicate, Var object, AsgQuery query, SparqlStrategyContext context, QuantType parentQuantType, String type) {
        AsgEBase<? extends EBase> quant;
        int current;
        String pType = type;
        //add property to context entity:
        // ** - if current context is not a quant - first add quant and than add the property
        quant = SparqlUtils.quant(context.getScope(), query, context, parentQuantType);
        //create the property
        EProp eProp = new EProp(AsgQueryUtil.max(query) + 1, pType, new IdentityProjection());
        //verify group's quant type compared to parentQuantType
        if (AsgQueryUtil.nextAdjacentDescendants(quant, EPropGroup.class).stream().anyMatch(g -> ((EPropGroup) g.geteBase()).getQuantType().equals(parentQuantType))) {
            AsgEBase<EBase> group = AsgQueryUtil.nextAdjacentDescendants(quant, EPropGroup.class).stream()
                    .filter(g -> ((EPropGroup) g.geteBase()).getQuantType().equals(parentQuantType))
                    .findFirst().get();
            //add the PropGroup with the property
            ((EPropGroup) group.geteBase()).addIsNoneExist(eProp);
        } else {
            //create a new group with the appropriate quant type
            quant.addNext(new AsgEBase<>(new EPropGroup(AsgQueryUtil.max(query) + 2, parentQuantType, eProp)));
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
    }

    /**
     * subject represents the abstract node element
     *
     * @param var
     * @param query
     * @param context
     * @param function
     */
    public void subjectEval(Var var, AsgQuery query, SparqlStrategyContext context, QuantType parentQuantType, Function function) {
        if (var.isAnonymous()) {
            //blank node  a concrete entity
            Value value = var.getValue();
            if (value != null) {
                //set current working scope (Econcrete type)
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
            } else {
                //set current working scope
                context.scope(getOrCreateQueryEntityContext(query, context, var.getName()));
            }
        } else {
            if (var.isConstant()) {
                //value node
            } else {
                //parameterized node
                //set current working scope
                context.scope(getOrCreateQueryEntityContext(query, context, var.getName()));
            }
        }
    }

    /**
     * @param query
     * @param context
     * @param name
     * @return
     */
    public AsgEBase<EBase> getOrCreateQueryEntityContext(AsgQuery query, SparqlStrategyContext context, String name) {
        return AsgQueryUtil.getByTag(query.getStart(), name).orElseGet(() -> {
            //generate new element - currently unTyped
            int index = AsgQueryUtil.maxEntityNum(context.getQuery()) + 1;
            // type not determined yet - this is the RDF nature...
            EEntityBase element = new ETyped(index, name, context.getOntology().eType$(THING), -1, -1);
            context.getScope().addNext(new AsgEBase<>(element));
            //return newly generated element
            return AsgQueryUtil.getByTag(query.getStart(), name).get();
        });
    }
}
