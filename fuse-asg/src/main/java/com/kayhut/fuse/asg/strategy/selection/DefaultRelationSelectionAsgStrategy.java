package com.kayhut.fuse.asg.strategy.selection;

import com.kayhut.fuse.asg.strategy.AsgStrategy;
import com.kayhut.fuse.dispatcher.ontology.OntologyProvider;
import com.kayhut.fuse.model.asgQuery.AsgQueryUtil;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.model.ontology.EntityType;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.ontology.OntologyFinalizer;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.entity.EUntyped;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.RelProp;
import com.kayhut.fuse.model.query.properties.RelPropGroup;
import com.kayhut.fuse.model.query.properties.projection.IdentityProjection;
import javaslang.collection.Stream;

import java.util.Optional;
import java.util.Set;

/**
 *
 * Go over all RelPropGroups - if no projection was requested - go over all existing entity's props and add them as default
 */
public class DefaultRelationSelectionAsgStrategy implements AsgStrategy {
    //region Constructors
    public DefaultRelationSelectionAsgStrategy(OntologyProvider ontologyProvider) {
        this.ontologyProvider = ontologyProvider;
        this.nonSelectablePTypes = Stream.of(OntologyFinalizer.ID_FIELD_PTYPE, OntologyFinalizer.TYPE_FIELD_PTYPE)
                .toJavaSet();
    }
    //endregion

    //region AsgStrategy Implementation
    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        Ontology.Accessor ont = new Ontology.Accessor(this.ontologyProvider.get(query.getOnt()).get());

        AsgQueryUtil.elements(query, RelPropGroup.class).forEach(relPropGroup -> {
                    if (Stream.ofAll(relPropGroup.geteBase().getProps())
                            .filter(eProp -> eProp.getProj() != null).isEmpty()) {

                        Optional<AsgEBase<Rel>> relAsgEBase = AsgQueryUtil.ancestor(relPropGroup, Rel.class);
                        if (relAsgEBase.isPresent()) {
                            Stream.ofAll(ont.$relation$(relAsgEBase.get().geteBase().getrType()).getProperties())
                                    .filter(pType -> !this.nonSelectablePTypes.contains(pType))
                                    .forEach(pType -> relPropGroup.geteBase().getProps().add(
                                            new RelProp(0, pType, new IdentityProjection(),0)));
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
