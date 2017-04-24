package com.kayhut.fuse.asg.util;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.query.EBase;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Created by Roman on 23/04/2017.
 */
public class AsgQueryUtils {
    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> getAncestor(AsgEBase<T> asgEBase, Predicate<AsgEBase> predicate) {
        for (AsgEBase<? extends EBase> parentAsgEBase : asgEBase.getParents()) {
            if (predicate.test(parentAsgEBase)) {
                return Optional.of((AsgEBase<S>)parentAsgEBase);
            }

            Optional<AsgEBase<S>> parentResult = getAncestor(parentAsgEBase, predicate);
            if (parentResult.isPresent()) {
                return parentResult;
            }
        }

        return Optional.empty();
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> getAncestor(AsgEBase<T> asgEBase, Class klass) {
        return getAncestor(asgEBase, (parent) -> klass.isAssignableFrom(parent.geteBase().getClass()));
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> getNextDescendant(AsgEBase<T> asgEBase, Predicate<AsgEBase> predicate) {
        for (AsgEBase<? extends EBase> childAsgEBase : asgEBase.getNext()) {
            if (predicate.test(childAsgEBase)) {
                return Optional.of((AsgEBase<S>)childAsgEBase);
            }

            Optional<AsgEBase<S>> parentResult = getNextDescendant(childAsgEBase, predicate);
            if (parentResult.isPresent()) {
                return parentResult;
            }
        }

        return Optional.empty();
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> getNextDescendant(AsgEBase<T> asgEBase, Class klass) {
        return getNextDescendant(asgEBase, (child) -> klass.isAssignableFrom(child.geteBase().getClass()));
    }
}
