package com.kayhut.fuse.assembly.knowledge.parser;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kayhut.fuse.assembly.knowledge.parser.model.Step;
import com.kayhut.fuse.assembly.knowledge.parser.model.Types;
import com.kayhut.fuse.model.Next;
import com.kayhut.fuse.model.query.EBase;
import com.kayhut.fuse.model.query.Query;
import com.kayhut.fuse.model.query.Rel;
import com.kayhut.fuse.model.query.Start;
import com.kayhut.fuse.model.query.entity.ETyped;
import com.kayhut.fuse.model.query.properties.EProp;
import com.kayhut.fuse.model.query.properties.constraint.Constraint;
import com.kayhut.fuse.model.query.properties.constraint.ConstraintOp;
import com.kayhut.fuse.model.query.quant.Quant1;
import com.kayhut.fuse.model.query.quant.QuantType;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.kayhut.fuse.assembly.knowledge.parser.model.MatchType2Constraint.asConstraint;

public class Json2QueryMapper {
    static private ObjectMapper mapper =
            new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);


    static public Query jsonParser(JSONObject query) throws IOException {
        Query.Builder builder = Query.Builder.instance();
        builder.withName(query.optString("name","Query:"+System.currentTimeMillis()));
        builder.withOnt(query.optString("ontology","Knowledge"));

        JSONArray clause = query.getJSONArray("clause");
        AtomicInteger sequence = new AtomicInteger(0);
        final JSONArray clauseArray = clause.getJSONArray(0);
        final Start start = new Start(sequence.get(), 0);
        AtomicReference<EBase> context = new AtomicReference<>(start);
        builder.withElement(start);

        //iterate and populate query elements
        for (int i = 0; i < clauseArray.length(); i++) {
            JSONObject obj = clauseArray.getJSONArray(i).getJSONObject(0);
            builder.withElement(element(context, mapper.readValue(obj.toString(), Step.class), sequence));
        }

        return builder.build();
    }


    private static EBase[] element(AtomicReference<EBase> context,Step step, AtomicInteger sequence) {
        List<EBase> elements = new ArrayList<>();
        switch (step.getType()) {
            case entity:
                elements.addAll(createNode(context,step,sequence));
                break;
            case link:
                elements.addAll(createEdge(context,step,sequence));
                break;
        }
        return elements.toArray(new EBase[elements.size()]);
    }

    /**
     * generate Entity type node plus hasEvalue relation with the eValue props
     * @param step
     * @param sequence
     * @return
     */
    private static Collection<EBase> createNode( AtomicReference<EBase> context, Step step, AtomicInteger sequence) {
        List<EBase> nodes = new ArrayList<>();
        final ETyped entity = new ETyped(sequence.incrementAndGet(),
                step.getConceptId() + "_" + sequence.get(),
                "Entity", 0, 0);
        addNext(context,entity);
        nodes.add(entity);
        //region populate eValues
        if(!step.getProperties().isEmpty()) {
            final int qauantId = sequence.incrementAndGet();
            entity.setNext(qauantId);
            Quant1 quant = new Quant1(qauantId,QuantType.all, new ArrayList<>(),0 );
            nodes.add(quant);
            //context moves to quant
            context.set(quant);
            //region add eProps
            step.getProperties().entrySet().forEach(e->{
                final int relId = sequence.incrementAndGet();
                quant.addNext(relId);

                //add rel hasEvalue
                final int eValue = sequence.incrementAndGet();
                nodes.add(new Rel(relId, "hasEvalue"  , Rel.Direction.R,relId+":"+step.getConceptId()+"_hasEvalue_"+ e.getKey(), eValue, 0));

                //add eValue Node
                final int propQuantId = sequence.incrementAndGet();
                nodes.add(new ETyped(eValue,eValue +":"+step.getConceptId()+"_eValue_"+e.getKey(),"EValue",propQuantId,0));

                //add properties quant
                final Quant1 propsQuant = new Quant1(propQuantId, QuantType.all, new ArrayList<>(), 0);
                nodes.add(propsQuant);

                //add eProp fieldId
                final int fieldId = sequence.incrementAndGet();
                nodes.add(new EProp(fieldId,"fieldId",
                        Constraint.of(ConstraintOp.eq,e.getKey())));
                propsQuant.addNext(fieldId);

                //add value constraint
                final int valueId = sequence.incrementAndGet();
                nodes.add(new EProp(valueId,
                        Types.byValue(e.getKey()).getFieldType(),
                        asConstraint(e.getValue().getMatchType(), e.getValue().getValue())));
                propsQuant.addNext(valueId);
            });
            //endregion
        }
        //endregion
        return nodes;
    }

    /**
     * generate Relation type node plus hasEvalue relation with the eValue props
     * @param context
     * @param step
     * @param sequence
     * @return
     */
    private static Collection<EBase> createEdge(AtomicReference<EBase> context, Step step, AtomicInteger sequence) {
        List<EBase> nodes = new ArrayList<>();
        //add rel hasEvalue
        final int relId = sequence.incrementAndGet();
        final int relEntity = sequence.incrementAndGet();
        final Rel hasRelation = new Rel(relId, "hasRelation", step.getDirection().getDirection(), relId + ":" + step.getConceptId() + "_hasRelation_", relEntity, 0);
        nodes.add(hasRelation);
        addNext(context,hasRelation);

        final ETyped entity = new ETyped(relEntity,
                step.getConceptId() + "_" + sequence.get(),
                "Entity", 0, 0);
        nodes.add(entity);
        //region populate eValues
        //todo add properties
        //endregion
        //todo add hasRelation to next entity
        return nodes;
    }

    private static void addNext(AtomicReference<EBase> context, EBase entity) {
        if(Quant1.class.isAssignableFrom(context.get().getClass())) {
            ((Quant1) context.get()).addNext(entity.geteNum());
        }else  if(Next.class.isAssignableFrom(context.get().getClass())) {
            ((Next) context.get()).setNext(entity.geteNum());
        }
    }

}
