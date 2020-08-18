package com.yangdb.fuse.dispatcher.query.rdf;

/*-
 * #%L
 * fuse-core
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

import com.google.common.collect.Sets;
import com.yangdb.fuse.dispatcher.ontology.OntologyTransformerIfc;
import com.yangdb.fuse.model.ontology.*;
import com.yangdb.fuse.model.ontology.EntityType;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import org.apache.commons.lang.NotImplementedException;
import org.openrdf.rio.RDFFormat;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.StringDocumentSource;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;

import java.util.*;
import java.util.stream.Collectors;

import static com.yangdb.fuse.model.ontology.Ontology.OntologyBuilder.YANGDB_ORG;

/**
 * transform OWL RDF ontology schema to YangDB ontology support
 */
public class OWL2OntologyTransformer implements OntologyTransformerIfc<Set<String>, Ontology> {
    private OWLOntologyManager manager;
    private OWLOntologyLoaderConfiguration config;
    private IRI root;

    public OWL2OntologyTransformer() {
        root = IRI.create(YANGDB_ORG);
        config = new OWLOntologyLoaderConfiguration();
        manager = createOwlOntologyManager(config);
    }

    public OWLOntologyManager getManager() {
        return manager;
    }

    public IRI getRoot() {
        return root;
    }

    public void setRoot(IRI root) {
        this.root = root;
    }

    @Override
    /**
     * load owl ontologies -
     *  the order of the ontologies is important in regards with the owl dependencies
     */
    public Ontology transform(Set<String> source) {
        Ontology.OntologyBuilder builder = Ontology.OntologyBuilder.anOntology();
        return importOWL( builder, source).build();
    }

    @Override
    public Set<String> translate(Ontology source) {
        throw new NotImplementedException("");
    }

