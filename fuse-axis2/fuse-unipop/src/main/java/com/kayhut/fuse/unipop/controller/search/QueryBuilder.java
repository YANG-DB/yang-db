package com.kayhut.fuse.unipop.controller.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vividsolutions.jts.geom.Coordinate;
import javaslang.collection.Stream;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.geo.ShapeRelation;
import org.elasticsearch.common.geo.builders.ShapeBuilder;
import org.elasticsearch.common.geo.builders.ShapeBuilders;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.geojson.Circle;
import org.geojson.Envelope;
import org.geojson.GeoJsonObject;

import java.io.IOException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * Created by User on 20/03/2017.
 */
public class QueryBuilder {
    public enum Op {
        query,
        matchAll,
        filtered,
        filter,
        bool,
        must,
        mustNot,
        should,
        term,
        terms,
        range,
        prefix,
        wildcard,
        regexp,
        match,
        ids,
        type,
        exists,
        queryBuilderFilter,
        param,
        geoShape,
        boost
    }

    private enum SeekMode {
        self,
        childrenOnly,
        selfWithChildren,
        full
    }

    public static class Keywords {
        // public static Set<String> getSet() {
        //    return set;
        // }
        //private static Set<String> set = ImmutableSet.of("$(ids)");
    }

    //region Constructor
    public QueryBuilder() {
        this.currentStack = new Stack<>();
    }
    //endregion

    //region Public Methods
    public QueryBuilder query() {
        return query(null);
    }

    public QueryBuilder query(String name) {
        if (this.root == null) {
            this.root = new QueryComposite(name, null);
            this.current = root;
        } else {
            if (this.current == root) {
                return this;
            }

            if (this.current.getOp() != Op.filtered) {
                throw new UnsupportedOperationException("'query' can only appear as root or immediately after 'filtered'");
            }

            if (seekLocalClass(current, QueryComposite.class) != null) {
                this.current = seekLocalClass(current, QueryComposite.class);
            } else {
                Composite queryComposite = new QueryComposite(name, current);
                this.current.children.add(queryComposite);
                this.current = queryComposite;
            }
        }

        return this;
    }

    public QueryBuilder matchAll() {
        if (this.root == null) {
            throw new UnsupportedOperationException("'matchAll' may not appear as first statement");
        }

        if (this.current.op != Op.query) {
            throw new UnsupportedOperationException("'matchAll' may only appear in the 'query' context");
        }

        Composite matchAllComposite = new MatchAllComposite(null, current);
        this.current.clear();
        this.current.children.add(matchAllComposite);

        return this;
    }

    public QueryBuilder filtered() {
        return filtered(null);
    }

    public QueryBuilder filtered(String name) {
        if (this.root == null) {
            throw new UnsupportedOperationException("'filtered' may not appear as first statement");
        }

        if (this.current != root) {
            throw new UnsupportedOperationException("'filtered' may only appear immediately after root 'query'");
        }

        if (seekLocalClass(current, FilteredComposite.class) != null) {
            this.current = seekLocalClass(current, FilteredComposite.class);
        } else {
            Composite filteredComposite = new FilteredComposite(name, current);
            this.current.clear();
            this.current.children.add(filteredComposite);
            this.current = filteredComposite;
        }

        return this;
    }

    public QueryBuilder filter() {
        return filter(null);
    }

    public QueryBuilder filter(String name) {
        if (this.root == null) {
            throw new UnsupportedOperationException("'filter' may not appear as first statement");
        }

        if (this.current.op != Op.filtered) {
            throw new UnsupportedOperationException("'filter' may only appear in the 'filtered' context");
        }

        if (seekLocalClass(current, FilterComposite.class) != null) {
            this.current = seekLocalClass(current, FilterComposite.class);
        } else {
            Composite filterComposite = new FilterComposite(name, current);
            this.current.children.add(filterComposite);
            this.current = filterComposite;
        }

        return this;
    }

    public QueryBuilder bool() {
        return bool(null);
    }

    public QueryBuilder bool(String name) {
        if (this.root == null) {
            throw new UnsupportedOperationException("'bool' may not appear as first statement");
        }

        if (this.current.op != Op.filter && current.op != Op.must && current.op != Op.mustNot && current.op != Op.should) {
            throw new UnsupportedOperationException("'bool' may only appear in the 'filter', 'must', 'mustNot' or 'should' context");
        }

        if (StringUtils.isNotBlank(name) && seekLocalName(current, name) != null) {
            this.current = seekLocalName(current, name);
            return this;
        }

        if (this.current.op == Op.filter && seekLocalClass(current, BoolComposite.class) != null) {
            this.current = seekLocalClass(current, BoolComposite.class);
            return this;
        }

        Composite boolComposite = new BoolComposite(name, current);
        this.current.children.add(boolComposite);
        this.current = boolComposite;

        return this;
    }

    public QueryBuilder must() {
        return must(null);
    }

    public QueryBuilder must(String name) {
        if (this.root == null) {
            throw new UnsupportedOperationException("'must' may not appear as first statement");
        }

        if (this.current.op != Op.bool) {
            throw new UnsupportedOperationException("'must' may only appear in the 'bool' context");
        }

        if (seekLocalClass(current, MustComposite.class) != null) {
            this.current = seekLocalClass(current, MustComposite.class);
        } else {
            Composite mustComposite = new MustComposite(name, current);
            this.current.children.add(mustComposite);
            this.current = mustComposite;
        }

        return this;
    }

    public QueryBuilder mustNot() {
        return mustNot(null);
    }

    public QueryBuilder mustNot(String name) {
        if (this.root == null) {
            throw new UnsupportedOperationException("'mustNot' may not appear as first statement");
        }

        if (this.current.op != Op.bool) {
            throw new UnsupportedOperationException("'mustNot' may only appear in the 'bool' context");
        }

        if (seekLocalClass(current, MustNotComposite.class) != null) {
            this.current = seekLocalClass(current, MustNotComposite.class);
        } else {
            Composite mustNotComposite = new MustNotComposite(name, current);
            this.current.children.add(mustNotComposite);
            this.current = mustNotComposite;
        }

        return this;
    }

