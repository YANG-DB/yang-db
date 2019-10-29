package com.yangdb.fuse.model.execution.plan;

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
 * Direction.java - fuse-model - yangdb - 2,016
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

import com.yangdb.fuse.model.query.Rel;

/**
 * Created by lior.perry on 22/02/2017.
 */
public enum Direction {
    in,
    out,
    both;

    public Direction reverse() {
        if (this == both)
            return both;
        return in == this ? out : in;
    }

    public static Rel.Direction reverse(Rel.Direction dir) {
        return of(dir).reverse().to();
    }

    public static Direction of(Rel.Direction dir) {
        switch (dir) {
            case R:
                return out;
            case L:
                return in;
            case RL:
                return both;
        }
        return both;
    }

    public Rel.Direction to() {
        switch (this) {
            case both:
                return Rel.Direction.RL;
            case in:
                return Rel.Direction.L;
            case out:
                return Rel.Direction.R;

        }
        return Rel.Direction.RL;
    }
}
