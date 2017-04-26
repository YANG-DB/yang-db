package com.kayhut.fuse.asg.util;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.query.EBase;

import java.util.*;
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

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> getBDescendant(AsgEBase<T> asgEBase, Predicate<AsgEBase> predicate) {
        return getElement(asgEBase, AsgEBase::getB, predicate);
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

    public static List<AsgEBase<? extends EBase>> findPath(AsgQuery query, int eNum1, int eNum2) {
        Optional<AsgEBase<EBase>> asgEBase1 = getNextDescendant(query.getStart(), eNum1);
        if (!asgEBase1.isPresent()) {
            return Collections.emptyList();
        }

        List<AsgEBase<? extends EBase>> path = collectNextDescendants(asgEBase1.get(), eNum2);
        if (path.isEmpty()) {
            path = collectAncestors(asgEBase1.get(), eNum2);
        }

        return path;
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> getNextBDescendant(AsgEBase<T> asgEBase, Predicate<AsgEBase> predicate){
        if (!asgEBase.getB().isEmpty()){
            Optional<AsgEBase<EBase>> nextBDescendant = getBDescendant(asgEBase, predicate);
            if (nextBDescendant.isPresent()) {
                return Optional.of((AsgEBase<S>)nextBDescendant.get());
            }
        }

        if (asgEBase.getNext().isEmpty()) {
            return Optional.empty();
        }

        for(AsgEBase<? extends EBase> nextAsgEBase : asgEBase.getNext()) {
            Optional<AsgEBase<EBase>> nextBDescendant = getNextBDescendant(nextAsgEBase, predicate);
            if (nextBDescendant.isPresent()) {
                return Optional.of((AsgEBase<S>)nextBDescendant.get());
            }
        }

        return Optional.empty();
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
            if (predicate.test(newPath.get(newPath.size() - 1))) {
                return newPath;
            }
        }

        return path;
    }

    public static <T extends EBase> Optional<AsgEBase<T>> getAsgEBaseByEnum(AsgQuery asgQuery, int eNum) {
        Optional<AsgEBase<T>> nextDescendant = getNextDescendant(asgQuery.getStart(), (child) -> child.geteNum() == eNum);
        Optional<AsgEBase<T>> bDescendant = getNextBDescendant(asgQuery.getStart(), (child) -> child.geteNum() == eNum);
        if (nextDescendant.isPresent())
            return nextDescendant;
        if (bDescendant.isPresent())
            return bDescendant;
        return Optional.empty();
    }
    //endregion
}
