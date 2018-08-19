package com.kayhut.fuse.assembly.knowledge;

import com.kayhut.fuse.asg.strategy.AsgStrategy;
import com.kayhut.fuse.dispatcher.ontology.OntologyProvider;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgQueryUtil;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.model.ontology.EntityType;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.ontology.OntologyFinalizer;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.entity.EUntyped;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.projection.IdentityProjection;
import javaslang.collection.Stream;

import java.util.Optional;
import java.util.Set;

public class KnowledgeDefaultSelectionAsgStrategy implements AsgStrategy {
    //region Constructors
    public KnowledgeDefaultSelectionAsgStrategy(OntologyProvider ontologyProvider) {
        this.ontologyProvider = ontologyProvider;
        this.nonSelectablePTypes = Stream.of(
                OntologyFinalizer.ID_FIELD_PTYPE,
                OntologyFinalizer.TYPE_FIELD_PTYPE,
                "lastUpdateUser",
                "lastUpdateTime",
                "creationTime",
                "creationUser")
                .toJavaSet();
    }
    //endregion

    //region AsgStrategy Implementation
    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        Ontology.Accessor ont = new Ontology.Accessor(this.ontologyProvider.get(query.getOnt()).get());

        AsgQueryUtil.elements(query, EPropGroup.class).forEach(ePropGroupAsgEBase -> {
                    if (Stream.ofAll(ePropGroupAsgEBase.geteBase().getProps())
                            .filter(eProp -> eProp.getProj() != null).isEmpty()) {

                        Optional<AsgEBase<ETyped>> eTypedAsgEBase = AsgQueryUtil.ancestor(ePropGroupAsgEBase, ETyped.class);
                        if (eTypedAsgEBase.isPresent()) {
                            Stream.ofAll(ont.$entity$(eTypedAsgEBase.get().geteBase().geteType()).getProperties())
                                    .filter(pType -> !this.nonSelectablePTypes.contains(pType))
                                    .forEach(pType -> ePropGroupAsgEBase.geteBase().getProps().add(
                                            new EProp(0, pType, new IdentityProjection())));
                        } else {
                            Optional<AsgEBase<EUntyped>> eUntypedAsgEBase = AsgQueryUtil.ancestor(ePropGroupAsgEBase, EUntyped.class);
                            eUntypedAsgEBase.ifPresent(eUntypedAsgEBase1 ->
                                    Stream.ofAll(ont.entities())
                                            .flatMap(EntityType::getProperties)
                                            .filter(pType -> !this.nonSelectablePTypes.contains(pType))
                                            .forEach(pType -> ePropGroupAsgEBase.geteBase().getProps().add(
                                                    new EProp(0, pType, new IdentityProjection()))));
                        }
                    }
                }
        );
    }
    //endregion

    //region Fields
    private OntologyProvider ontologyProvider;
    private Set<String> nonSelectablePTypes;
    //endregion
}