    public QueryBuilder should() {
        return should(null);
    }

    public QueryBuilder should(String name) {
        if (this.root == null) {
            throw new UnsupportedOperationException("'should' may not appear as first statement");
        }

        if (this.current.op != Op.bool) {
            throw new UnsupportedOperationException("'should' may only appear in the 'bool' context");
        }

        if (seekLocalClass(current, ShouldComposite.class) != null) {
            this.current = seekLocalClass(current, ShouldComposite.class);
        } else {
            Composite shouldComposite = new ShouldComposite(name, current);
            this.current.children.add(shouldComposite);
            this.current = shouldComposite;
        }

        return this;
    }

    public QueryBuilder term(String fieldName, Object value) {
        return this.term(null, fieldName, value);
    }

    public QueryBuilder term(String name, String fieldName, Object value) {
        if (this.root == null) {
            throw new UnsupportedOperationException("'term' may not appear as first statement");
        }

        if (this.current.op != Op.filter && current.op != Op.must && current.op != Op.mustNot && current.op != Op.should) {
            throw new UnsupportedOperationException("'term' may only appear in the 'filter', 'must', 'mustNot' or 'should' context");
        }

        if (StringUtils.isNotBlank(name) && seekLocalName(current, name) != null) {
            this.current = seekLocalName(current, name);
            return this;
        }

        Composite termComposite = new TermComposite(name, fieldName, value, current);
        this.current.children.add(termComposite);
        this.current = termComposite;

        return this;
    }

    public QueryBuilder terms(String fieldName, Object value) {
        return this.terms(null, fieldName, value);
    }

    public QueryBuilder terms(String name, String fieldName, Object value) {
        if (this.root == null) {
            throw new UnsupportedOperationException("'terms' may not appear as first statement");
        }

        if (this.current.op != Op.filter && current.op != Op.must && current.op != Op.mustNot && current.op != Op.should) {
            throw new UnsupportedOperationException("'terms' may only appear in the 'filter', 'must', 'mustNot' or 'should' context");
        }

        if (!(Iterable.class.isAssignableFrom(value.getClass()))) {
            throw new IllegalArgumentException("illegal value argument for 'terms': " + value.getClass().getSimpleName());
        }

        if (StringUtils.isNotBlank(name) && seekLocalName(current, name) != null) {
            this.current = seekLocalName(current, name);
            return this;
        }

        Composite termsComposite = new TermsComposite(name, fieldName, value, current);
        this.current.children.add(termsComposite);
        this.current = termsComposite;

        return this;
    }

    public QueryBuilder prefix(String fieldName, String prefix) {
        return prefix(null, fieldName, prefix);
    }

    public QueryBuilder prefix(String name, String fieldName, String prefix) {
        if (this.root == null) {
            throw new UnsupportedOperationException("'prefix' may not appear as first statement");
        }

        if (this.current.op != Op.filter && current.op != Op.must && current.op != Op.mustNot && current.op != Op.should) {
            throw new UnsupportedOperationException("'prefix' may only appear in the 'filter', 'must', 'mustNot' or 'should' context");
        }

        if (StringUtils.isNotBlank(name) && seekLocalName(current, name) != null) {
            this.current = seekLocalName(current, name);
            return this;
        }

        Composite prefixComposite = new PrefixComposite(name, fieldName, prefix, current);
        this.current.children.add(prefixComposite);
        this.current = prefixComposite;

        return this;
    }

    public QueryBuilder regexp(String fieldName, String regexp) {
        return regexp(null, fieldName, regexp);
    }

    public QueryBuilder regexp(String name, String fieldName, String regexp) {
        if (this.root == null) {
            throw new UnsupportedOperationException("'regexp' may not appear as first statement");
        }

        if (this.current.op != Op.filter && current.op != Op.must && current.op != Op.mustNot && current.op != Op.should) {
            throw new UnsupportedOperationException("'regexp' may only appear in the 'filter', 'must', 'mustNot' or 'should' context");
        }

        if (StringUtils.isNotBlank(name) && seekLocalName(current, name) != null) {
            this.current = seekLocalName(current, name);
            return this;
        }

        Composite regexpComposite = new RegexpComposite(name, fieldName, regexp, current);
        this.current.children.add(regexpComposite);
        this.current = regexpComposite;

        return this;
    }

    public QueryBuilder wildcard(String fieldName, String wildcard) { return wildcard(null, fieldName, wildcard); }

    public QueryBuilder wildcard(String name, String fieldName, String wildcard) {
        if (this.root == null) {
            throw new UnsupportedOperationException("'wildcard' may not appear as first statement");
        }

        if (this.current.op != Op.filter && current.op != Op.must && current.op != Op.mustNot && current.op != Op.should) {
            throw new UnsupportedOperationException("'wildcard' may only appear in the 'filter', 'must', 'mustNot' or 'should' context");
        }

        if (StringUtils.isNotBlank(name) && seekLocalName(current, name) != null) {
            this.current = seekLocalName(current, name);
            return this;
        }

        Composite wildcardComposite = new WildcardComposite(name, fieldName, wildcard, current);
        this.current.children.add(wildcardComposite);
        this.current = wildcardComposite;

        return this;
    }

    public QueryBuilder match(String fieldName, Object value) {
        return this.match(null, fieldName, value);
    }

    public QueryBuilder match(String name, String fieldName, Object value) {
        if (this.root == null) {
            throw new UnsupportedOperationException("'match' may not appear as first statement");
        }

        if (this.current.op != Op.filter && current.op != Op.must && current.op != Op.mustNot && current.op != Op.should) {
            throw new UnsupportedOperationException("'match' may only appear in the 'filter', 'must', 'mustNot' or 'should' context");
        }

        if (StringUtils.isNotBlank(name) && seekLocalName(current, name) != null) {
            this.current = seekLocalName(current, name);
            return this;
        }

        Composite matchComposite = new MatchComposite(name, fieldName, value, current);
        this.current.children.add(matchComposite);
        this.current = matchComposite;

        return this;
    }

