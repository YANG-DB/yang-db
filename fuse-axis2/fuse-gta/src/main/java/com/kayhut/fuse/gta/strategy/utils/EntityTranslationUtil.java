package com.kayhut.fuse.gta.strategy.utils;

import com.kayhut.fuse.model.ontology.EntityType;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.ontology.OntologyUtil;
import com.kayhut.fuse.model.query.entity.EConcrete;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.entity.EUntyped;
import com.kayhut.fuse.unipop.controller.GlobalConstants;
import com.kayhut.fuse.unipop.promise.Constraint;
import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.T;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Created by Roman on 14/05/2017.
 */
public class EntityTranslationUtil {
    public static List<String> getValidEntityNames(Ontology.Accessor ont, EEntityBase entity) {
        if (entity instanceof EConcrete) {
            return Collections.singletonList(ont.$entity$(((EConcrete) entity).geteType()).getName());
        } else if (entity instanceof ETyped) {
            return Collections.singletonList(ont.$entity$(((ETyped) entity).geteType()).getName());
        } else if (entity instanceof EUntyped) {
            return getValidEntityNames(ont, (EUntyped)entity);
        }

        return Collections.emptyList();
    }

    public static List<String> getValidEntityNames(Ontology.Accessor ont, EUntyped eUntyped) {
        List<String> eTypeNames = Stream.ofAll(eUntyped.getvTypes() == null ?
                Collections.emptyList() :
                eUntyped.getvTypes())
                .map(eType -> ont.$entity$(eType).getName())
                .toJavaList();

        if (eTypeNames.isEmpty()) {
            Set<String> nvTypeNames = Stream.ofAll(eUntyped.getNvTypes() == null ?
                    Collections.emptyList() :
                    eUntyped.getNvTypes())
                    .map(eType -> ont.$entity$(eType).getName())
                    .toJavaSet();

            eTypeNames = Stream.ofAll(ont.entities())
                    .map(EntityType::getName)
                    .filter(eName -> !nvTypeNames.contains(eName))
                    .toJavaList();
        }

        if (eTypeNames.isEmpty()) {
            eTypeNames = Stream.ofAll(ont.entities())
                    .map(EntityType::getName)
                    .toJavaList();
        }

        return eTypeNames;
    }
}
