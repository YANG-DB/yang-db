package com.kayhut.fuse.asg.strategy.selection;

import com.kayhut.fuse.asg.strategy.AsgStrategy;
import com.kayhut.fuse.dispatcher.ontology.OntologyProvider;
import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.ontology.OntologyFinalizer;
import com.kayhut.fuse.model.query.entity.ETyped;
import javaslang.collection.Stream;

/**
 * Created by Roman on 13/06/2017.
 */
public class DefaultSelectionAsgStrategy implements AsgStrategy {
    //region Constructors
    public DefaultSelectionAsgStrategy(OntologyProvider ontologyProvider) {
        this.ontologyProvider = ontologyProvider;
    }
    //endregion

    //region AsgStrategy Implementation
    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        Ontology.Accessor ont = new Ontology.Accessor(this.ontologyProvider.get(query.getOnt()).get());

        AsgQueryUtil.elements(query, ETyped.class).forEach(asgEBase -> {
                if (asgEBase.geteBase().getReportProps() == null ||
                        asgEBase.geteBase().getReportProps().isEmpty()) {

                    asgEBase.geteBase().setReportProps(
                            Stream.ofAll(ont.$entity$(asgEBase.geteBase().geteType()).getProperties())
                                .filter(pType -> !pType.equals(OntologyFinalizer.ID_FIELD_PTYPE) &&
                                                !pType.equals(OntologyFinalizer.TYPE_FIELD_PTYPE))
                                    .toJavaList());
                }
            }
        );
    }
    //endregion

    //region Fields
    private OntologyProvider ontologyProvider;
    //endregion
}
