package com.kayhut.fuse.asg.strategy.selection;

import com.kayhut.fuse.asg.strategy.AsgStrategy;
import com.kayhut.fuse.dispatcher.ontolgy.OntologyProvider;
import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.entity.EUntyped;
import javaslang.collection.Stream;

/**
 * Created by Roman on 13/06/2017.
 */
public class AsgDefaultSelectionStrategy implements AsgStrategy {
    //region Constructors
    public AsgDefaultSelectionStrategy(OntologyProvider ontologyProvider) {
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
                                .map(pType -> Integer.toString(pType))
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
