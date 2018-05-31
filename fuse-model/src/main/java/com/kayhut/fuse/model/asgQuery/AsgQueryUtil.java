package com.kayhut.fuse.model.asgQuery;

import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.properties.EPropGroup;
import com.kayhut.fuse.model.query.properties.RelPropGroup;
import com.kayhut.fuse.model.query.quant.Quant2;

import javax.management.relation.Relation;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Created by Roman on 15/05/2017.
 */
public class AsgQueryUtil {
    //region Public Methods
    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> get(AsgEBase<T> asgEBase, int eNum) {
        return element(asgEBase, emptyIterableFunction, AsgEBase::getNext, p->p.geteBase().geteNum()==eNum, truePredicate);
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> ancestor(AsgEBase<T> asgEBase, Predicate<AsgEBase> predicate) {
        return element(asgEBase, emptyIterableFunction, AsgEBase::getParents, predicate, truePredicate);
    }

    public static <T extends EBase, S extends EBase> List<AsgEBase<S>> ancestors(AsgEBase<T> asgEBase, Predicate<AsgEBase> predicate) {
        return elements(asgEBase, emptyIterableFunction, AsgEBase::getParents, predicate, truePredicate, Collections.emptyList());
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> ancestor(AsgEBase<T> asgEBase, Class<?> klass) {
        return ancestor(asgEBase, (asgEBase1) -> classPredicateFunction.apply(klass).test(asgEBase1) &&
                notThisPredicateFunction.apply(asgEBase).test(asgEBase1));
    }

    public static <T extends EBase, S extends EBase> List<AsgEBase<S>> ancestors(AsgEBase<T> asgEBase, Class<?> klass) {
        return ancestors(asgEBase, (asgEBase1) -> classPredicateFunction.apply(klass).test(asgEBase1) &&
                notThisPredicateFunction.apply(asgEBase).test(asgEBase1));
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> ancestor(AsgEBase<T> asgEBase, int eNum) {
        return ancestor(asgEBase, enumPredicateFunction.apply(eNum));
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> nextDescendant(AsgEBase<T> asgEBase, Predicate<AsgEBase> predicate) {
        return element(asgEBase, emptyIterableFunction, AsgEBase::getNext, predicate, truePredicate);
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> nextDescendant(AsgEBase<T> asgEBase, Class<?> klass) {
        return nextDescendant(asgEBase, (asgEBase1) -> classPredicateFunction.apply(klass).test(asgEBase1) &&
                notThisPredicateFunction.apply(asgEBase).test(asgEBase1));
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> nextDescendant(AsgEBase<T> asgEBase, int eNum) {
        return nextDescendant(asgEBase, enumPredicateFunction.apply(eNum));
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> nextAdjacentDescendant(AsgEBase<T> asgEBase, Predicate<AsgEBase> predicate) {
        return element(asgEBase, emptyIterableFunction, AsgEBase::getNext, predicate, adjacentDfsPredicate.apply(asgEBase));
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> adjacentAncestor(AsgEBase<T> asgEBase, Predicate<AsgEBase> predicate) {
        return element(asgEBase, emptyIterableFunction, AsgEBase::getParents, predicate, adjacentDfsPredicate.apply(asgEBase));
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> nextAdjacentDescendant(AsgEBase<T> asgEBase, Class<?> klass) {
        return nextAdjacentDescendant(asgEBase, (asgEBase1) -> classPredicateFunction.apply(klass).test(asgEBase1) &&
                notThisPredicateFunction.apply(asgEBase).test(asgEBase1));
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> adjacentAncestor(AsgEBase<T> asgEBase, Class<?> klass) {
        return adjacentAncestor(asgEBase, (asgEBase1) -> classPredicateFunction.apply(klass).test(asgEBase1) &&
                notThisPredicateFunction.apply(asgEBase).test(asgEBase1));
    }

    /**
     * A leaf is:
     * a node that has no next element OR has no parents and next descendant is not a quant
     * @param asgEBase
     * @param <T>
     * @return
     */
    public static <T extends EBase>  Optional<Boolean> isLeaf(AsgEBase<T> asgEBase) {
        if(!EEntityBase.class.isAssignableFrom(asgEBase.geteBase().getClass()))
            return Optional.empty();
        return Optional.of(!asgEBase.hasNext() || (isFirst(asgEBase) && !nextDescendant(asgEBase, Quant2.class).isPresent()));
    }

    private static <T extends EBase> boolean isFirst(AsgEBase<T> asgEBase) {
        return ((asgEBase.geteBase().getClass().equals(Start.class)) ||
            asgEBase.getParents().isEmpty() ||
            asgEBase.getParents().get(0).geteBase().getClass().equals(Start.class));
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> nextAdjacentDescendant(AsgEBase<T> asgEBase, Class<?> klass, int hopes) {
        int count = 0;
        Optional<AsgEBase<S>> element = nextAdjacentDescendant(asgEBase, (asgEBase1) -> classPredicateFunction.apply(klass).test(asgEBase1) &&
                notThisPredicateFunction.apply(asgEBase).test(asgEBase1));
        while (!element.isPresent() && count < hopes && !asgEBase.getNext().isEmpty()) {
            AsgEBase<? extends EBase> next = asgEBase.getNext().get(0);
            element = nextAdjacentDescendant(next, (asgEBase1) -> classPredicateFunction.apply(klass).test(asgEBase1) &&
                    notThisPredicateFunction.apply(next).test(asgEBase1));
            count++;
        }
        return element;
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> nextAdjacentDescendant(AsgEBase<T> asgEBase, int eNum) {
        return nextAdjacentDescendant(asgEBase, enumPredicateFunction.apply(eNum));
    }

    public static <T extends EBase, S extends EBase> List<AsgEBase<S>> nextDescendants(AsgEBase<T> asgEBase, Predicate<AsgEBase> elementPredicate, Predicate<AsgEBase> dfsPredicate) {
        return elements(asgEBase, emptyIterableFunction, AsgEBase::getNext, elementPredicate, dfsPredicate, Collections.emptyList());
    }

    public static <T extends EBase, S extends EBase> List<AsgEBase<S>> nextDescendants(AsgEBase<T> asgEBase, Class<?> klass) {
        return nextDescendants(asgEBase, classPredicateFunction.apply(klass), truePredicate);
    }

    public static <T extends EBase, S extends EBase> List<AsgEBase<S>> nextAdjacentDescendants(AsgEBase<T> asgEBase, Predicate<AsgEBase> elementPredicate) {
        return nextDescendants(asgEBase, elementPredicate, adjacentDfsPredicate.apply(asgEBase));
    }

    public static <T extends EBase, S extends EBase> List<AsgEBase<S>> nextDescendantsSingleHop(AsgEBase<T> asgEBase, Class<?> klass) {
        return nextDescendants(asgEBase, (asgEBase1 -> classPredicateFunction.apply(klass).test(asgEBase1) && asgEBase1 != asgEBase), (asgEBase1 -> asgEBase1 == asgEBase || !classPredicateFunction.apply(klass).test(asgEBase1)) );
    }



    public static <T extends EBase, S extends EBase> List<AsgEBase<S>> nextAdjacentDescendants(AsgEBase<T> asgEBase, Class<?> klass) {
        return nextDescendants(asgEBase, classPredicateFunction.apply(klass), adjacentDfsPredicate.apply(asgEBase));
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> bDescendant(AsgEBase<T> asgEBase, Predicate<AsgEBase> predicate) {
        return element(asgEBase, AsgEBase::getB, emptyIterableFunction, predicate, truePredicate);
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> bDescendant(AsgEBase<T> asgEBase, Class<?> klass) {
        return bDescendant(asgEBase, (asgEBase1) -> classPredicateFunction.apply(klass).test(asgEBase1) &&
                notThisPredicateFunction.apply(asgEBase).test(asgEBase1));
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> bDescendant(AsgEBase<T> asgEBase, int eNum) {
        return bDescendant(asgEBase, enumPredicateFunction.apply(eNum));
    }

    public static <T extends EBase, S extends EBase> List<AsgEBase<S>> bDescendants(AsgEBase<T> asgEBase, Predicate<AsgEBase> elementPredicate, Predicate<AsgEBase> dfsPredicate) {
        return elements(asgEBase, AsgEBase::getB, emptyIterableFunction, elementPredicate, dfsPredicate, Collections.emptyList());
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> descendantBDescendant(AsgEBase<T> asgEBase, Predicate<AsgEBase> predicate){
        return element(asgEBase, AsgEBase::getB, AsgEBase::getNext, predicate, truePredicate);
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> descendantBDescendant(AsgEBase<T> asgEBase, Class<?> klass){
        return descendantBDescendant(asgEBase, classPredicateFunction.apply(klass));
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> descendantBDescendant(AsgEBase<T> asgEBase, int eNum){
        return descendantBDescendant(asgEBase, enumPredicateFunction.apply(eNum));
    }

    public static <T extends EBase, S extends EBase> List<AsgEBase<S>> descendantBDescendants(AsgEBase<T> asgEBase, Predicate<AsgEBase> elementPredicate, Predicate<AsgEBase> dfsPredicate){
        return elements(asgEBase, AsgEBase::getB, AsgEBase::getNext, elementPredicate, dfsPredicate, Collections.emptyList());
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> ancestorBDescendant(AsgEBase<T> asgEBase, Predicate<AsgEBase> predicate){
        return element(asgEBase, AsgEBase::getB, AsgEBase::getParents, predicate, truePredicate);
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> ancestorBDescendant(AsgEBase<T> asgEBase, Class<?> klass){
        return ancestorBDescendant(asgEBase, (asgEBase1) -> classPredicateFunction.apply(klass).test(asgEBase1) &&
                notThisPredicateFunction.apply(asgEBase).test(asgEBase1));
    }

    public static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> ancestorBDescendant(AsgEBase<T> asgEBase, int eNum){
        return ancestorBDescendant(asgEBase, enumPredicateFunction.apply(eNum));
    }

    public static <T extends EBase> Optional<AsgEBase<T>> element(AsgQuery query, Predicate<AsgEBase> predicate) {
        return element(query.getStart(), AsgEBase::getB, AsgEBase::getNext, predicate, truePredicate);
    }

    public static <T extends EBase> Optional<AsgEBase<T>> element(AsgQuery query, Class<T> klass) {
        return element(query, classPredicateFunction.apply(klass));
    }

    public static <T extends EBase> Optional<AsgEBase<T>> element(AsgQuery query, int eNum) {
        return element(query, enumPredicateFunction.apply(eNum));
    }

    public static <T extends EBase> AsgEBase<T> element$(AsgQuery query, Predicate<AsgEBase> predicate) {
        return AsgQueryUtil.<T>element(query, predicate).get();
    }

    public static <T extends EBase> AsgEBase<T> element$(AsgQuery query, Class<T> klass) {
        return AsgQueryUtil.<T>element(query, klass).get();
    }

    public static <T extends EBase> AsgEBase<T> element$(AsgQuery query, int eNum) {
        return AsgQueryUtil.<T>element(query, eNum).get();
    }

    public static <T extends EBase> List<AsgEBase<T>> elements(AsgQuery query, Predicate<AsgEBase> elementPredicate) {
        return elements(query.getStart(), AsgEBase::getB, AsgEBase::getNext, elementPredicate, truePredicate, Collections.emptyList());
    }

    public static <T extends EBase> List<AsgEBase<T>> elements(AsgQuery query, Class<T> klass) {
        return elements(query, classPredicateFunction.apply(klass));
    }

    public static <T extends EBase> List<AsgEBase<T>> elements(AsgQuery query, int eNum) {
        return elements(query, enumPredicateFunction.apply(eNum));
    }

    public static <T extends EBase> List<AsgEBase<? extends EBase>> pathToNextDescendant(AsgEBase<T> asgEBase, Predicate<AsgEBase> predicate) {
        return path(asgEBase, AsgEBase::getNext, predicate, Collections.emptyList());
    }

    public static <T extends EBase> List<AsgEBase<? extends EBase>> pathToNextDescendant(AsgEBase<T> asgEBase, Class<?> klass) {
        return pathToNextDescendant(asgEBase, classPredicateFunction.apply(klass));
    }

    public static <T extends EBase> List<AsgEBase<? extends EBase>> pathToNextDescendant(AsgEBase<T> asgEBase, int eNum) {
        return pathToNextDescendant(asgEBase, enumPredicateFunction.apply(eNum));
    }

    public static <T extends EBase> List<AsgEBase<? extends EBase>> pathToBDescendant(AsgEBase<T> asgEBase, Predicate<AsgEBase> predicate) {
        return path(asgEBase, AsgEBase::getB, predicate, Collections.emptyList());
    }

    public static <T extends EBase> List<AsgEBase<? extends EBase>> pathToBDescendant(AsgEBase<T> asgEBase, Class<?> klass) {
        return pathToBDescendant(asgEBase, classPredicateFunction.apply(klass));
    }

    public static <T extends EBase> List<AsgEBase<? extends EBase>> pathToBDescendant(AsgEBase<T> asgEBase, int eNum) {
        return pathToBDescendant(asgEBase, enumPredicateFunction.apply(eNum));
    }

    public static <T extends EBase> List<AsgEBase<? extends EBase>> pathToAncestor(AsgEBase<T> asgEBase, Predicate<AsgEBase> predicate, List<AsgEBase<? extends EBase>> path) {
        return path(asgEBase, AsgEBase::getParents, predicate, path);
    }

    public static <T extends EBase> List<AsgEBase<? extends EBase>> pathToAncestor(AsgEBase<T> asgEBase, Class<?> klass) {
        return pathToAncestor(asgEBase, classPredicateFunction.apply(klass), Collections.emptyList());
    }

    public static <T extends EBase> List<AsgEBase<? extends EBase>> pathToAncestor(AsgEBase<T> asgEBase, int eNum) {
        return pathToAncestor(asgEBase, enumPredicateFunction.apply(eNum), Collections.emptyList());
    }

    public static <T extends EBase, S extends EBase> List<AsgEBase<? extends EBase>> path(AsgEBase<T> sourceAsgEBase, AsgEBase<S> destinationAsgEBase) {
        List<AsgEBase<? extends EBase>> path = pathToNextDescendant(sourceAsgEBase, destinationAsgEBase.geteNum());
        if (path.isEmpty()) {
            path = pathToAncestor(sourceAsgEBase, destinationAsgEBase.geteNum());
        }

        return path;
    }

    public static <T extends EBase, S extends EBase,Z extends EBase> Optional<AsgEBase<Z>> findFirstInPath(AsgEBase<T> sourceAsgEBase, AsgEBase<S> destinationAsgEBase, Predicate<AsgEBase> predicate) {
        List<AsgEBase<? extends EBase>> path = pathToNextDescendant(sourceAsgEBase, destinationAsgEBase.geteNum());
        if (path.isEmpty()) {
            path = pathToAncestor(sourceAsgEBase, destinationAsgEBase.geteNum());
        }
        Optional<AsgEBase<? extends EBase>> first = path.stream().filter(p -> predicate.test(p)).findFirst();
        if(first.isPresent())
            return Optional.of((AsgEBase<Z>) first.get());

        return Optional.empty();
    }

    public static List<AsgEBase<? extends EBase>> path(AsgQuery query, int sourceEnum, int destinationEnum) {
        Optional<AsgEBase<EBase>> sourceAsgEBase = element(query, sourceEnum);
        if (!sourceAsgEBase.isPresent()) {
            return Collections.emptyList();
        }

        List<AsgEBase<? extends EBase>> path = pathToNextDescendant(sourceAsgEBase.get(), destinationEnum);
        if (path.isEmpty()) {
            path = pathToAncestor(sourceAsgEBase.get(), destinationEnum);

            if (path.isEmpty()) {
                path = pathToBDescendant(sourceAsgEBase.get(), destinationEnum);
            }
        }

        return path;
    }

    public static List<Integer> eNums(AsgQuery query) {
        return values(query.getStart(), AsgEBase::getB, AsgEBase::getNext, AsgEBase::geteNum,
                truePredicate, truePredicate, Collections.emptyList());
    }

    public static AsgEBase<Rel> reverse(AsgEBase<Rel> relAsgEBase) {
        Rel reversedRel = new Rel();
        reversedRel.seteNum(relAsgEBase.geteNum());
        reversedRel.setrType(relAsgEBase.geteBase().getrType());
        reversedRel.setDir(relAsgEBase.geteBase().getDir() == Rel.Direction.L ? Rel.Direction.R : Rel.Direction.L);

        return AsgEBase.Builder.<Rel>get().withEBase(reversedRel).build();
    }
    //endregion

    //region Private Methods
    private static <T extends EBase, S extends EBase> Optional<AsgEBase<S>> element(
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
                Optional<AsgEBase<EBase>> recResult = element(elementAsgEBase, hElementProvider, vElementProvider, predicate, dfsPredicate);
                if (recResult.isPresent()) {
                    return Optional.of((AsgEBase<S>) recResult.get());
                }
            }
        }

        if (dfsPredicate.test(asgEBase)) {
            for (AsgEBase<? extends EBase> elementAsgEBase : hElementProvider.apply(asgEBase)) {
                Optional<AsgEBase<EBase>> recResult = element(elementAsgEBase, hElementProvider, vElementProvider, predicate, dfsPredicate);
                if (recResult.isPresent()) {
                    return Optional.of((AsgEBase<S>) recResult.get());
                }
            }
        }

        return Optional.empty();
    }

    public static <T extends EBase> List<AsgEBase<T>> elements(
            AsgEBase<? extends EBase> asgEBase,
            Function<AsgEBase<? extends EBase>,Iterable<AsgEBase<? extends EBase>>> vElementProvider,
            Function<AsgEBase<? extends EBase>,Iterable<AsgEBase<? extends EBase>>> hElementProvider,
            Predicate<AsgEBase> elementPredicate,
            Predicate<AsgEBase> dfsPredicate,
            List<AsgEBase<T>> elements) {

        return values(
                asgEBase,
                vElementProvider,
                hElementProvider,
                asgEBase1 -> (AsgEBase<T>)asgEBase1,
                elementPredicate,
                dfsPredicate,
                elements);
    }

    private static <T> List<T> values(
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
                newValues = values(elementAsgEBase, vElementProvider, hElementProvider, valueFunction, elementPredicate, dfsPredicate, newValues);
            }
        }

        if (dfsPredicate.test(asgEBase)) {
            for (AsgEBase<? extends EBase> elementAsgEBase : hElementProvider.apply(asgEBase)) {
                newValues = values(elementAsgEBase, vElementProvider, hElementProvider, valueFunction, elementPredicate, dfsPredicate, newValues);
            }
        }

        return newValues;
    }

    public static List<AsgEBase> elements(AsgQuery query) {
        return elements(query.getStart(), AsgEBase::getB, AsgEBase::getNext, truePredicate, truePredicate, Collections.EMPTY_LIST);
    }

    public static String pattern(AsgQuery query) {
        List<AsgEBase> elements = elements(query) ;
        StringJoiner joiner = new StringJoiner(":","","");
        elements.forEach(e-> {
            if(e.geteBase() instanceof EEntityBase)
                joiner.add(EEntityBase.class.getSimpleName());
            else if(e.geteBase() instanceof Rel)
                joiner.add(Relation.class.getSimpleName());
            else if(e.geteBase() instanceof EPropGroup)
                joiner.add(EPropGroup.class.getSimpleName());
            else if(e.geteBase() instanceof RelPropGroup)
                joiner.add(RelPropGroup.class.getSimpleName());
            else
                joiner.add(e.geteBase().getClass().getSimpleName());
        });
        return joiner.toString();
    }

    private static List<AsgEBase<? extends EBase>> path(
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
            newPath = path(elementAsgEBase, elementProvider, predicate, newPath);
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