    public QueryBuilder range(String fieldName) {
        return this.range(null, fieldName);
    }

    public QueryBuilder range(String name, String fieldName) {
        if (this.root == null) {
            throw new UnsupportedOperationException("'range' may not appear as first statement");
        }

        if (this.current.op != Op.filter && current.op != Op.must && current.op != Op.mustNot && current.op != Op.should) {
            throw new UnsupportedOperationException("'range' may only appear in the 'filter', 'must', 'mustNot' or 'should' context");
        }

        if (StringUtils.isNotBlank(name) && seekLocalName(current, name) != null) {
            this.current = seekLocalName(current, name);
            return this;
        }

        Composite rangeComposite = new RangeComposite(name, fieldName, current);
        this.current.children.add(rangeComposite);
        this.current = rangeComposite;

        return this;
    }

    public QueryBuilder geoShape(String fieldName) {
        return this.geoShape(null, fieldName);
    }

    public QueryBuilder geoShape(String name, String fieldName) {
        if (this.root == null) {
            throw new UnsupportedOperationException("'geoShape' may not appear as first statement");
        }

        if (this.current.op != Op.filter && current.op != Op.must && current.op != Op.mustNot && current.op != Op.should) {
            throw new UnsupportedOperationException("'geoShape' may only appear in the 'filter', 'must', 'mustNot' or 'should' context");
        }

        if (StringUtils.isNotBlank(name) && seekLocalName(current, name) != null) {
            this.current = seekLocalName(current, name);
            return this;
        }

        Composite geoShapeComposite = new GeoShapeComposite(name, fieldName, current);
        this.current.children.add(geoShapeComposite);
        this.current = geoShapeComposite;

        return this;
    }

    public QueryBuilder ids(Object value) {
        if (this.root == null) {
            throw new UnsupportedOperationException("'ids' may not appear as first statement");
        }

        if (this.current.op != Op.filter && current.op != Op.must && current.op != Op.mustNot && current.op != Op.should) {
            throw new UnsupportedOperationException("'ids' may only appear in the 'filter', 'must', 'mustNot' or 'should' context");
        }

        if (!(value instanceof Iterable)) {
            throw new IllegalArgumentException("illegal value argument for 'ids'");
        }

        Composite idsComposite = new IdsComposite(value, current);
        this.current.children.add(idsComposite);
        this.current = idsComposite;

        return this;
    }

    public QueryBuilder ids(Iterable<String> ids, String... types) {
        if (this.root == null) {
            throw new UnsupportedOperationException("'ids' may not appear as first statement");
        }

        if (this.current.op != Op.filter && current.op != Op.must && current.op != Op.mustNot && current.op != Op.should) {
            throw new UnsupportedOperationException("'ids' may only appear in the 'filter', 'must', 'mustNot' or 'should' context");
        }

        Composite idsComposite = new IdsComposite(ids, types, current);
        this.current.children.add(idsComposite);
        this.current = idsComposite;

        return this;
    }

    public QueryBuilder type(String value) {
        if (this.root == null) {
            throw new UnsupportedOperationException("'type' may not appear as first statement");
        }

        if (this.current.op != Op.filter && current.op != Op.must && current.op != Op.mustNot && current.op != Op.should) {
            throw new UnsupportedOperationException("'type' may only appear in the 'filter', 'must', 'mustNot' or 'should' context");
        }

        Composite typeComposite = new TypeComposite(value, current);
        this.current.children.add(typeComposite);
        this.current = typeComposite;

        return this;
    }

    public QueryBuilder exists(String value) {
        if (this.root == null) {
            throw new UnsupportedOperationException("'exists' may not appear as first statement");
        }

        if (this.current.op != Op.filter && current.op != Op.must && current.op != Op.mustNot && current.op != Op.should) {
            throw new UnsupportedOperationException("'exists' may only appear in the 'filter', 'must', 'mustNot' or 'should' context");
        }

        Composite existsComposite = new ExistsComposite(value, current);
        this.current.children.add(existsComposite);
        this.current = existsComposite;

        return this;
    }

    public QueryBuilder queryBuilderFilter(QueryBuilder queryBuilder) {
        return this.queryBuilderFilter(null, queryBuilder);
    }

    public QueryBuilder queryBuilderFilter(String name, QueryBuilder queryBuilder) {
        if (this.root == null) {
            throw new UnsupportedOperationException("'queryBuilderFilter' (the filter of a Builder) may not appear as first statement");
        }

        if (this.current.op != Op.filter && current.op != Op.must && current.op != Op.mustNot && current.op != Op.should) {
            throw new UnsupportedOperationException("'queryBuilderFilter' may only appear in the 'filter', 'must', 'mustNot' or 'should' context");
        }

        if (StringUtils.isNotBlank(name) && seekLocalName(current, name) != null) {
            this.current = seekLocalName(current, name);
            return this;
        }

        if (this.current.op == Op.filter && seekLocalClass(current, QueryBuilderFilterComposite.class) != null) {
            this.current = seekLocalClass(current, QueryBuilderFilterComposite.class);
            return this;
        }

        Composite queryBuilderFilterComposite = new QueryBuilderFilterComposite(name, current, queryBuilder);
        this.current.children.add(queryBuilderFilterComposite);
        this.current = queryBuilderFilterComposite;

        return this;
    }

    public <V> QueryBuilder param(String name, V value) {
        if (this.current == this.root) {
            throw new UnsupportedOperationException("parameters may not be added getTo the root aggregation");
        }

        if (seekLocalParam(this.current, name) != null) {
            seekLocalParam(this.current, name).setValue(value);
        } else {
            Composite param = new ParamComposite(name, value, this.current);
            this.current.getChildren().add(param);
        }

        return this;
    }