    public OWLOntologyManager createOwlOntologyManager(
            OWLOntologyLoaderConfiguration config
    ) {
        this.manager = OWLManager.createOWLOntologyManager();
        config.setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT);
        return manager;
    }

    public Ontology.OntologyBuilder importOWL(
            Ontology.OntologyBuilder builder,
            Set<String> owls) {
        try {
            //set ontology name
            builder.withOnt(root.toString());

            owls.stream().map(owl -> {
                try {
                    return populate(builder, manager.loadOntologyFromOntologyDocument(new StringDocumentSource(owl, root), config));
                } catch (OWLOntologyCreationException e) {
                    e.printStackTrace();
                }
                return null;
            }).filter(Objects::nonNull).collect(Collectors.toSet());
        } catch (Exception err) {
            throw new FuseError.FuseErrorException(new FuseError(err.getMessage(), err));
        }
        return builder;
    }

    /**
     * populate yangDb ontology according to OWL ontology structure
     *
     * @param builder
     * @param o
     * @return
     */
    private OWLOntology populate(Ontology.OntologyBuilder builder, OWLOntology o) {
        OWLReasoner reasoner = new StructuralReasonerFactory().createReasoner(o);
        importOntologyClasses(builder, o, reasoner);
        importObjectProperties(builder, o, reasoner);
        importDataProperties(builder, o, reasoner);
        return o;
    }

    /**
     * import OWL classes (including enum class)
     *
     * @param builder
     * @param o
     * @param reasoner
     */
    private void importOntologyClasses(Ontology.OntologyBuilder builder, OWLOntology o, OWLReasoner reasoner) {
        //abstract classes
        for (OWLClass ontologyClass : o.getClassesInSignature()) {
            String type = ontologyClass.getIRI().getRemainder().or(ontologyClass.getIRI().toString());
            //ToDo add hirarchy information to yangDb ontology
            //information used to infer structural inheritance
            NodeSet<OWLClass> subClasses = reasoner.getSubClasses(ontologyClass, true);
            NodeSet<OWLClass> superClasses = reasoner.getSuperClasses(ontologyClass, true);
            NodeSet<OWLNamedIndividual> instances = reasoner.getInstances(ontologyClass, true);

            //enum filter
            Optional<OWLAxiom> enumAxiom = o.getAxioms().stream()
                    .filter(ax -> ax.isOfType(Sets.newHashSet(AxiomType.EQUIVALENT_CLASSES))) // get classes
                    .filter(ax -> ax.getClassesInSignature().contains(ontologyClass)) //verify same class as current
                    .filter(ax -> !ax.getNestedClassExpressions().isEmpty())
                    .findFirst();

            if (enumAxiom.isPresent()) {
                Optional<OWLClassExpression> expression = ((OWLEquivalentClassesAxiom) enumAxiom.get()).getClassExpressions().stream()
                        .filter(exp -> exp instanceof OWLObjectOneOf)
                        .findFirst();
                if (expression.isPresent()) {
                    List<String> values = ((OWLObjectOneOf) expression.get()).getIndividuals().stream()
                            .map(el -> el.asOWLNamedIndividual().getIRI().getRemainder().or(el.asOWLNamedIndividual().toStringID()))
                            .collect(Collectors.toList());
                    //enum type class
                    builder.addEnumeratedTypes(EnumeratedType.EnumeratedTypeBuilder.anEnumeratedType()
                            .withEType(type)
                            .values(values)
                            .build());
                }
            } else {
                //simple type class
                //functional actions: if EntityType does not exist create on, add it to builder and continue
                builder.getEntityType(type).orElseGet(
                        () -> builder.addEntityType(
                                EntityType.Builder.get()
                                        .withEType(type)
                                        .withName(type)
                                        .build())
                                .getEntityType(type)
                                .get());
            }
        }
    }

    /**
     * import OWL relationships (objects)
     *
     * @param builder
     * @param o
     * @param reasoner
     */
    private void importObjectProperties(Ontology.OntologyBuilder builder, OWLOntology o, OWLReasoner reasoner) {
        // Relationships
        for (OWLObjectProperty objectProperty : o.getObjectPropertiesInSignature()) {
            if (!o.isDeclared(objectProperty, Imports.EXCLUDED)) {
                continue;
            }
            String type = objectProperty.getIRI().getRemainder().or(objectProperty.getIRI().toString());
            //functional actions: if RelType does not exist create on, add it to builder and continue
            builder.getRelationshipType(type).orElseGet(
                    () -> builder.addRelationshipType(
                            RelationshipType.Builder.get()
                                    .withRType(type)
                                    .withName(type)
                                    .withEPairs(generateRelPairs(o, objectProperty))
                                    .build())
                            .getRelationshipType(type)
                            .get());
        }
    }

    private List<EPair> generateRelPairs(OWLOntology o, OWLObjectProperty objectProperty) {
        List<EPair> pairs = new ArrayList<>();
        List<OWLObjectPropertyDomainAxiom> objectPropertyDomainAxioms = new ArrayList<>(o.getObjectPropertyDomainAxioms(objectProperty));
        //todo add Thing to list ?
        if (objectPropertyDomainAxioms.isEmpty()) return Collections.emptyList();

        List<OWLObjectPropertyRangeAxiom> objectPropertyRangeAxioms = new ArrayList<>(o.getObjectPropertyRangeAxioms(objectProperty));
        //todo add Thing to list ?
        if (objectPropertyRangeAxioms.isEmpty()) return Collections.emptyList();

        //match domain & range pairs
        for (int i = 0; i < objectPropertyDomainAxioms.size(); i++) {
            pairs.addAll(
                    createPair(objectPropertyDomainAxioms.get(i).getDomain(), objectPropertyRangeAxioms.get(i).getRange()));
        }
        return pairs;
    }

    private List<EPair> createPair(OWLClassExpression domain, OWLClassExpression range) {
        //domain == sideA
        Set<String> sideA = new HashSet<>();
        Set<String> sideB = new HashSet<>();

        if (domain.isClassExpressionLiteral()) {
            sideA.add(domain.asOWLClass().getIRI().getRemainder()
                    .or(domain.asOWLClass().getIRI().toString()));
        } else {
            sideA.addAll(domain.asDisjunctSet().stream().map(element ->
                    element.asOWLClass().getIRI().getRemainder()
                            .or(element.asOWLClass().getIRI().toString()))
                    .collect(Collectors.toSet()));
        }

        if (range.isClassExpressionLiteral()) {
            sideB.add(range.asOWLClass().getIRI().getRemainder()
                    .or(range.asOWLClass().getIRI().toString()));
        } else {
            sideB.addAll(range.asDisjunctSet().stream().map(element ->
                    element.asOWLClass().getIRI().getRemainder()
                            .or(element.asOWLClass().getIRI().toString()))
                    .collect(Collectors.toSet()));
        }
        //return cartesian product of the two sides
        return Sets.cartesianProduct(sideA, sideB).stream().map(p -> new EPair(p.get(0), p.get(1))).collect(Collectors.toList());
    }

    /**
     * import OWL properties (class fields)
     *
     * @param builder
     * @param o
     * @return
     */
    private void importDataProperties(Ontology.OntologyBuilder builder, OWLOntology o, OWLReasoner reasoner) {
        //DatatypeProperty - nodes properties
        o.getDataPropertiesInSignature()
                .forEach(dataTypeProperty -> builder.addProperties(generateProperty(builder, o, dataTypeProperty)));
    }

    private List<Property> generateProperty(Ontology.OntologyBuilder builder, OWLOntology o, OWLDataProperty objectProperty) {
        List<Property> properties = new ArrayList<>();
        List<OWLDataPropertyDomainAxiom> dataPropertyDomainAxioms = new ArrayList<>(o.getDataPropertyDomainAxioms(objectProperty));
        List<OWLDataPropertyRangeAxiom> dataPropertyRangeAxioms = new ArrayList<>(o.getDataPropertyRangeAxioms(objectProperty));
        //match domain & range pairs
        for (int i = 0; i < dataPropertyDomainAxioms.size(); i++) {
            String dataType = dataPropertyRangeAxioms.get(i).getRange().asOWLDatatype().getIRI().getRemainder()
                    .or(dataPropertyRangeAxioms.get(i).getRange().asOWLDatatype().getIRI().toString());

            OWLClassExpression domain = dataPropertyDomainAxioms.get(i).getDomain();
            if (domain.isAnonymous()) {
                //todo add union or other axiom
                //domain is a collection of elements
            } else {
                String eType = domain.asOWLClass().getIRI().getRemainder()
                        .or(domain.asOWLClass().getIRI().toString());

                Property property = new Property(
                        objectProperty.getIRI().getRemainder().or(objectProperty.toStringID()),
                        objectProperty.getIRI().getRemainder().or(objectProperty.toStringID()),
                        dataType);
                //
                properties.add(property);
                //add property to class

                //add property to class (eType)
                builder.getEntityType(eType).ifPresent(entityType -> entityType.getProperties().add(property.getpType()));
            }
        }
        return properties;
    }

    /**
     * import OWL annotations (directives)
     *
     * @param o
     */
    private void importOntologyAnnotationProperties(OWLOntology o) {
        //
        for (OWLAnnotationProperty annotation : o.getAnnotationPropertiesInSignature()) {
            //todo -
        }
    }

}

