package com.yangdb.dragons;

import com.yangdb.dragons.playground.DragonsPlaygroundRegistrar;
import com.yangdb.dragons.services.DragonsExtensionRegistrar;
import com.yangdb.fuse.dispatcher.urlSupplier.DefaultAppUrlSupplier;
import com.yangdb.fuse.extension.YangDbExtension;
import com.yangdb.fuse.services.FuseApp;
import com.yangdb.fuse.services.extensions.YangDbExtensions;
import org.jooby.Jooby;
import org.jooby.Route;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/** A3: the Dragons routes are registered through the extension SPI, with no config entry and no core package. */
public class DragonsExtensionTest {

    @Test
    public void theDragonsExtensionIsDiscoveredWithBothRegistrars() {
        List<YangDbExtension> dragons = YangDbExtensions.load(getClass().getClassLoader()).stream()
                .filter(e -> e instanceof DragonsExtension).collect(Collectors.toList());
        Assert.assertEquals(1, dragons.size());
        Assert.assertEquals(Arrays.asList(DragonsPlaygroundRegistrar.class, DragonsExtensionRegistrar.class),
                dragons.get(0).registrars().stream().map(Object::getClass).collect(Collectors.toList()));
    }

    @Test
    public void theServiceRegistersEveryDragonsRouteOnce() {
        List<String> routes = new ArrayList<>();
        for (Route.Definition route : Jooby.exportRoutes(new FuseApp(new DefaultAppUrlSupplier("/fuse")))) {
            routes.add(route.method() + " " + route.pattern());
        }
        for (String route : Arrays.asList(
                // the playground
                "GET /dragons/swagger", "GET /dragons/swagger/openapi.json", "POST /fuse/dragons/tutorial/load",
                // the extension query routes (A3: never registered before)
                "GET /ext", "POST /fuse/query/clause", "POST /fuse/query/clause/run",
                "GET /fuse/query/cypher/logical/run", "POST /fuse/query/query/v1/logical/run")) {
            Assert.assertEquals(route + " in " + routes, 1, Collections.frequency(routes, route));
        }
        // the generic language routes come after them, so /fuse/query/clause(/run) is not taken for a language
        Assert.assertTrue(routes.indexOf("POST /fuse/query/clause/run") < routes.indexOf("POST /fuse/query/:lang/run"));
    }
}