    public QueryBuilder cache(boolean cache) {
        return this.param("cache", cache);
    }

    public <V> QueryBuilder from(V from) {
        return this.param("getFrom", from);
    }
    public <V> QueryBuilder to(V to) {
        return this.param("getTo", to);
    }
    public QueryBuilder includeLower(boolean includeLower) {
        return this.param("include_lower", includeLower);
    }
    public QueryBuilder includeUpper(boolean includeLower) {
        return this.param("include_upper", includeLower);
    }

    public QueryBuilder shape(GeoJsonObject shape) {
        return this.param("shape", shape);
    }

    public QueryBuilder relation(ShapeRelation relation) {
        return this.param("relation", relation);
    }

    public QueryBuilder seek(String name) {
        return seek(composite -> {
            if (composite == null) {
                return false;
            }

            return composite.getName() != null && composite.getName().equals(name);
        });
    }

    public QueryBuilder seek(Composite compositeSeek) {
        return seek(composite -> {
            if (composite == null) {
                return false;
            }

            return composite.equals(compositeSeek);
        });
    }

    public QueryBuilder seek(Predicate<Composite> predicate) {
        Composite seek = this.root.seek(predicate,SeekMode.full);
        if (seek != null) {
            this.current = seek;
        }

        return this;
    }

    public QueryBuilder seekRoot() {
        this.current = this.root;
        return this;
    }

    public QueryBuilder drop() {
        if (this.root == null) {
            return this;
        }

        if (this.root == this.current) {
            this.root = null;
            this.current = null;
            return this;
        }

        Composite newCurrent = this.current.getParent();
        this.current.drop();
        this.current = newCurrent;

        return this;
    }

    public QueryBuilder prune() {
        Composite pruneComposite = null;
        final List<Op> opPruneCandidates = Arrays.asList(Op.bool, Op.must, Op.mustNot, Op.should);
        do {
            pruneComposite = this.root.seek((composite) -> {
                return opPruneCandidates.contains(composite.getOp()) && (composite.getChildren() == null || composite.getChildren().size() == 0);

            }, SeekMode.full);

            if (this.current == pruneComposite) {
                this.drop();
            } else {
                pruneComposite.drop();
            }

        } while(pruneComposite != null);

        return this;
    }

    public QueryBuilder expand(Map<String, Object> expandValues) {
        this.root.expand(expandValues);
        return this;
    }

    public QueryBuilder clear() {
        if (this.current != null) {
            this.current.clear();
            if (this.current == this.root) {
                this.current = null;
                this.root = null;
            }
        }

        return this;
    }

    public QueryBuilder push() {
        this.currentStack.push(this.current);
        return this;
    }

    public QueryBuilder pop() {
        if (this.currentStack.size() > 0) {
            this.current = this.currentStack.pop();
        }

        return this;
    }

    public ContextualFieldValues getFieldValues(String fieldName) {
        if (root == null) {
            return null;
        }

        return this.getFieldValuesImpl(fieldName);
    }

    public org.elasticsearch.index.query.QueryBuilder getQuery() {
        if (root == null) {
            return null;
        }

        return (org.elasticsearch.index.query.QueryBuilder)root.build();
    }

    // The clone will return a deep clone of the query builder (except leaf values: e.g the Object value in terms composite).
    // The clone will set the current field getTo point getTo the root due getTo the difficulty in finding the cloned current composite in the clone Builder.
    @Override
    public QueryBuilder clone() {
        try {
            QueryBuilder clone = (QueryBuilder) super.clone();
            if (root != null) {
                clone.root = root.clone();
            }
            clone.current = clone.root;
            return clone;
        } catch(CloneNotSupportedException ex){
            return null;
        }
    }

    public boolean isValid() {
        List<Op> parentOps = Arrays.asList(Op.query, Op.filtered, Op.filter, Op.bool, Op.must, Op.mustNot, Op.should);
        Composite invalidComposite = this.root.seek(composite -> {
            return parentOps.contains(composite.getOp()) &&
                    (composite.getChildren() == null || composite.getChildren().size() == 0);
        }, SeekMode.full);

        return invalidComposite == null;
    }

    public boolean hasFilters() {
        List<Op> filterOps = Arrays.asList(Op.exists, Op.ids, Op.range, Op.term, Op.terms, Op.type);
        Composite filter = this.root.seek(composite -> {
            return filterOps.contains(composite.getOp());
        }, SeekMode.full);

        return filter != null;
    }

    public<T> T visit(String labelNodeToSeek, BiFunction<Composite, T, T> accumulator, T seed) {
        QueryBuilder queryBuilder = this.seek(labelNodeToSeek);
        return visit(queryBuilder.current, accumulator, seed);
    }

    public<T> T visit(Composite composite, BiFunction<Composite, T, T> accumulator, T seed) {
        if (composite == null) {
            return null;
        }

        T result = accumulator.apply(composite, seed);

        for (Composite child : composite.getChildren()) {
            result = visit(child, accumulator, result);
        }

        return result;
    }

    public QueryBuilder parent() {
        if (this.current == this.root) {
            return this;
        }

        this.current = this.current.getParent();
        return this;
    }

    //endregion

    //region Properties
    public Composite getCurrent() {
        return this.current;
    }
    //endregion

    //region Private Methods
    private Composite seekLocalName(Composite composite, String name) {
        return composite.seek(childComposite -> {
            if (childComposite == null) {
                return false;
            }

            return childComposite.getName() != null && childComposite.getName().equals(name);
        }, SeekMode.childrenOnly);
    }

    private Composite seekLocalClass(Composite composite, Class<? extends Composite> compositeClass){
        return composite.seek(childComposite -> {
            if (childComposite == null) {
                return false;
            }

            return childComposite.getClass().equals(compositeClass);
        }, SeekMode.childrenOnly);
    }

