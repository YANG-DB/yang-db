package com.kayhut.fuse.assembly.knowlegde;

import com.kayhut.fuse.asg.strategy.AsgElementStrategy;
import com.kayhut.fuse.asg.strategy.AsgStrategy;
import com.kayhut.fuse.asg.strategy.RuleBoostProvider;
import com.kayhut.fuse.asg.strategy.schema.utils.LikeUtil;
import com.kayhut.fuse.dispatcher.ontology.OntologyProvider;
import com.kayhut.fuse.executor.ontology.GraphElementSchemaProviderFactory;
import com.kayhut.fuse.model.asgQuery.AsgEBase;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.model.asgQuery.AsgQueryUtil;
import com.kayhut.fuse.model.asgQuery.AsgStrategyContext;
import com.kayhut.fuse.model.ontology.Ontology;
import com.kayhut.fuse.model.ontology.Property;
import com.kayhut.fuse.model.query.entity.EEntityBase;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.properties.*;
import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import com.kayhut.fuse.model.query.properties.constraint.ConstraintOp;
import com.kayhut.fuse.model.query.quant.QuantType;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementPropertySchema;
import com.kayhut.fuse.unipop.schemaProviders.GraphElementSchemaProvider;
import com.kayhut.fuse.unipop.schemaProviders.GraphVertexSchema;
import javaslang.collection.Stream;
import javaslang.control.Option;

import java.util.*;

/**
 * asg strategy that takes EPropGroups and searches within the next Eprops:
 * fieldIds: ['title','nicknames']
 * operators: ['LIKE','EQ']
 * <p>
 * [
 * AND{
 * - fieldId == "title"
 * - stringValue LIKE "...."
 * },
 * <p>
 * AND{
 * - fieldId == "nicknames"
 * - stringValue EQ "...."
 * }
 * ]
 * <p>
 * The strategy will replace the Eprop with a ScoreEprop, the entire group with ScoreEPropGroup.
 * We assume that the EpropGroup tree will represent the actual query sent to ES. To do so,
 * we will create two parts in the tree: condition (regular) props and ranking (boosting) props. The
 * regular props represent the filter section of the query.
 */
public class KnowledgeRankingAsgStrategy implements AsgStrategy, AsgElementStrategy<EPropGroup> {


    public KnowledgeRankingAsgStrategy(RuleBoostProvider boostProvider, OntologyProvider ontologyProvider, GraphElementSchemaProviderFactory schemaProviderFactory) {
        this.boostProvider = boostProvider;
        this.ontologyProvider = ontologyProvider;
        this.schemaProviderFactory = schemaProviderFactory;

        this.fieldNames = javaslang.collection.HashSet.of("title").add("nicknames");

        this.constraintOps = new HashSet<>();
        this.constraintOps.add(ConstraintOp.eq);
        this.constraintOps.add(ConstraintOp.like);
    }

    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {

        List<AsgEBase<EPropGroup>> eProps = AsgQueryUtil.elements(query, EPropGroup.class);

        eProps.forEach(e -> apply(query, e, context));
    }

    @Override
    public void apply(AsgQuery query, AsgEBase<EPropGroup> element, AsgStrategyContext context) {
        translateGroup(query, element);
    }

    private void translateGroup(AsgQuery query, AsgEBase<EPropGroup> eProp) {
        eProp.geteBase().getGroups().forEach(g -> translateGroup(query, new AsgEBase<>(g)));

        if (eProp.geteBase().getQuantType().equals(QuantType.all)) {
            List<EProp> fieldProp = Stream.ofAll(eProp.geteBase().getProps())
                    .filter(p -> p.getpType().equals("fieldId")
                            && p.getCon().getOp().equals(ConstraintOp.eq)
                            && java.util.stream.Stream.of(p.getCon().getExpr()).anyMatch(v -> !this.fieldNames.contains(v.toString())))
                    .toJavaList();
            if (!fieldProp.isEmpty()) {
                Option<EProp> stringValue = Stream.ofAll(eProp.geteBase().getProps()).find(p -> p.getpType().equals("stringValue") &&
                        this.constraintOps.contains(p.getCon().getOp()));
                if (!stringValue.isEmpty()) {
                    EPropGroup wrapperGroup = new EPropGroup(eProp.geteNum());
                    wrapperGroup.setQuantType(QuantType.some);
                    fieldNames.forEach(field ->
                            appendRanking(query, eProp, new AsgEBase<>(wrapperGroup),
                                    EProp.of(fieldProp.get(0).geteNum(), fieldProp.get(0).getpType(), new Constraint(ConstraintOp.eq, field)),
                                    stringValue.get()));
                    eProp.geteBase().getGroups().add(wrapperGroup);
                }
            }
        }
        //eProp.geteBase().getGroups().forEach(g -> translateGroup(new AsgEBase<>(g)));
    }

    private void appendRanking(AsgQuery query, AsgEBase<EPropGroup> parentGroup, AsgEBase<EPropGroup> eProp, EProp fieldProp, EProp stringValue) {
        EPropGroup group = new EPropGroup(eProp.geteNum());
        //add general fieldId filter
        group.getProps().add(EProp.of(fieldProp.geteNum(), fieldProp.getpType(), fieldProp.getCon()));

        //add ranking
        if (stringValue.getCon().getOp().equals(ConstraintOp.eq)) {
            group.getGroups().add(translateEquals(query, stringValue, fieldProp));
        }

        if (stringValue.getCon().getOp().equals(ConstraintOp.like)) {
            group.getGroups().add(translateLike(query, parentGroup, stringValue, fieldProp));
        }

        eProp.geteBase().getGroups().add(group);
    }

