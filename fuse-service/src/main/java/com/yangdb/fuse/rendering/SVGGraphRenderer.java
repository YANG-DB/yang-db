package com.yangdb.fuse.rendering;

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
