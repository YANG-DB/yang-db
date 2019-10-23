package com.yangdb.fuse.model.execution.plan.costs;

/*-
 * #%L
 * fuse-model
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

/*-
 *
 * DetailedCost.java - fuse-model - yangdb - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
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
 *
 */

/**
 * Created by moti on 5/18/2017.
 */
public class DetailedCost extends DoubleCost {

    public final double lambdaNode;
    public final double lambdaEdge;
    public final double R;
    public final double N2;

    public DetailedCost(double cost, double lambdaNode , double lambdaEdge , double R, double N2) {
        super(cost);
        this.lambdaNode = lambdaNode;
        this.lambdaEdge = lambdaEdge;
        this.R = R;
        this.N2 = N2;
    }

    @Override
    public String toString() {
        return "{" +
                "estimation=" + cost +
                "lambdaNode=" + lambdaNode +
                ", lambdaEdge=" + lambdaEdge +
                ", R=" + R +
                ", N2=" + N2 +
                '}';
    }
}
