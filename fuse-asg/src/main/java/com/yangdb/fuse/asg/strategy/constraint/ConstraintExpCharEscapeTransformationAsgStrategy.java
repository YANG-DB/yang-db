package com.yangdb.fuse.asg.strategy.constraint;

/*-
 * #%L
 * fuse-asg
 * %%
 * Copyright (C) 2016 - 2019 The YangDb Graph Database Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */



import com.yangdb.fuse.asg.strategy.AsgStrategy;
import com.yangdb.fuse.model.asgQuery.AsgQuery;
import com.yangdb.fuse.model.asgQuery.AsgStrategyContext;
import com.yangdb.fuse.model.ontology.Property;
import com.yangdb.fuse.model.query.EBase;
import com.yangdb.fuse.model.query.properties.BaseProp;
import com.yangdb.fuse.model.query.properties.EProp;
import com.yangdb.fuse.model.query.properties.RelProp;
import com.yangdb.fuse.model.query.properties.constraint.Constraint;
import javaslang.collection.Stream;

import java.util.List;
import java.util.Optional;

import static com.yangdb.fuse.model.asgQuery.AsgQueryUtil.getEprops;
import static com.yangdb.fuse.model.asgQuery.AsgQueryUtil.getRelProps;
import static com.yangdb.fuse.model.query.properties.constraint.ConstraintOp.like;
import static com.yangdb.fuse.model.query.properties.constraint.ConstraintOp.likeAny;

/**
 * checks all constraints with op = like / like_any and adds to all expression's special chars '\\' is not present
 * this strategy must be the last since there are prior strategies that transforms like to eq (performance optimization)
 * therefore we don't want to change the expression when it is not going to be a "wildcard" search
 * we also need to do the expression altering only on string type properties so we need to wait for
 * the ConstraintTypeTransformationStrategy to be applied first
 */
public class ConstraintExpCharEscapeTransformationAsgStrategy implements AsgStrategy {
    //region AsgStrategy Implementation
    @Override
    public void apply(AsgQuery query, AsgStrategyContext context) {

        getEprops(query).stream()
                .filter(prop -> prop.getCon()!=null)
                .forEach(eProp -> applyExpressionTransformation(context, eProp, EProp.class));

        getRelProps(query).stream()
                .filter(prop -> prop.getCon()!=null)
                .forEach(relProp -> applyExpressionTransformation(context, relProp, RelProp.class));
    }
    //endregion

    //region Private Methods
    private void applyExpressionTransformation(AsgStrategyContext context, EBase eBase, Class klass) {
        if (klass == EProp.class || klass == RelProp.class) {
            BaseProp eProp = (BaseProp) eBase;
            Optional<Property> property = context.getOntologyAccessor().$property(eProp.getpType());
            final Constraint con = eProp.getCon();
            if (property.isPresent()
                    && con != null
                    //verify constraint of wildcard type
                    && (con.getOp().equals(like) || con.getOp().equals(likeAny))
                    //verify expression of string type
                    && property.get().getType().equals("string")) {
                con.setExpr(escapeSpecialChars(con.getExpr()));
            }
        }
    }

    private Object escapeSpecialChars(Object expr) {
        if (expr instanceof List) {
            return Stream.ofAll(((List) expr)).map(p -> escape(p)).toJavaList();
        }
        return escape(expr);
    }

    /**
     * Returns a String where those characters that QueryParser
     * expects to be escaped are escaped by a preceding <code>\</code>.
     */
    public static Object escape(Object s) {
        if (s instanceof String) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < ((String) s).length(); i++) {
                char c = ((String) s).charAt(i);
                // These characters are part of the query syntax and must be escaped
                if ( c == '?' || c == '\\') {
                    sb.append('\\');
                }
                sb.append(c);
            }
            return sb.toString();
        }
        //do nothing
        return s;
    }

    //endregion

}




