package com.kayhut.es.plugins.script;

/*-
 * #%L
 * es-plugin
 * %%
 * Copyright (C) 2016 - 2018 kayhut
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

import com.kayhut.es.plugins.script.regex.RegexScriptFactory;
import com.kayhut.es.plugins.script.regex.WildcardScriptFactory;
import org.elasticsearch.common.settings.Setting;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.plugins.ScriptPlugin;
import org.elasticsearch.script.NativeScriptFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Roman on 5/26/2018.
 */
public class CommonScriptPlugin extends Plugin implements ScriptPlugin {
    //region Constructors
    public CommonScriptPlugin(Settings settings) {
    }
    //endregion

    //region SciprtPlugin Implementation
    @Override
    public List<NativeScriptFactory> getNativeScripts() {
        return Arrays.asList(
                new RegexScriptFactory(),
                new WildcardScriptFactory()
        );
    }
    //endregion
}
