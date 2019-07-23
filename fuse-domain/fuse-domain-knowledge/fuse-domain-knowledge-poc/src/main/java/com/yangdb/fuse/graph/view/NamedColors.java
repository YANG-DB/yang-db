package com.yangdb.fuse.graph.view;

/*-
 * #%L
 * fuse-domain-knowledge-poc
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

import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;

import static javafx.scene.paint.Color.*;

public class NamedColors {
    private static final Map<String, Color> namedColors =
            createNamedColors();

    private NamedColors() {
    }

    public static Color get(String name) {
        return (Color) namedColors.values().toArray()[Math.abs(name.hashCode() % (namedColors.values().size()-1))];
    }

    private static Map<String, Color> createNamedColors() {
        Map<String, Color> colors = new HashMap<String, Color>(256);

        colors.put("aliceblue", ALICEBLUE);
        colors.put("antiquewhite", ANTIQUEWHITE);
        colors.put("aqua", AQUA);
        colors.put("aquamarine", AQUAMARINE);
        colors.put("azure", AZURE);
        colors.put("beige", BEIGE);
        colors.put("bisque", BISQUE);
        colors.put("black", BLACK);
        colors.put("blanchedalmond", BLANCHEDALMOND);
        colors.put("blue", BLUE);
        colors.put("blueviolet", BLUEVIOLET);
        colors.put("brown", BROWN);
        colors.put("burlywood", BURLYWOOD);
        colors.put("cadetblue", CADETBLUE);
        colors.put("chartreuse", CHARTREUSE);
        colors.put("chocolate", CHOCOLATE);
        colors.put("coral", CORAL);
        colors.put("cornflowerblue", CORNFLOWERBLUE);
        colors.put("cornsilk", CORNSILK);
        colors.put("crimson", CRIMSON);
        colors.put("cyan", CYAN);
        colors.put("darkblue", DARKBLUE);
        colors.put("darkcyan", DARKCYAN);
        colors.put("darkgoldenrod", DARKGOLDENROD);
        colors.put("darkgray", DARKGRAY);
        colors.put("darkgreen", DARKGREEN);
        colors.put("darkgrey", DARKGREY);
        colors.put("darkkhaki", DARKKHAKI);
        colors.put("darkmagenta", DARKMAGENTA);
        colors.put("darkolivegreen", DARKOLIVEGREEN);
        colors.put("darkorange", DARKORANGE);
        colors.put("darkorchid", DARKORCHID);
        colors.put("darkred", DARKRED);
        colors.put("darksalmon", DARKSALMON);
        colors.put("darkseagreen", DARKSEAGREEN);
        colors.put("darkslateblue", DARKSLATEBLUE);
        colors.put("darkslategray", DARKSLATEGRAY);
        colors.put("darkslategrey", DARKSLATEGREY);
        colors.put("darkturquoise", DARKTURQUOISE);
        colors.put("darkviolet", DARKVIOLET);
        colors.put("deeppink", DEEPPINK);
        colors.put("deepskyblue", DEEPSKYBLUE);
        colors.put("dimgray", DIMGRAY);
        colors.put("dimgrey", DIMGREY);
        colors.put("dodgerblue", DODGERBLUE);
        colors.put("firebrick", FIREBRICK);
        colors.put("floralwhite", FLORALWHITE);
        colors.put("forestgreen", FORESTGREEN);
        colors.put("fuchsia", FUCHSIA);
        colors.put("gainsboro", GAINSBORO);
        colors.put("ghostwhite", GHOSTWHITE);
        colors.put("gold", GOLD);
        colors.put("goldenrod", GOLDENROD);
        colors.put("gray", GRAY);
        colors.put("green", GREEN);
        colors.put("greenyellow", GREENYELLOW);
        colors.put("grey", GREY);
        colors.put("honeydew", HONEYDEW);
        colors.put("hotpink", HOTPINK);
        colors.put("indianred", INDIANRED);
        colors.put("indigo", INDIGO);
        colors.put("ivory", IVORY);
        colors.put("khaki", KHAKI);
        colors.put("lavender", LAVENDER);
        colors.put("lavenderblush", LAVENDERBLUSH);
        colors.put("lawngreen", LAWNGREEN);
        colors.put("lemonchiffon", LEMONCHIFFON);
        colors.put("lightblue", LIGHTBLUE);
        colors.put("lightcoral", LIGHTCORAL);
        colors.put("lightcyan", LIGHTCYAN);
        colors.put("lightgoldenrodyellow", LIGHTGOLDENRODYELLOW);
        colors.put("lightgray", LIGHTGRAY);
        colors.put("lightgreen", LIGHTGREEN);
        colors.put("lightgrey", LIGHTGREY);
        colors.put("lightpink", LIGHTPINK);
        colors.put("lightsalmon", LIGHTSALMON);
        colors.put("lightseagreen", LIGHTSEAGREEN);
        colors.put("lightskyblue", LIGHTSKYBLUE);
        colors.put("lightslategray", LIGHTSLATEGRAY);
        colors.put("lightslategrey", LIGHTSLATEGREY);
        colors.put("lightsteelblue", LIGHTSTEELBLUE);
        colors.put("lightyellow", LIGHTYELLOW);
        colors.put("lime", LIME);
        colors.put("limegreen", LIMEGREEN);
        colors.put("linen", LINEN);
        colors.put("magenta", MAGENTA);
        colors.put("maroon", MAROON);
        colors.put("mediumaquamarine", MEDIUMAQUAMARINE);
        colors.put("mediumblue", MEDIUMBLUE);
        colors.put("mediumorchid", MEDIUMORCHID);
        colors.put("mediumpurple", MEDIUMPURPLE);
        colors.put("mediumseagreen", MEDIUMSEAGREEN);
        colors.put("mediumslateblue", MEDIUMSLATEBLUE);
        colors.put("mediumspringgreen", MEDIUMSPRINGGREEN);
        colors.put("mediumturquoise", MEDIUMTURQUOISE);
        colors.put("mediumvioletred", MEDIUMVIOLETRED);
        colors.put("midnightblue", MIDNIGHTBLUE);
        colors.put("mintcream", MINTCREAM);
        colors.put("mistyrose", MISTYROSE);
        colors.put("moccasin", MOCCASIN);
        colors.put("navajowhite", NAVAJOWHITE);
        colors.put("navy", NAVY);
        colors.put("oldlace", OLDLACE);
        colors.put("olive", OLIVE);
        colors.put("olivedrab", OLIVEDRAB);
        colors.put("orange", ORANGE);
        colors.put("orangered", ORANGERED);
        colors.put("orchid", ORCHID);
        colors.put("palegoldenrod", PALEGOLDENROD);
        colors.put("palegreen", PALEGREEN);
        colors.put("paleturquoise", PALETURQUOISE);
        colors.put("palevioletred", PALEVIOLETRED);
        colors.put("papayawhip", PAPAYAWHIP);
        colors.put("peachpuff", PEACHPUFF);
        colors.put("peru", PERU);
        colors.put("pink", PINK);
        colors.put("plum", PLUM);
        colors.put("powderblue", POWDERBLUE);
        colors.put("purple", PURPLE);
        colors.put("red", RED);
        colors.put("rosybrown", ROSYBROWN);
        colors.put("royalblue", ROYALBLUE);
        colors.put("saddlebrown", SADDLEBROWN);
        colors.put("salmon", SALMON);
        colors.put("sandybrown", SANDYBROWN);
        colors.put("seagreen", SEAGREEN);
        colors.put("seashell", SEASHELL);
        colors.put("sienna", SIENNA);
        colors.put("silver", SILVER);
        colors.put("skyblue", SKYBLUE);
        colors.put("slateblue", SLATEBLUE);
        colors.put("slategray", SLATEGRAY);
        colors.put("slategrey", SLATEGREY);
        colors.put("snow", SNOW);
        colors.put("springgreen", SPRINGGREEN);
        colors.put("steelblue", STEELBLUE);
        colors.put("tan", TAN);
        colors.put("teal", TEAL);
        colors.put("thistle", THISTLE);
        colors.put("tomato", TOMATO);
        colors.put("transparent", TRANSPARENT);
        colors.put("turquoise", TURQUOISE);
        colors.put("violet", VIOLET);
        colors.put("wheat", WHEAT);
        colors.put("white", WHITE);
        colors.put("whitesmoke", WHITESMOKE);
        colors.put("yellow", YELLOW);
        colors.put("yellowgreen", YELLOWGREEN);

        return colors;
    }
}
