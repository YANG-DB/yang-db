package com.kayhut.fuse.asg.util;

import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.ontology.OntologyUtil;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.entity.EUntyped;
import javaslang.collection.Stream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Created by Roman on 23/04/2017.
 */
public class AsgQueryUtils {

    public static boolean equals(AsgQuery source, AsgQuery target) {
        if(!source.getStart().equals(target.getStart()))
            return false;
        if(!source.getName().equals(target.getName()))
            return false;
        if(!source.getOnt().equals(target.getOnt()))
            return false;

        return true;
    }

    //region Public Methods
    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> getAncestor(AsgEBase<T> asgEBase, Predicate<AsgEBase> predicate) {
        return getElement(asgEBase, emptyIterableFunction, AsgEBase::getParents, predicate, truePredicate);
    }

    public static <T extends EBase, S extends EBase> List<AsgEBase<S>> getAncestors(AsgEBase<T> asgEBase, Predicate<AsgEBase> predicate) {
        return getElements(asgEBase, emptyIterableFunction, AsgEBase::getParents, predicate, truePredicate, Collections.emptyList());
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> getAncestor(AsgEBase<T> asgEBase, Class<?> klass) {
        return getAncestor(asgEBase, (asgEBase1) -> classPredicateFunction.apply(klass).test(asgEBase1) &&
                notThisPredicateFunction.apply(asgEBase).test(asgEBase1));
    }

    public static <T extends EBase, S extends EBase> List<AsgEBase<S>> getAncestors(AsgEBase<T> asgEBase, Class<?> klass) {
        return getAncestors(asgEBase, (asgEBase1) -> classPredicateFunction.apply(klass).test(asgEBase1) &&
                notThisPredicateFunction.apply(asgEBase).test(asgEBase1));
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> getAncestor(AsgEBase<T> asgEBase, int eNum) {
        return getAncestor(asgEBase, enumPredicateFunction.apply(eNum));
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> getNextDescendant(AsgEBase<T> asgEBase, Predicate<AsgEBase> predicate) {
        return getElement(asgEBase, emptyIterableFunction, AsgEBase::getNext, predicate, truePredicate);
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> getNextDescendant(AsgEBase<T> asgEBase, Class<?> klass) {
        return getNextDescendant(asgEBase, (asgEBase1) -> classPredicateFunction.apply(klass).test(asgEBase1) &&
                notThisPredicateFunction.apply(asgEBase).test(asgEBase1));
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> getNextDescendant(AsgEBase<T> asgEBase, int eNum) {
        return getNextDescendant(asgEBase, enumPredicateFunction.apply(eNum));
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> getNextAdjacentDescendant(AsgEBase<T> asgEBase, Predicate<AsgEBase> predicate) {
        return getElement(asgEBase, emptyIterableFunction, AsgEBase::getNext, predicate, adjacentDfsPredicate.apply(asgEBase));
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> getNextAdjacentAncestor(AsgEBase<T> asgEBase, Predicate<AsgEBase> predicate) {
        return getElement(asgEBase, emptyIterableFunction, AsgEBase::getParents, predicate, adjacentDfsPredicate.apply(asgEBase));
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> getNextAdjacentDescendant(AsgEBase<T> asgEBase, Class<?> klass) {
        return getNextAdjacentDescendant(asgEBase, (asgEBase1) -> classPredicateFunction.apply(klass).test(asgEBase1) &&
                notThisPredicateFunction.apply(asgEBase).test(asgEBase1));
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> getNextAdjacentAncestor(AsgEBase<T> asgEBase, Class<?> klass) {
        return getNextAdjacentAncestor(asgEBase, (asgEBase1) -> classPredicateFunction.apply(klass).test(asgEBase1) &&
                notThisPredicateFunction.apply(asgEBase).test(asgEBase1));
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> getNextAdjacentDescendant(AsgEBase<T> asgEBase, Class<?> klass,int hopes) {
        int count = 0;
        Optional<AsgEBase<S>> element = getNextAdjacentDescendant(asgEBase, (asgEBase1) -> classPredicateFunction.apply(klass).test(asgEBase1) &&
                notThisPredicateFunction.apply(asgEBase).test(asgEBase1));
        while (!element.isPresent() && count < hopes && !asgEBase.getNext().isEmpty()) {
            AsgEBase<? extends EBase> next = asgEBase.getNext().get(0);
            element = getNextAdjacentDescendant(next, (asgEBase1) -> classPredicateFunction.apply(klass).test(asgEBase1) &&
                    notThisPredicateFunction.apply(next).test(asgEBase1));
            count++;
        }
        return element;
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> getNextAdjacentDescendant(AsgEBase<T> asgEBase, int eNum) {
        return getNextAdjacentDescendant(asgEBase, enumPredicateFunction.apply(eNum));
    }

    public static <T extends EBase, S extends EBase> List<AsgEBase<S>> getNextDescendants(AsgEBase<T> asgEBase, Predicate<AsgEBase> elementPredicate, Predicate<AsgEBase> dfsPredicate) {
        return getElements(asgEBase, emptyIterableFunction, AsgEBase::getNext, elementPredicate, dfsPredicate, Collections.emptyList());
    }

    public static <T extends EBase, S extends EBase> List<AsgEBase<S>> getNextAdjacentDescendants(AsgEBase<T> asgEBase, Predicate<AsgEBase> elementPredicate) {
        return getNextDescendants(asgEBase, elementPredicate, adjacentDfsPredicate.apply(asgEBase));
    }

    public static <T extends EBase, S extends EBase> List<AsgEBase<S>> getNextAdjacentDescendants(AsgEBase<T> asgEBase, Class<?> klass) {
        return getNextDescendants(asgEBase, classPredicateFunction.apply(klass), adjacentDfsPredicate.apply(asgEBase));
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> getBDescendant(AsgEBase<T> asgEBase, Predicate<AsgEBase> predicate) {
        return getElement(asgEBase, AsgEBase::getB, emptyIterableFunction, predicate, truePredicate);
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> getBDescendant(AsgEBase<T> asgEBase, Class<?> klass) {
        return getBDescendant(asgEBase, (asgEBase1) -> classPredicateFunction.apply(klass).test(asgEBase1) &&
                notThisPredicateFunction.apply(asgEBase).test(asgEBase1));
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> getBDescendant(AsgEBase<T> asgEBase, int eNum) {
        return getBDescendant(asgEBase, enumPredicateFunction.apply(eNum));
    }

    public static <T extends EBase, S extends EBase> List<AsgEBase<S>> getBDescendants(AsgEBase<T> asgEBase, Predicate<AsgEBase> elementPredicate, Predicate<AsgEBase> dfsPredicate) {
        return getElements(asgEBase, AsgEBase::getB, emptyIterableFunction, elementPredicate, dfsPredicate, Collections.emptyList());
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> getDecendantBDescendant(AsgEBase<T> asgEBase, Predicate<AsgEBase> predicate){
        return getElement(asgEBase, AsgEBase::getB, AsgEBase::getNext, predicate, truePredicate);
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> getDecendantBDescendant(AsgEBase<T> asgEBase, Class<?> klass){
        return getDecendantBDescendant(asgEBase, classPredicateFunction.apply(klass));
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> getDecendantBDescendant(AsgEBase<T> asgEBase, int eNum){
        return getDecendantBDescendant(asgEBase, enumPredicateFunction.apply(eNum));
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> getAncestorBDescendant(AsgEBase<T> asgEBase, Predicate<AsgEBase> predicate){
        return getElement(asgEBase, AsgEBase::getB, AsgEBase::getParents, predicate, truePredicate);
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> getAncestorBDescendant(AsgEBase<T> asgEBase, Class<?> klass){
        return getAncestorBDescendant(asgEBase, (asgEBase1) -> classPredicateFunction.apply(klass).test(asgEBase1) &&
                notThisPredicateFunction.apply(asgEBase).test(asgEBase1));
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> getAncestorBDescendant(AsgEBase<T> asgEBase, int eNum){
        return getAncestorBDescendant(asgEBase, enumPredicateFunction.apply(eNum));
    }

    public static <T extends EBase> Optional<AsgEBase<T>> getElement(AsgQuery query, Predicate<AsgEBase> predicate) {
        return getElement(query.getStart(), AsgEBase::getB, AsgEBase::getNext, predicate, truePredicate);
    }

    public static <T extends EBase> Optional<AsgEBase<T>> getElement(AsgQuery query, Class<?> klass) {
        return getElement(query, classPredicateFunction.apply(klass));
    }

    public static <T extends EBase> Optional<AsgEBase<T>> getElement(AsgQuery query, int eNum) {
        return getElement(query, enumPredicateFunction.apply(eNum));
    }

    public static <T extends EBase> List<AsgEBase<T>> getElements(AsgQuery query, Predicate<AsgEBase> elementPredicate) {
        return getElements(query.getStart(), AsgEBase::getB, AsgEBase::getNext, elementPredicate, truePredicate, Collections.emptyList());
    }

    public static <T extends EBase> List<AsgEBase<T>> getElements(AsgQuery query, Class<?> klass) {
        return getElements(query, classPredicateFunction.apply(klass));
    }

    public static <T extends EBase> List<AsgEBase<T>> getElements(AsgQuery query, int eNum) {
        return getElements(query, enumPredicateFunction.apply(eNum));
    }

    public static <T extends EBase> List<AsgEBase<? extends EBase>> getPathToNextDescendant(AsgEBase<T> asgEBase, Predicate<AsgEBase> predicate) {
        return getPath(asgEBase, AsgEBase::getNext, predicate, Collections.emptyList());
    }

    public static <T extends EBase> List<AsgEBase<? extends EBase>> getPathToNextDescendant(AsgEBase<T> asgEBase, Class<?> klass) {
        return getPathToNextDescendant(asgEBase, classPredicateFunction.apply(klass));
    }

    public static <T extends EBase> List<AsgEBase<? extends EBase>> getPathToNextDescendant(AsgEBase<T> asgEBase, int eNum) {
        return getPathToNextDescendant(asgEBase, enumPredicateFunction.apply(eNum));
    }

    public static <T extends EBase> List<AsgEBase<? extends EBase>> getPathToBDescendant(AsgEBase<T> asgEBase, Predicate<AsgEBase> predicate) {
        return getPath(asgEBase, AsgEBase::getB, predicate, Collections.emptyList());
    }

    public static <T extends EBase> List<AsgEBase<? extends EBase>> getPathToBDescendant(AsgEBase<T> asgEBase, Class<?> klass) {
        return getPathToBDescendant(asgEBase, classPredicateFunction.apply(klass));
    }

    public static <T extends EBase> List<AsgEBase<? extends EBase>> getPathToBDescendant(AsgEBase<T> asgEBase, int eNum) {
        return getPathToBDescendant(asgEBase, enumPredicateFunction.apply(eNum));
    }

    public static <T extends EBase> List<AsgEBase<? extends EBase>> getPathToAncestor(AsgEBase<T> asgEBase, Predicate<AsgEBase> predicate, List<AsgEBase<? extends EBase>> path) {
        return getPath(asgEBase, AsgEBase::getParents, predicate, path);
    }

    public static <T extends EBase> List<AsgEBase<? extends EBase>> getPathToAncestor(AsgEBase<T> asgEBase, Class<?> klass) {
        return getPathToAncestor(asgEBase, classPredicateFunction.apply(klass), Collections.emptyList());
    }

    public static <T extends EBase> List<AsgEBase<? extends EBase>> getPathToAncestor(AsgEBase<T> asgEBase, int eNum) {
        return getPathToAncestor(asgEBase, enumPredicateFunction.apply(eNum), Collections.emptyList());
    }

    public static <T extends EBase, S extends EBase> List<AsgEBase<? extends EBase>> getPath(AsgEBase<T> sourceAsgEBase, AsgEBase<S> destinationAsgEBase) {
        List<AsgEBase<? extends EBase>> path = getPathToNextDescendant(sourceAsgEBase, destinationAsgEBase.geteNum());
        if (path.isEmpty()) {
            path = getPathToAncestor(sourceAsgEBase, destinationAsgEBase.geteNum());
        }

        return path;
    }

    public static List<AsgEBase<? extends EBase>> getPath(AsgQuery query, int sourceEnum, int destinationEnum) {
        Optional<AsgEBase<EBase>> sourceAsgEBase = getElement(query, sourceEnum);
        if (!sourceAsgEBase.isPresent()) {
            return Collections.emptyList();
        }

        List<AsgEBase<? extends EBase>> path = getPathToNextDescendant(sourceAsgEBase.get(), destinationEnum);
        if (path.isEmpty()) {
            path = getPathToAncestor(sourceAsgEBase.get(), destinationEnum);

            if (path.isEmpty()) {
                path = getPathToBDescendant(sourceAsgEBase.get(), destinationEnum);
            }
        }

        return path;
    }

    public static List<Integer> getEnums(AsgQuery query) {
        return getValues(query.getStart(), AsgEBase::getB, AsgEBase::getNext, AsgEBase::geteNum,
                truePredicate, truePredicate, Collections.emptyList());
    }

    public static AsgEBase<Rel> reverseRelation(AsgEBase<Rel> relAsgEBase) {
        Rel reversedRel = new Rel();
        reversedRel.seteNum(relAsgEBase.geteNum());
        reversedRel.setrType(relAsgEBase.geteBase().getrType());
        reversedRel.setDir(relAsgEBase.geteBase().getDir() == Rel.Direction.L ? Rel.Direction.R : Rel.Direction.L);

        return AsgEBase.Builder.<Rel>get().withEBase(reversedRel).build();
    }
    //endregion

    //region Private Methods
    private static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> getElement(
            AsgEBase<T> asgEBase,
            Function<AsgEBase<? extends EBase>, Iterable<AsgEBase<? extends EBase>>> vElementProvider,
            Function<AsgEBase<? extends EBase>, Iterable<AsgEBase<? extends EBase>>> hElementProvider,
            Predicate<AsgEBase> predicate,
            Predicate<AsgEBase> dfsPredicate) {

        if (predicate.test(asgEBase)) {
            return Optional.of((AsgEBase<S>) asgEBase);
        }

        if (dfsPredicate.test(asgEBase)) {
            for (AsgEBase<? extends EBase> elementAsgEBase : vElementProvider.apply(asgEBase)) {
                Optional<AsgEBase<EBase>> recResult = getElement(elementAsgEBase, hElementProvider, vElementProvider, predicate, dfsPredicate);
                if (recResult.isPresent()) {
                    return Optional.of((AsgEBase<S>) recResult.get());
                }
            }
        }

        if (dfsPredicate.test(asgEBase)) {
            for (AsgEBase<? extends EBase> elementAsgEBase : hElementProvider.apply(asgEBase)) {
                Optional<AsgEBase<EBase>> recResult = getElement(elementAsgEBase, hElementProvider, vElementProvider, predicate, dfsPredicate);
                if (recResult.isPresent()) {
                    return Optional.of((AsgEBase<S>) recResult.get());
                }
            }
        }

        return Optional.empty();
    }

    public static List<String> getVertexTypes(EEntityBase entity, Ontology ontology, Iterable<String> vertexTypes) {
        List<String> _vertexTypes = Stream.ofAll(vertexTypes).toJavaList();
        if (entity instanceof EUntyped) {
            EUntyped eUntyped = (EUntyped) entity;
            if (eUntyped.getvTypes().size() > 0) {
                _vertexTypes = eUntyped.getvTypes().stream().map(v -> OntologyUtil.getEntityTypeNameById(ontology, v)).collect(Collectors.toList());
            } else {
                _vertexTypes = StreamSupport.stream(vertexTypes.spliterator(), false).collect(Collectors.toList());
                if (eUntyped.getNvTypes().size() > 0) {
                    _vertexTypes.removeAll(eUntyped.getNvTypes().stream().map(v -> OntologyUtil.getEntityTypeNameById(ontology, v)).collect(Collectors.toList()));
                }
            }
        } else if (entity instanceof ETyped) {
            _vertexTypes = Collections.singletonList(OntologyUtil.getEntityTypeNameById(ontology, ((ETyped) entity).geteType()));
        }
        return _vertexTypes;
    }

    private static <T extends EBase> List<AsgEBase<T>> getElements(
            AsgEBase<? extends EBase> asgEBase,
            Function<AsgEBase<? extends EBase>,Iterable<AsgEBase<? extends EBase>>> vElementProvider,
            Function<AsgEBase<? extends EBase>,Iterable<AsgEBase<? extends EBase>>> hElementProvider,
            Predicate<AsgEBase> elementPredicate,
            Predicate<AsgEBase> dfsPredicate,
            List<AsgEBase<T>> elements) {

        return getValues(
                asgEBase,
                vElementProvider,
                hElementProvider,
                asgEBase1 -> (AsgEBase<T>)asgEBase1,
                elementPredicate,
                dfsPredicate,
                elements);
    }

    private static <T> List<T> getValues(
            AsgEBase<? extends EBase> asgEBase,
            Function<AsgEBase<? extends EBase>, Iterable<AsgEBase<? extends EBase>>> vElementProvider,
            Function<AsgEBase<? extends EBase>, Iterable<AsgEBase<? extends EBase>>> hElementProvider,
            Function<AsgEBase<? extends EBase>, T> valueFunction,
            Predicate<AsgEBase> elementPredicate,
            Predicate<AsgEBase> dfsPredicate,
            List<T> values) {

        List<T> newValues = values;

        if (elementPredicate.test(asgEBase)) {
            newValues = new ArrayList<>(values);
            newValues.add(valueFunction.apply(asgEBase));
        }

        if (dfsPredicate.test(asgEBase)) {
            for (AsgEBase<? extends EBase> elementAsgEBase : vElementProvider.apply(asgEBase)) {
                newValues = getValues(elementAsgEBase, vElementProvider, hElementProvider, valueFunction, elementPredicate, dfsPredicate, newValues);
            }
        }

        if (dfsPredicate.test(asgEBase)) {
            for (AsgEBase<? extends EBase> elementAsgEBase : hElementProvider.apply(asgEBase)) {
                newValues = getValues(elementAsgEBase, vElementProvider, hElementProvider, valueFunction, elementPredicate, dfsPredicate, newValues);
            }
        }

        return newValues;
    }

    private static List<AsgEBase<? extends EBase>> getPath(
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
            newPath = getPath(elementAsgEBase, elementProvider, predicate, newPath);
            if (predicate.test(newPath.get(newPath.size() - 1))) {
                return newPath;
            }
        }

        return path;
    }

    private static Function<AsgEBase<? extends EBase>, Iterable<AsgEBase<? extends EBase>>> emptyIterableFunction =
            (asgEBase -> Collections.emptyList());

    private static Function<Class<?>, Predicate<AsgEBase>> classPredicateFunction =
            (klass) -> (asgEBase -> klass.isAssignableFrom(asgEBase.geteBase().getClass()));

    private static Function<AsgEBase, Predicate<AsgEBase>> notThisPredicateFunction =
            (asgEBase) -> (asgEBase1 -> asgEBase1 != asgEBase);

    private static Function<Integer, Predicate<AsgEBase>> enumPredicateFunction =
            (eNum) -> (asgEBase -> asgEBase.geteNum() == eNum);

    private static Predicate<AsgEBase> truePredicate = (asgEBase -> true);
    private static Predicate<AsgEBase> falsePredicate = (asgEBase -> false);

    private static Function<AsgEBase, Predicate<AsgEBase>> adjacentDfsPredicate = (asgEBase -> (asgEBase1 -> asgEBase == asgEBase1));
    //endregion
}