    private ParamComposite seekLocalParam(Composite composite, String name) {
        ParamComposite param = (ParamComposite)composite.seek(childComposite -> {
            if (childComposite == null) {
                return false;
            }

            return childComposite.getName() != null && childComposite.getName().equals(name) &&
                    ParamComposite.class.isAssignableFrom(childComposite.getClass());
        }, SeekMode.childrenOnly);

        return param;
    }

    private ContextualFieldValues getFieldValuesImpl(String fieldName) {
        ContextualFieldValues contextualFieldValues = new ContextualFieldValues();

        this.visit(this.root,
                (composite, set) -> {
                    switch (composite.getOp()) {
                        case mustNot:
                            return set == contextualFieldValues.getPositiveValues() ?
                                    contextualFieldValues.getNegativeValues() :
                                    contextualFieldValues.getPositiveValues();

                        case term:
                            QueryBuilder.TermComposite termComposite = (QueryBuilder.TermComposite)composite;
                            if (termComposite.getFieldName().equals(fieldName)) {
                                set.add(termComposite.getValue());
                            }
                            break;

                        case terms:
                            QueryBuilder.TermsComposite termsComposite = (QueryBuilder.TermsComposite)composite;
                            if (termsComposite.getFieldName().equals(fieldName)) {
                                if (Iterable.class.isAssignableFrom(termsComposite.getValue().getClass())) {
                                    set.addAll(Stream.ofAll((Iterable)termsComposite.getValue()).toJavaList());
                                } else if (String[].class.isAssignableFrom(termsComposite.getClass())) {
                                    set.addAll(Stream.of((String[])termsComposite.getValue()).toJavaList());
                                } else if (Date[].class.isAssignableFrom(termsComposite.getClass())) {
                                    set.addAll(Stream.of((Date[])termsComposite.getValue()).toJavaList());
                                } else if (Integer[].class.isAssignableFrom(termsComposite.getClass())) {
                                    set.addAll(Stream.of((Integer[])termsComposite.getValue()).toJavaList());
                                } else if (Long[].class.isAssignableFrom(termsComposite.getClass())) {
                                    set.addAll(Stream.of((Long[])termsComposite.getValue()).toJavaList());
                                } else {
                                    set.add(termsComposite.getValue());
                                }
                            }
                            break;

                        case range:
                            QueryBuilder.RangeComposite rangeComposite = (QueryBuilder.RangeComposite)composite;
                            if (rangeComposite.getFieldName().equals(fieldName)) {
                                Object from = CompositeHelper.getParamValue(rangeComposite, "getFrom", null);
                                Object to = CompositeHelper.getParamValue(rangeComposite, "getTo", null);
                                set.add(new ContextualFieldValues().new Range(from, to));
                            }
                            break;

                        case queryBuilderFilter:
                            QueryBuilderFilterComposite queryBuilderFilterComposite = (QueryBuilderFilterComposite)composite;
                            ContextualFieldValues fieldValues = queryBuilderFilterComposite.getQueryBuilder().getFieldValues(fieldName);

                            if (set == contextualFieldValues.getPositiveValues()) {
                                contextualFieldValues.getPositiveValues().addAll(fieldValues.getPositiveValues());
                                contextualFieldValues.getNegativeValues().addAll(fieldValues.getNegativeValues());
                            } else {
                                contextualFieldValues.getNegativeValues().addAll(fieldValues.getPositiveValues());
                                contextualFieldValues.getPositiveValues().addAll(fieldValues.getNegativeValues());
                            }
                    }

                    return set;
                }, contextualFieldValues.getPositiveValues());

        return contextualFieldValues;
    }
    //endregion

    //region Fields
    private Composite root;
    private Composite current;
    private Stack<Composite> currentStack;
    //endregion

    //region ContextualFieldValues
    public class ContextualFieldValues {
        public class Range {
            //region Constructor
            public Range(Object from, Object to) {
                this.from = from;
                this.to = to;
            }
            //endregion

            //region Properties
            public Object getFrom() {
                return this.from;
            }

            public Object getTo() {
                return this.to;
            }
            //endregion

            //region Fields
            private Object from;
            private Object to;
            //endregion
        }
        //region Constructor
        public ContextualFieldValues() {
            this.positiveValues = new HashSet<>();
            this.negativeValues = new HashSet<>();
        }
        //endregion

        //region Properties
        public Set<Object> getPositiveValues() {
            return this.positiveValues;
        }

        public Set<Object> getNegativeValues() {
            return this.negativeValues;
        }
        //endregion

        //region Fields
        private Set<Object> positiveValues;
        private Set<Object> negativeValues;
        //endregion
    }
    //endregion

    //region Composite
    public abstract class Composite implements Cloneable{

        //region Constructor
        public Composite(String name, Op op, Composite parent) {
            this.name = name;
            this.op = op;
            this.parent = parent;

            this.children = new ArrayList<>();
        }
        //endregion

        //region Abstract Methods
        protected abstract Object build();
        //endregion

        //region Protected Methods
        protected Composite seek(Predicate<Composite> predicate, SeekMode seekMode) {
            if (seekMode != SeekMode.childrenOnly) {
                if (predicate.test(this)) {
                    return this;
                }
            }

            if ((seekMode == SeekMode.childrenOnly || seekMode == SeekMode.selfWithChildren || seekMode == SeekMode.full)
                    && this.getChildren() != null) {
                for (Composite child : this.getChildren()) {
                    if (seekMode == SeekMode.full) {
                        Composite childSeek = child.seek(predicate, seekMode);
                        if (childSeek != null) {
                            return childSeek;
                        }
                    } else {
                        if (predicate.test(child)) {
                            return child;
                        }
                    }
                }
            }

            return null;
        }

        protected void expand(Map<String, Object> expandValues) {
            if (this.getChildren() != null) {
                for(Composite child : this.getChildren()) {
                    child.expand(expandValues);
                }
            }
        }

        protected void clear() {
            this.getChildren().clear();
        }

        protected void drop() {
            if (this.getParent() == null) {
                return;
            }

            this.getParent().getChildren().remove(this);
            this.parent = null;
        }

