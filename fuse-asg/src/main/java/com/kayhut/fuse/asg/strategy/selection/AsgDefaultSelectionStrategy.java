package com.kayhut.fuse.asg.strategy.selection;

import com.kayhut.fuse.asg.strategy.AsgStrategy;
import com.kayhut.fuse.dispatcher.ontolgy.OntologyProvider;
import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.model.query.entity.EEntityBase;

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
        AsgQueryUtil.elements(query, EEntityBase.class).forEach(eEntityBase -> {
            //eEntityBase.ge
        });
    }
    //endregion

    //region Fields
    private OntologyProvider ontologyProvider;
    //endregion
}
