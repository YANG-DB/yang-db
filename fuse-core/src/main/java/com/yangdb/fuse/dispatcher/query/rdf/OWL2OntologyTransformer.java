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
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.StringDocumentSource;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;

import java.util.*;
import java.util.stream.Collectors;

/**
 * transform OWL RDF ontology schema to YangDB ontology support
 */
public class OWL2OntologyTransformer implements OntologyTransformerIfc<Set<String>, Ontology> {
    @Override
    /**
     * load owl ontologies -
     *  the order of the ontologies is important in regards with the owl dependencies
     */
    public Ontology transform(Set<String> source) {
        Ontology.OntologyBuilder builder = Ontology.OntologyBuilder.anOntology();
        return importOWL(IRI.create("http://yangdb.org"), builder, source).build();
    }

    @Override
    public Set<String> translate(Ontology source) {
        throw new NotImplementedException("");
    }

    public OWLOntologyManager createOwlOntologyManager(
            OWLOntologyLoaderConfiguration config,
            IRI excludeDocumentIRI
    ) {
        OWLOntologyManager m = OWLManager.createOWLOntologyManager();
        config.setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT);
        return m;
    }

    public Ontology.OntologyBuilder importOWL(
            IRI documentIRI,
            Ontology.OntologyBuilder builder,
            Set<String> owls) {
        try {
            //set ontology name
            builder.withOnt(documentIRI.toString());

            OWLOntologyLoaderConfiguration config = new OWLOntologyLoaderConfiguration();
            OWLOntologyManager m = createOwlOntologyManager(config, documentIRI);
            owls.stream().map(owl -> {
                try {
                    return populate(builder, m.loadOntologyFromOntologyDocument(new StringDocumentSource(owl, documentIRI), config));
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

    private OWLOntology populate(Ontology.OntologyBuilder builder, OWLOntology o) {
        long startTime = System.currentTimeMillis();
        long endTime = System.currentTimeMillis();
        long importAnnotationPropertiesTime = endTime - startTime;

        startTime = System.currentTimeMillis();
        importOntologyClasses(builder, o);
        endTime = System.currentTimeMillis();
        long importConceptsTime = endTime - startTime;

        startTime = System.currentTimeMillis();
        importObjectProperties(builder, o);
        endTime = System.currentTimeMillis();
        long importObjectPropertiesTime = endTime - startTime;

        startTime = System.currentTimeMillis();
        importDataProperties(builder, o);
        endTime = System.currentTimeMillis();
        long importDataPropertiesTime = endTime - startTime;
        return o;
    }

    private void importOntologyClasses(Ontology.OntologyBuilder builder, OWLOntology o) {
        System.out.println("--------------------------getClassesInSignature--------------------------------------------");
        //abstract classes
        for (OWLClass ontologyClass : o.getClassesInSignature()) {
            String type = ontologyClass.getIRI().getRemainder().or(ontologyClass.getIRI().toString());
            //functional actions: if EntityType does not exist create on, add it to builder and continue
            builder.getEntityType(type).orElseGet(
                    () -> builder.addEntityType(
                            EntityType.Builder.get()
                                    .withEType(type)
                                    .withName(type)
                                    .build())
                            .getEntityType(type)
                            .get());
            System.out.println("Type:" + type);
        }
    }

    private void importObjectProperties(Ontology.OntologyBuilder builder, OWLOntology o) {
        System.out.println("--------------------------getObjectPropertiesInSignature--------------------------------------------");
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
        if(objectPropertyDomainAxioms.isEmpty()) return Collections.emptyList();

        List<OWLObjectPropertyRangeAxiom> objectPropertyRangeAxioms = new ArrayList<>(o.getObjectPropertyRangeAxioms(objectProperty));
        //todo add Thing to list ?
        if(objectPropertyRangeAxioms.isEmpty()) return Collections.emptyList();

        //match domain & range pairs
        for (int i = 0; i < objectPropertyDomainAxioms.size(); i++) {
            pairs.addAll(
                    createPair(objectPropertyDomainAxioms.get(i).getDomain(),objectPropertyRangeAxioms.get(i).getRange()));
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
        return Sets.cartesianProduct(sideA,sideB).stream().map(p->new EPair(p.get(0),p.get(1))).collect(Collectors.toList());
    }

    private List<Property> generateProperty(Ontology.OntologyBuilder builder, OWLOntology o, OWLDataProperty objectProperty) {
        List<Property> properties = new ArrayList<>();
        List<OWLDataPropertyDomainAxiom> dataPropertyDomainAxioms = new ArrayList<>(o.getDataPropertyDomainAxioms(objectProperty));
        List<OWLDataPropertyRangeAxiom> dataPropertyRangeAxioms = new ArrayList<>(o.getDataPropertyRangeAxioms(objectProperty));
        //match domain & range pairs
        for (int i = 0; i < dataPropertyDomainAxioms.size(); i++) {
            String dataType = dataPropertyRangeAxioms.get(i).getRange().asOWLDatatype().getIRI().getRemainder()
                    .or(dataPropertyRangeAxioms.get(i).getRange().asOWLDatatype().getIRI().toString());

            String eType = dataPropertyDomainAxioms.get(i).getDomain().asOWLClass().getIRI().getRemainder()
                    .or(dataPropertyDomainAxioms.get(i).getDomain().asOWLClass().getIRI().toString());

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
        return properties;
    }

    private void importOntologyAnnotationProperties(OWLOntology o) {
        System.out.println("--------------------------getAnnotationPropertiesInSignature--------------------------------------------");
        //
        for (OWLAnnotationProperty annotation : o.getAnnotationPropertiesInSignature()) {
            //todo -
            System.out.println(annotation);
        }
    }

    private void importDataProperties(Ontology.OntologyBuilder builder, OWLOntology o) {
        //DatatypeProperty - nodes properties
        System.out.println("--------------------------getDataPropertiesInSignature--------------------------------------------");
        o.getDataPropertiesInSignature()
                .forEach(dataTypeProperty -> builder.addProperties(generateProperty(builder, o, dataTypeProperty)));
    }

}

