package com.kayhut.fuse.asg.util;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.query.EBase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Created by Roman on 23/04/2017.
 */
public class AsgQueryUtils {
    //region Public Methods
    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> getAncestor(AsgEBase<T> asgEBase, Predicate<AsgEBase> predicate) {
        return getElement(asgEBase, AsgEBase::getParents, predicate);
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> getAncestor(AsgEBase<T> asgEBase, Class klass) {
        return getAncestor(asgEBase, (parent) -> klass.isAssignableFrom(parent.geteBase().getClass()));
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> getAncestor(AsgEBase<T> asgEBase, int eNum) {
        return getAncestor(asgEBase, (parent) -> parent.geteNum() == eNum);
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> getNextDescendant(AsgEBase<T> asgEBase, Predicate<AsgEBase> predicate) {
        return getElement(asgEBase, AsgEBase::getNext, predicate);
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> getNextDescendant(AsgEBase<T> asgEBase, Class klass) {
        return getNextDescendant(asgEBase, (child) -> klass.isAssignableFrom(child.geteBase().getClass()));
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> getNextDescendant(AsgEBase<T> asgEBase, int eNum) {
        return getNextDescendant(asgEBase, (child) -> child.geteNum() == eNum);
    }

    public static <T extends EBase> List<AsgEBase<? extends EBase>> collectNextDescendants(AsgEBase<T> asgEBase, Predicate<AsgEBase> predicate, List<AsgEBase<? extends EBase>> path) {
        return collectElements(asgEBase, AsgEBase::getNext, predicate, path);
    }

    public static <T extends EBase> List<AsgEBase<? extends EBase>> collectNextDescendants(AsgEBase<T> asgEBase, Class<?> klass) {
        return collectNextDescendants(asgEBase, (child) -> klass.isAssignableFrom(child.geteBase().getClass()), Collections.emptyList());
    }

    public static <T extends EBase> List<AsgEBase<? extends EBase>> collectNextDescendants(AsgEBase<T> asgEBase, int eNum) {
        return collectNextDescendants(asgEBase, (child) -> child.geteNum() == eNum, Collections.emptyList());
    }

    public static <T extends EBase> List<AsgEBase<? extends EBase>> collectAncestors(AsgEBase<T> asgEBase, Predicate<AsgEBase> predicate, List<AsgEBase<? extends EBase>> path) {
        return collectElements(asgEBase, AsgEBase::getParents, predicate, path);
    }

    public static <T extends EBase> List<AsgEBase<? extends EBase>> collectAncestors(AsgEBase<T> asgEBase, Class<?> klass) {
        return collectAncestors(asgEBase, (child) -> klass.isAssignableFrom(child.geteBase().getClass()), Collections.emptyList());
    }

    public static <T extends EBase> List<AsgEBase<? extends EBase>> collectAncestors(AsgEBase<T> asgEBase, int eNum) {
        return collectAncestors(asgEBase, (child) -> child.geteNum() == eNum, Collections.emptyList());
    }
    //endregion

    //region Private Methods
    private static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> getElement(
            AsgEBase<T> asgEBase,
            Function<AsgEBase<? extends EBase>, Iterable<AsgEBase<? extends EBase>>> elementProvider,
            Predicate<AsgEBase> predicate) {

        for (AsgEBase<? extends EBase> elementAsgEBase : elementProvider.apply(asgEBase)) {
            if (predicate.test(elementAsgEBase)) {
                return Optional.of((AsgEBase<S>)elementAsgEBase);
            }

            Optional<AsgEBase<S>> recResult = getElement(elementAsgEBase, elementProvider, predicate);
            if (recResult.isPresent()) {
                return recResult;
            }
        }

        return Optional.empty();
    }

    private static List<AsgEBase<? extends EBase>> collectElements(
            AsgEBase<? extends EBase> asgEBase,
            Function<AsgEBase<? extends EBase>, Iterable<AsgEBase<? extends EBase>>> elementProvider,
            Predicate<AsgEBase> predicate,
            List<AsgEBase<? extends EBase>> path) {

        List<AsgEBase<? extends EBase>> newPath = new ArrayList<>(path);
        newPath.add(asgEBase);
        if (predicate.test(asgEBase)) {
            return newPath;
        }

        for (AsgEBase<? extends EBase> elementAsgEBase : elementProvider.apply(asgEBase)) {
            newPath = AsgQueryUtils.collectElements(elementAsgEBase, elementProvider, predicate, newPath);
            if (predicate.test(path.get(newPath.size() - 1))) {
                return newPath;
            }
        }

        return path;
    }
    //endregion
}
