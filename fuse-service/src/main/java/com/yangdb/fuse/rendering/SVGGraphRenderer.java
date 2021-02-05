package com.yangdb.fuse.rendering;

/*-
 * #%L
 * fuse-service
 * %%
 * Copyright (C) 2016 - 2021 The YangDb Graph Database Project
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

import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.attribute.Style;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;

import java.io.File;
import java.io.IOException;

public interface SVGGraphRenderer {
    /**
     * render dot graph model into an SVG image
     * @param dotFormatGraph
     * @return
     * @throws IOException
     */
    static File render(String name, String dotFormatGraph) throws IOException {
        MutableGraph g = new Parser().read(dotFormatGraph);
        g.graphAttrs()
                .add(Color.WHITE.gradient(Color.WHITESMOKE).background().angle(90))
                .nodeAttrs().add(Color.LIGHTBLUE.fill())
                .nodes().forEach(node ->
                node.add(
                        Color.named(node.name().toString()),
                        Style.lineWidth(2), Style.FILLED));
        return Graphviz.fromGraph(g).width(1200).render(Format.SVG_STANDALONE)
                .toFile(File.createTempFile("illustrateGraph_"+name,".svg"));

    }
}