        @Override
        protected Composite clone() throws CloneNotSupportedException{
            Composite clone = (Composite)super.clone();
            clone.children = new ArrayList<>();
            for(Composite child : this.getChildren()) {
                Composite childClone = child.clone();

                clone.children.add(childClone);
                childClone.parent = clone;
            }

            return clone;
        }
        //endregion

        //region Properties
        public String getName() {
            return name;
        }

        public Op getOp() {
            return op;
        }

        protected List<Composite> getChildren() {
            return children;
        }

        public Composite getParent() {
            return parent;
        }
        //endregion

        //region Fields
        private String name;
        private Op op;
        private Composite parent;

        private List<Composite> children;
        //endregion
    }

    public class QueryBuilderFilterComposite extends Composite {
        //region Constructor
        public QueryBuilderFilterComposite(String name, Composite parent, QueryBuilder queryBuilder) {
            super(name, Op.queryBuilderFilter, parent);
            this.queryBuilder = queryBuilder;
        }
        //endregion

        //region Composite Implementation
        @Override
        public Object build() {
            return this.queryBuilder.seekRoot().query().filtered().filter().getCurrent().build();
        }

        @Override
        protected Composite clone() throws CloneNotSupportedException {
            QueryBuilderFilterComposite clone = (QueryBuilderFilterComposite) super.clone();
            clone.queryBuilder = this.queryBuilder.clone();

            return clone;
        }
        //endregion

        //region Properties
        public QueryBuilder getQueryBuilder() {
            return this.queryBuilder;
        }
        //endregion

        //region Fields
        private QueryBuilder queryBuilder;
        //endregion
    }

    public class QueryComposite extends Composite {
        //region Constructor
        protected QueryComposite(String name, Composite parent) {
            super(name, Op.query, parent);
        }
        //endregion

        //region Fields
        @Override
        protected Object build() {
            return getChildren().get(0).build();
        }
        //endregion
    }

    public class FilteredComposite extends Composite {
        //region Constructor
        protected FilteredComposite(String name, Composite parent) {
            super(name, Op.filtered, parent);
        }
        //endregion

        //region Composite
        @Override
        protected Object build() {
            org.elasticsearch.index.query.QueryBuilder queryBuilder = QueryBuilders.matchAllQuery();
            org.elasticsearch.index.query.QueryBuilder filterBuilder = QueryBuilders.matchAllQuery();

            for(Composite child : getChildren()) {
                if (child.getOp() == Op.query) {
                    queryBuilder = (org.elasticsearch.index.query.QueryBuilder) child.build();
                } else if (child.getOp() == Op.filter) {
                    filterBuilder = (org.elasticsearch.index.query.QueryBuilder) child.build();
                }
            }

            return QueryBuilders.boolQuery().must(queryBuilder).filter(filterBuilder);
        }
        //endregion
    }

    public class FilterComposite extends Composite {
        //region Constructor
        protected FilterComposite(String name, Composite parent) {
            super(name, Op.filter, parent);
        }
        //endregion

        //region Composite Implementation
        @Override
        protected Object build() {
            if (getChildren().isEmpty()) {
                return QueryBuilders.matchAllQuery();
            }

            return getChildren().get(0).build();
        }
        //endregion
    }

    public class BoolComposite extends Composite {
        //region Constructor
        protected BoolComposite(String name, Composite parent) {
            super(name, Op.bool, parent);
        }
        //endregion

        //region Composite Implementation
        @Override
        protected Object build() {
            Iterable<org.elasticsearch.index.query.QueryBuilder> mustFilters = new ArrayList<>();
            Iterable<org.elasticsearch.index.query.QueryBuilder> mustNotFilters = new ArrayList<>();
            Iterable<org.elasticsearch.index.query.QueryBuilder> shouldFilters = new ArrayList<>();

            for(Composite child : getChildren()) {
                switch (child.getOp()) {
                    case must:
                        mustFilters = (Iterable<org.elasticsearch.index.query.QueryBuilder>)child.build();
                        break;

                    case mustNot:
                        mustNotFilters = (Iterable<org.elasticsearch.index.query.QueryBuilder>)child.build();
                        break;

                    case should:
                        shouldFilters = (Iterable<org.elasticsearch.index.query.QueryBuilder>)child.build();
                        break;
                }
            }

            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            javaslang.collection.Stream.ofAll(mustFilters).forEach(filter -> boolQueryBuilder.must(filter));
            javaslang.collection.Stream.ofAll(mustNotFilters).forEach(filter -> boolQueryBuilder.mustNot(filter));
            javaslang.collection.Stream.ofAll(shouldFilters).forEach(filter -> boolQueryBuilder.should(filter));
            return boolQueryBuilder;
        }
        //endregion
    }

    public abstract class FiltersComposite extends Composite {
        //region Constructor
        protected FiltersComposite(String name, Op op, Composite parent) {
            super(name, op, parent);
        }
        //endregion

        //region Composite Implementation
        @Override
        protected Object build() {
            ArrayList<org.elasticsearch.index.query.QueryBuilder> filters = new ArrayList<>();
            for(Composite child : getChildren()) {
                filters.add((org.elasticsearch.index.query.QueryBuilder) child.build());
            }
            return filters;
        }
        //endregion
    }

    public class MustComposite extends FiltersComposite {

        protected MustComposite(String name, Composite parent) {
            super(name, Op.must, parent);
        }
    }

    public class MustNotComposite extends FiltersComposite {

        protected MustNotComposite(String name, Composite parent) {
            super(name, Op.mustNot, parent);
        }
    }

    public class ShouldComposite extends FiltersComposite {

        protected ShouldComposite(String name, Composite parent) {
            super(name, Op.should, parent);
        }
    }

    public class ParamComposite<V> extends Composite {
        //region Constructor
        public ParamComposite(String name, V value, Composite parent) {
            super(name, Op.param, parent);
            this.value = value;
        }
        //endregion

