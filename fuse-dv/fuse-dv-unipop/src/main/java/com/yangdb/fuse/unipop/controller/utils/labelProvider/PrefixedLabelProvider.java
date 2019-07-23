package com.yangdb.fuse.unipop.controller.utils.labelProvider;

/*-
 * #%L
 * fuse-dv-unipop
 * %%
 * Copyright (C) 2016 - 2018 yangdb   ------ www.yangdb.org ------
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

/**
 * Created by Roman on 22/05/2017.
 */
public class PrefixedLabelProvider implements LabelProvider<String> {
    //region Constructors
    public PrefixedLabelProvider(String splitString) {
        this.splitString = splitString;
    }
    //endregion

    //region LabelProvider Implementation
    @Override
    public String get(String data) {
        return data.split(splitString)[0];
    }
    //endregion

    //region Fields
    private String splitString;
    //endregion
}
