package org.elasticsearch.bootstrap;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Todo
 *  To run this  the integration test separately add the below line to VM arguments in the run configurations.
 *   -ea -Dtests.security.manager=false
 */
public class JarHell {
    private JarHell() {}
    public static void checkJarHell() throws Exception {}
    public static void checkJarHell(Consumer v) throws Exception {}
    public static void checkJarHell(URL urls[]) throws Exception {}
    public static void checkVersionFormat(String targetVersion) {}
    public static void checkJavaVersion(String resource, String targetVersion) {}
    public static Set<URL> parseClassPath() {return new HashSet<URL>(){};}
}