    private EPropGroup translateLike(AsgQuery query, AsgEBase<EPropGroup> parentGroup ,EProp stringValue, EProp fieldProp) {

        EPropGroup totalGroups = new EPropGroup(stringValue.geteNum());
        totalGroups.setQuantType(QuantType.some);
        // Rule 1
        EPropGroup r1Group = translateEquals(query, EProp.of(stringValue.geteNum(), stringValue.getpType(), Constraint.of(ConstraintOp.eq, stringValue.getCon().getExpr())), fieldProp);

        // Rule 2
        EPropGroup r2Group = translateRule(query, parentGroup, stringValue, fieldProp, " ", 2);

        // Rule 3
        String newExpression = stringValue.getCon().getExpr().toString().trim().replace(" ", "*");
        EPropGroup r3Group = translateRule(query, parentGroup, EProp.of(stringValue.geteNum(), stringValue.getpType(), new Constraint(ConstraintOp.eq, newExpression)), fieldProp, " ", 3);

        // Rule 4
        newExpression = stringValue.getCon().getExpr().toString().trim().replace(" ", "*");
        EPropGroup r4Group = translateRule(query, parentGroup, EProp.of(stringValue.geteNum(), stringValue.getpType(), new Constraint(ConstraintOp.eq, newExpression)), fieldProp, "", 4);

        totalGroups.getGroups().add(r1Group);
        totalGroups.getGroups().add(r2Group);
        totalGroups.getGroups().add(r3Group);
        totalGroups.getGroups().add(r4Group);
        return totalGroups;
    }

    private EPropGroup translateRule(AsgQuery query, AsgEBase<EPropGroup> parentGroup, EProp stringValue, EProp fieldProp, String wildcard, int ruleIndex) {
        EPropGroup group = new EPropGroup(stringValue.geteNum());
        group.setQuantType(QuantType.all);
        //inner group
        EPropGroup ePropGroup = new EPropGroup(stringValue.geteNum());
        ePropGroup.setQuantType(QuantType.some);
        String[] words = stringValue.getCon().getExpr().toString().split("\\*");
        List<EProp> newEprops = new ArrayList<>();
        Stream.ofAll(Arrays.asList(words)).filter(w -> w.length() > 0).forEach(word -> {
            newEprops.add(EProp.of(stringValue.geteNum(), stringValue.getpType(), Constraint.of(ConstraintOp.like, "*" + wildcard + word)));
            newEprops.add(EProp.of(stringValue.geteNum(), stringValue.getpType(), Constraint.of(ConstraintOp.like, word + wildcard + "*")));
            newEprops.add(EProp.of(stringValue.geteNum(), stringValue.getpType(), Constraint.of(ConstraintOp.like, "*" + wildcard + word + wildcard + "*")));
        });

        Ontology.Accessor ont = new Ontology.Accessor(ontologyProvider.get(query.getOnt()).get());
        GraphElementSchemaProvider schemaProvider = this.schemaProviderFactory.get(ont.get());
        Optional<AsgEBase<ETyped>> eTypedAsgEBase = AsgQueryUtil.ancestor(parentGroup, EEntityBase.class);

        Iterable<GraphVertexSchema> vertexSchemas = schemaProvider.getVertexSchemas(eTypedAsgEBase.get().geteBase().geteType());

        // currently supports a single vertex schema
        GraphVertexSchema vertexSchema = Stream.ofAll(vertexSchemas).get(0);
        Optional<Property> property = ont.$property(stringValue.getpType());
        Optional<GraphElementPropertySchema> propertySchema = vertexSchema.getProperty(property.get().getName());

        newEprops.forEach(eProp -> {
            LikeUtil.applyWildcardRules(eProp, propertySchema.get()).forEach(eProp1 -> ePropGroup.getProps().add(eProp1));
        });

        group.getGroups().add(ePropGroup);

        EPropGroup groupRule2Score = new ScoreEPropGroup(stringValue.geteNum(), boostProvider.getBoost(fieldProp, ruleIndex));
        groupRule2Score.setQuantType(QuantType.all);
        Set<String> terms = Stream.ofAll(Arrays.asList(stringValue.getCon().getExpr().toString().trim().replace("*", " ").split("\\s"))).filter(w -> w.length() > 0).toJavaSet();
        groupRule2Score.getProps().add(EProp.of(stringValue.geteNum(), stringValue.getpType(), Constraint.of(ConstraintOp.inSet, terms)));
        group.getGroups().add(groupRule2Score);
        return group;
    }

    /**
     * Converts a condition on AND(field, value) to (AND (field, value) (scored value))
     */
    private EPropGroup translateEquals(AsgQuery query, EProp stringValue, EProp fieldProp) {
        EProp adjustedStringValue = new ScoreEProp(stringValue.geteNum(), stringValue.getpType(), Constraint.of(ConstraintOp.eq, stringValue.getCon().getExpr().toString().replace("*", " ").trim()), boostProvider.getBoost(fieldProp, 1));
        EPropGroup group = new EPropGroup(stringValue.geteNum());
        group.setQuantType(QuantType.all);
        group.getProps().add(adjustedStringValue);
        return group;
    }

    private javaslang.collection.Set<String> fieldNames;
    private Set<ConstraintOp> constraintOps;
    private RuleBoostProvider boostProvider;
    private final OntologyProvider ontologyProvider;
    private final GraphElementSchemaProviderFactory schemaProviderFactory;


}
