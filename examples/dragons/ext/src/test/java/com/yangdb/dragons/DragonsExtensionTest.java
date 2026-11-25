package com.yangdb.dragons;

import com.yangdb.dragons.playground.DragonsPlaygroundRegistrar;
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

/**
 * A3: the Dragons routes are registered through the extension SPI, with no config entry and no core package.
 * A4: the dead clause routes, {@code /ext} and the duplicates of the core run routes are gone.
 */
public class DragonsExtensionTest {

    @Test
    public void theDragonsExtensionIsDiscoveredWithThePlaygroundRegistrar() {
        List<YangDbExtension> dragons = YangDbExtensions.load(getClass().getClassLoader()).stream()
                .filter(e -> e instanceof DragonsExtension).collect(Collectors.toList());
        Assert.assertEquals(1, dragons.size());
        Assert.assertEquals(Collections.singletonList(DragonsPlaygroundRegistrar.class),
                dragons.get(0).registrars().stream().map(Object::getClass).collect(Collectors.toList()));
    }

    @Test
    public void theServiceRegistersEveryDragonsRouteOnce() {
        List<String> routes = new ArrayList<>();
        for (Route.Definition route : Jooby.exportRoutes(new FuseApp(new DefaultAppUrlSupplier("/fuse")))) {
            routes.add(route.method() + " " + route.pattern());
        }
        for (String route : Arrays.asList(
                "GET /dragons/swagger", "GET /dragons/swagger/openapi.json", "POST /fuse/dragons/tutorial/load")) {
            Assert.assertEquals(route + " in " + routes, 1, Collections.frequency(routes, route));
        }
        // A4: the clause language was the removed Knowledge domain's; the logical/run routes duplicated core routes
        for (String route : Arrays.asList("GET /ext", "POST /fuse/query/clause", "POST /fuse/query/clause/run",
                "GET /fuse/query/cypher/logical/run", "POST /fuse/query/query/v1/logical/run")) {
            Assert.assertFalse(route + " in " + routes, routes.contains(route));
        }
    }
}