        //region Composite Implementation
        @Override
        public Object build() {
            return null;
        }
        //endregion

        //region Properties
        public V getValue() {
            return this.value;
        }

        public void setValue(V value) {
            this.value = value;
        }
        //endregion

        //region Fields
        private V value;
        //endregion
    }

    public abstract class FieldComposite extends Composite {
        //region Constructor
        public FieldComposite(String name, String fieldName, Op op, Composite parent) {
            super(name, op, parent);
            this.fieldName = fieldName;
        }
        //endregion

        //region Properties
        public String getFieldName() {
            return this.fieldName;
        }
        //endregion

        //region Fields
        private String fieldName;
        //endregion
    }

    public class TermComposite extends FieldComposite {
        //region Constructor
        protected TermComposite(String name, String fieldName, Object value, Composite parent) {
            super(name, fieldName, Op.term, parent);
            this.value = value;
        }
        //endregion

        //region Composite Implementation
        @Override
        protected Object build() {
            if (this.value != null && this.value.getClass().equals(Date.class)) {
                return QueryBuilders.termQuery(this.getFieldName(), ((Date)this.value).getTime());
            }

            return QueryBuilders.termQuery(this.getFieldName(), this.value.toString());
        }
        //endregion

        //region Properties
        public Object getValue() {
            return this.value;
        }
        //endregion

        //region Fields
        private Object value;
        //endregion
    }

    public class TermsComposite extends FieldComposite {
        //region Constructor
        protected TermsComposite(String name, String fieldName, Object value, Composite parent) {
            super(name, fieldName, Op.terms, parent);
            this.value = value;
        }
        //endregion

        //region Composite Implementation
        @Override
        protected Object build() {
            if (this.value != null && Iterable.class.isAssignableFrom(this.value.getClass())) {
                List valueList = javaslang.collection.Stream.ofAll((Iterable)value).toJavaList();
                if (!valueList.isEmpty() && valueList.get(0).getClass().equals(Date.class)) {
                    return QueryBuilders.termsQuery(this.getFieldName(),
                            Stream.ofAll(valueList).map(listVal -> ((Date)listVal).getTime()).toJavaList());
                }

                return QueryBuilders.termsQuery(
                        this.getFieldName(),
                        javaslang.collection.Stream.ofAll((Iterable)value).toJavaList());
            }

            return QueryBuilders.termsQuery(this.getFieldName(), this.value);
        }

        @Override
        protected void expand(Map<String, Object> expandValues) {
            super.expand(expandValues);

            if (this.value instanceof String && this.value.toString().startsWith("$")) {
                Object newValue = expandValues.get(this.value);

                if (newValue == null || !(newValue instanceof Iterable)) {
                    throw new IllegalArgumentException("illegal expand value for 'terms'");
                }

                this.value = newValue;
            }
        }
        //endregion

        //region Properties
        public Object getValue() {
            return this.getValue();
        }
        //endregion

        //region Fields
        private Object value;
        //endregion
    }

    public class RangeComposite extends FieldComposite {

        //region Constructor
        protected RangeComposite(String name, String fieldName, Composite parent) {
            super(name, fieldName, Op.range, parent);
        }
        //endregion

        //region Composite Implementation
        @Override
        protected Object build() {
            Object from = CompositeHelper.getParamValue(this, "getFrom", null);
            Object to = CompositeHelper.getParamValue(this, "getTo", null);
            boolean includeLower = CompositeHelper.getParamValue(this, "include_lower", true);
            boolean includeUpper = CompositeHelper.getParamValue(this, "include_upper", false);
            boolean cache = CompositeHelper.getParamValue(this, "cache", true);

            // TEMPORARY PATCH: should add transformation logic to GTA
            if (from != null && from.getClass().equals(Date.class)) {
                from = ((Date)from).getTime();
            }

            if (to != null && to.getClass().equals(Date.class)) {
                to = ((Date)to).getTime();
            }
            // TEMPORARY PATCH: should add transformation logic to GTA

            if (from != null || to != null) {
                return QueryBuilders.rangeQuery(this.getFieldName())
                        .from(from)
                        .to(to)
                        .includeLower(includeLower)
                        .includeUpper(includeUpper);
            } else if (from != null && to == null) {
                return QueryBuilders.rangeQuery(this.getFieldName())
                        .from(from)
                        .includeLower(includeLower);
            } else if (from == null && to != null) {
                return QueryBuilders.rangeQuery(this.getFieldName())
                        .to(to)
                        .includeUpper(includeUpper);
            } else {
                throw new UnsupportedOperationException("range filter can only be built with full range using 'getFrom' and 'getTo' or using the Compare predicate");
            }
        }
        //endregion
    }

    public class PrefixComposite extends FieldComposite {
        //region Constructor
        protected PrefixComposite(String name, String fieldName, String prefix, Composite parent) {
            super(name, fieldName, Op.prefix, parent);
            this.prefix = prefix;
        }
        //endregion

        //region Composite Implementation
        @Override
        protected Object build() {
            return QueryBuilders.prefixQuery(this.getFieldName(), this.prefix);
        }
        //endregion

        //region Properties
        public String getPrefix() {
            return this.prefix;
        }
        //endregion

        //region Fields
        private String prefix;
        //endregion
    }

    public class RegexpComposite extends FieldComposite {
        //region Constructor
        protected RegexpComposite(String name, String fieldName, String regexp, Composite parent) {
            super(name, fieldName, Op.regexp, parent);
            this.regexp = regexp;
        }
        //endregion

        //region Composite Implementation
        @Override
        protected Object build() {
            return QueryBuilders.regexpQuery(this.getFieldName(), this.regexp);
        }
        //endregion

        //region Properties
        public String getRegexp() {
            return this.regexp;
        }
        //endregion

        //region Fields
        private String regexp;
        //endregion
    }

    public class WildcardComposite extends FieldComposite {
        //region Constructor
        protected WildcardComposite(String name, String fieldName, String value, Composite parent) {
            super(name, fieldName, Op.wildcard, parent);
            this.value = value;
        }
        //endregion

