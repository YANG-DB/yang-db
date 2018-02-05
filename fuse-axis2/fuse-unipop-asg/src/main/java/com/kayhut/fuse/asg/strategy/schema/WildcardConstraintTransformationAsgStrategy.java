package com.kayhut.fuse.asg.strategy.schema;

import com.kayhut.fuse.asg.strategy.AsgStrategy;
import com.kayhut.fuse.dispatcher.ontology.OntologyProvider;
import com.kayhut.fuse.dispatcher.utils.AsgQueryUtil;
import com.kayhut.fuse.executor.ontology.GraphElementSchemaProviderFactory;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.query.Constraint;
import com.kayhut.fuse.model.query.ConstraintOp;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import javaslang.collection.Stream;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Created by roman.margolis on 05/02/2018.
 */
public class WildcardConstraintTransformationAsgStrategy implements AsgStrategy {
    //region Constructors
    public WildcardConstraintTransformationAsgStrategy(OntologyProvider ontologyProvider, GraphElementSchemaProviderFactory schemaProviderFactory) {
        this.ontologyProvider = ontologyProvider;
        this.schemaProviderFactory = schemaProviderFactory;
    }
    //endregion

    //region AsgStrategy Implementation
    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        Optional<Ontology> ontology = this.ontologyProvider.get(query.getOnt());
        if (!ontology.isPresent()) {
            return;
        }

        GraphElementSchemaProvider schemaProvider = this.schemaProviderFactory.get(ontology.get());

        AsgQueryUtil.elements(query, EPropGroup.class).forEach(ePropGroupAsgEBase -> {
            for(EProp eProp : Collections.unmodifiableCollection(ePropGroupAsgEBase.geteBase().getProps())) {
                if (eProp.getCon().getOp().equals(ConstraintOp.wildcard)) {
                    applyWildcardRules(schemaProvider, eProp, ePropGroupAsgEBase.geteBase());
                }
            }
        });
    }
    //endregion

    //region Private Methods
    private void applyWildcardRules(GraphElementSchemaProvider schemaProvider, EProp eProp, EPropGroup ePropGroup) {
        String expr = (String) eProp.getCon().getExpr();
        if (expr == null || expr.equals("")) {
            eProp.getCon().setOp(ConstraintOp.eq);
            return;
        }

        List<String> wildcardParts = Stream.of(expr.split("\\*")).filter(part -> !part.equals("")).toJavaList();

        boolean prefix = !expr.startsWith("*");
        boolean suffix = !expr.endsWith("*");
        boolean exact = prefix && suffix && wildcardParts.size() == 1;

        if (exact) {
            eProp.getCon().setOp(ConstraintOp.eq);
            return;
        }

        ePropGroup.getProps().remove(eProp);

        if (prefix) {
            ePropGroup.getProps().add(EProp.of(
                    eProp.getpType(),
                    eProp.geteNum(),
                    Constraint.of(ConstraintOp.wildcard, wildcardParts.get(0) + "*")));
        }

        if (suffix) {
            ePropGroup.getProps().add(EProp.of(
                    eProp.getpType(),
                    eProp.geteNum(),
                    Constraint.of(ConstraintOp.wildcard, "*" + wildcardParts.get(wildcardParts.size() - 1))));
        }

        if (wildcardParts.size() <= 2) {
            return;
        }

        for(int wildcardPartIndex = 1 ; wildcardPartIndex < wildcardParts.size() - 1 ; wildcardPartIndex++) {
            String wildcardPart = wildcardParts.get(wildcardPartIndex);
            if (eligibleForNgram(eProp, wildcardPart)) {
                ePropGroup.getProps().add(EProp.of(
                        eProp.getpType(),
                        eProp.geteNum(),
                        Constraint.of(ConstraintOp.eq, wildcardPart)));
            } else {
                ePropGroup.getProps().add(EProp.of(
                        eProp.getpType(),
                        eProp.geteNum(),
                        Constraint.of(ConstraintOp.wildcard, "*" + wildcardParts.get(wildcardPartIndex) + "*")));
            }
        }
    }

    private boolean eligibleForNgram(EProp eProp, String wildcardPart) {
        if (!wildcardPart.contains(" ") && wildcardPart.length() < 10) {
            return true;
        }

        return false;
    }
    //endregion

    //region Fields
    private GraphElementSchemaProviderFactory schemaProviderFactory;
    private OntologyProvider ontologyProvider;
    //endregion
}
