package com.kayhut.fuse.assembly.knowledge;

import com.kayhut.fuse.asg.strategy.AsgStrategy;
import com.kayhut.fuse.assembly.knowledge.consts.ETypes;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgQueryUtil;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import com.kayhut.fuse.model.query.properties.constraint.ConstraintOp;
import com.kayhut.fuse.model.query.quant.Quant1;
import javaslang.collection.Stream;

import java.util.List;
import java.util.Optional;

import static com.kayhut.fuse.model.query.properties.constraint.ConstraintOp.eq;
import static com.kayhut.fuse.model.query.properties.constraint.ConstraintOp.ne;

public class KnowledgeGlobalEntityRedundantStrategy implements AsgStrategy {
    //region AsgStrategy Implementation
    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {
        List<AsgEBase<EBase>> globalEntities = Stream.ofAll(AsgQueryUtil.nextDescendants(query.getStart(), ETyped.class))
                .filter(this::isEntityGlobal)
                .toJavaList();

        for(AsgEBase<EBase> globalEntity : globalEntities) {
            List<AsgEBase<? extends EBase>> path = AsgQueryUtil.pathToAncestor(globalEntities.get(0), this::isEntityContextual);

            Optional<AsgEBase<? extends EBase>> contextualEntity = Stream.ofAll(path)
                    .filter(asgEBase -> isEntityContextual((AsgEBase<EBase>) asgEBase))
                    .toJavaOptional();
            boolean hasRelations = !Stream.ofAll(path).filter(asgEBase -> ETyped.class.isAssignableFrom(asgEBase.geteBase().getClass()))
                    .filter(asgEBase -> ((ETyped)asgEBase.geteBase()).geteType().equals("Relation"))
                    .isEmpty();

            if (!contextualEntity.isPresent() || hasRelations) {
                path = AsgQueryUtil.pathToNextDescendant(globalEntity, this::isEntityContextual);
                contextualEntity = Stream.ofAll(path)
                        .filter(asgEBase -> isEntityContextual((AsgEBase<EBase>) asgEBase))
                        .toJavaOptional();
                hasRelations = !Stream.ofAll(path).filter(asgEBase -> ETyped.class.isAssignableFrom(asgEBase.geteBase().getClass()))
                        .filter(asgEBase -> ((ETyped)asgEBase.geteBase()).geteType().equals("Relation"))
                        .isEmpty();
            }

            if (!contextualEntity.isPresent() || hasRelations) {
                continue;
            }

            EPropGroup contextualProps = getAdjacentEPropGroup((AsgEBase<EBase>) contextualEntity.get()).get().geteBase();
            List<EProp> contextProps = contextualProps.findAll(eProp -> eProp.getCon() != null && eProp.getpType().equals("context"));
            List<EProp> categoryProps = contextualProps.findAll(eProp -> eProp.getCon() != null && eProp.getpType().equals("category"));

            EPropGroup globalProps = getAdjacentEPropGroup(globalEntity).get().geteBase();
            Stream.ofAll(contextProps).forEach(prop -> globalProps.getProps().add(EProp.of(0, "subContext", prop.getCon())));
            Stream.ofAll(categoryProps).forEach(prop -> globalProps.getProps().add(EProp.of(0, "subCategory", prop.getCon())));

            Stream.ofAll(AsgQueryUtil.nextAdjacentDescendants(
                    AsgQueryUtil.nextAdjacentDescendant(globalEntity, Quant1.class).get(),
                    Rel.class)).filter(asgEBase -> ((Rel)asgEBase.geteBase()).getrType().equals("hasEvalue"))
                    .forEach(asgEBase -> {
                        EPropGroup globalValueProps = getAdjacentEPropGroup(asgEBase.getNext(0)).get().geteBase();
                        Stream.ofAll(contextProps).forEach(prop -> globalValueProps.getProps().add(EProp.of(0, "subContext", prop.getCon())));
                        Stream.ofAll(categoryProps).forEach(prop -> globalValueProps.getProps().add(EProp.of(0, "subCategory", prop.getCon())));
                    });

            AsgQueryUtil.adjacentAncestor(
                    AsgQueryUtil.ancestor(globalEntity, Quant1.class).get(),
                    ETyped.class).ifPresent(asgEBase -> {
                if (((ETyped)asgEBase.geteBase()).geteType().equals("Evalue")) {
                    EPropGroup globalValueProps = getAdjacentEPropGroup(asgEBase.getNext(0)).get().geteBase();
                    Stream.ofAll(contextProps).forEach(prop -> globalValueProps.getProps().add(EProp.of(0, "subContext", prop.getCon())));
                    Stream.ofAll(categoryProps).forEach(prop -> globalValueProps.getProps().add(EProp.of(0, "subCategory", prop.getCon())));
                }});
            }
    }
    //endregion

    //region Private Methods
    private boolean isEntityGlobal(AsgEBase<EBase> asgEBase) {
        return doesEntityHasConstraint(asgEBase, "context", Constraint.of(eq, "global"));
    }

    private boolean isEntityContextual(AsgEBase<EBase> asgEBase) {
        return doesEntityHasConstraint(asgEBase, "context", Constraint.of(ne, "global"));
    }

    private boolean doesEntityHasConstraint(AsgEBase<EBase> asgEBase, String pType, Constraint constraint) {
        if (!ETyped.class.isAssignableFrom(asgEBase.geteBase().getClass())) {
            return false;
        }

        ETyped eTyped = (ETyped)asgEBase.geteBase();
        if (!eTyped.geteType().equals("Entity")) {
            return false;
        }

        Optional<AsgEBase<EPropGroup>> ePropGroup = getAdjacentEPropGroup(asgEBase);
        if (!ePropGroup.isPresent()) {
            return false;
        }

        if (ePropGroup.get().geteBase().findAll(prop -> prop.equals(EProp.of(prop.geteNum(), pType, constraint))).isEmpty()) {
            return false;
        }

         return true;
    }

    private Optional<AsgEBase<EPropGroup>> getAdjacentEPropGroup(AsgEBase<EBase> asgEBase) {
        Optional<AsgEBase<Quant1>> quant = AsgQueryUtil.nextAdjacentDescendant(asgEBase, Quant1.class);
        if (!quant.isPresent()) {
            return Optional.empty();
        }

        return AsgQueryUtil.nextAdjacentDescendant(quant.get(), EPropGroup.class);
    }
    //endregion
}