        //region Composite Implementation
        @Override
        protected Object build() {
            return QueryBuilders.wildcardQuery(this.getFieldName(), this.value);
        }
        //endregion

        //region Properties
        public String getValue() {
            return this.value;
        }
        //endregion

        //region Fields
        private String value;
        //endregion
    }

    public class MatchComposite extends FieldComposite {
        //region Constructor
        protected MatchComposite(String name, String fieldName, Object value, Composite parent) {
            super(name, fieldName, Op.match, parent);
            this.value = value;
        }
        //endregion

        //region Composite Implementation
        @Override
        protected Object build() {
            return QueryBuilders.matchQuery(this.getFieldName(), this.value);
        }
        //endregion

        //region Properties
        public Object getValue() {
            return this.value;
        }
        //endregion

        //region Fields
        private Object value;
        //endregion
    }

    public class IdsComposite extends Composite {
        //region Constructor
        public IdsComposite(Object value, Composite parent) {
            super(null, Op.ids, parent);
            this.value = value;
            types = new String[0];
        }

        public IdsComposite(Iterable<String> ids, String[] types, Composite parent) {
            super(null, Op.ids, parent);
            this.value = ids;
            this.types = types;
        }
        //endregion

        //region Composite Implementation
        @Override
        protected Object build() {
            if (this.value instanceof Iterable) {
                ArrayList<String> ids = new ArrayList<>();
                for(Object obj : (Iterable)this.value) {
                    ids.add(obj.toString());
                }

                return QueryBuilders.idsQuery(this.types).addIds(Stream.ofAll(ids).toJavaArray(String.class));
            }

            return QueryBuilders.idsQuery(this.types).addIds(this.value.toString());
        }

        @Override
        protected void expand(Map<String, Object> expandValues) {
            super.expand(expandValues);

            if (this.value instanceof String && this.value.toString().startsWith("$")) {
                Object newValue = expandValues.get(this.value);

                if (newValue == null || !(newValue instanceof Iterable)) {
                    throw new IllegalArgumentException("illegal expand value for 'terms'");
                }

                this.value = newValue;
            }
        }
        //endregion

        //region Fields
        private Object value;
        private String[] types;
        //endregion
    }

    public class TypeComposite extends Composite {
        //region Constructor
        public TypeComposite(String value, Composite parent) {
            super(null, Op.type, parent);
            this.value = value;
        }
        //endregion

        //region Composite Implementation
        @Override
        protected Object build() {
            return QueryBuilders.typeQuery(this.value);
        }
        //endregion

        //region Fields
        private String value;
        //endregion
    }

    public class ExistsComposite extends Composite {
        //region Constructor
        public ExistsComposite(String value, Composite parent) {
            super(null, Op.exists, parent);
            this.value = value;
        }
        //endregion

        //region Composite Implementation
        @Override
        protected Object build() {
            return QueryBuilders.existsQuery(this.value);
        }
        //endregion

        //region Fields
        private String value;
        //endregion
    }

    public class MatchAllComposite extends Composite {
        //region Constructor
        public MatchAllComposite(String name, Composite parent) {
            super(name, Op.matchAll, parent);
        }
        //endregion

        //region Composite Implementation
        @Override
        protected Object build() {
            return QueryBuilders.matchAllQuery();
        }
        //endregion
    }

    public class GeoShapeComposite extends FieldComposite {
        //region Constructor
        public GeoShapeComposite(String name, String fieldName, Composite parent) {
            super(name, fieldName, Op.geoShape, parent);
        }

        //region Composite Implementation
        @Override
        protected Object build() {
            GeoJsonObject geoJson = CompositeHelper.getParamValue(this, "shape", null);
            ShapeRelation relation = CompositeHelper.getParamValue(this, "relation", ShapeRelation.INTERSECTS);
            boolean cache = CompositeHelper.getParamValue(this, "cache", true);

            try {
                return QueryBuilders.geoShapeQuery(this.getFieldName(), GetShapeBuilder(geoJson)).relation(relation);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        //endregion

        //region Private Methods
        private ShapeBuilder GetShapeBuilder(GeoJsonObject geoJson) {
            try {
                if (geoJson instanceof Circle) {
                    Circle cirlce = (Circle)geoJson;
                    return ShapeBuilders.newCircleBuilder()
                            .center(
                                    cirlce.getCoordinates().getLongitude(),
                                    cirlce.getCoordinates().getLatitude())
                            .radius(new DistanceUnit.Distance(cirlce.getRadius(), DistanceUnit.METERS));
                } else if (geoJson instanceof Envelope) {
                    Envelope envelope = (Envelope)geoJson;
                    return ShapeBuilders.newEnvelope(
                            new Coordinate(
                                    envelope.getCoordinates().get(0).getLongitude(),
                                    envelope.getCoordinates().get(1).getLatitude()),
                            new Coordinate(
                                    envelope.getCoordinates().get(1).getLongitude(),
                                    envelope.getCoordinates().get(0).getLatitude()));
                } else {
                    String geoJsonString = mapper.writeValueAsString(geoJson);
                    XContentParser parser = JsonXContent.jsonXContent.createParser(NamedXContentRegistry.EMPTY, geoJsonString);
                    parser.nextToken();

                    return ShapeBuilder.parse(parser);
                }
            } catch (Exception e) {
                return null;
            }
        }
        //endregion
    }

    private static ObjectMapper mapper = new ObjectMapper();

    public static class CompositeHelper {
        public static <V> V getParamValue(Composite composite, String paramName, V defaultValue) {
            ParamComposite<V> param = (ParamComposite<V>)composite.seek(
                    childComposite -> ParamComposite.class.isAssignableFrom(childComposite.getClass())
                            && childComposite.getName() == paramName, SeekMode.childrenOnly);

            if (param == null) {
                return defaultValue;
            }

            return param.getValue();
        }
    }
    //endregion
}
