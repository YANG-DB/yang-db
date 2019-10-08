package providers.test;

import com.yangdb.fuse.unipop.schemaProviders.GraphLayoutProvider;
import com.yangdb.fuse.unipop.schemaProviders.GraphRedundantPropertySchema;
import com.yangdb.test.BaseITMarker;
import com.yangdb.test.data.DragonsOntologyGraphLayoutProviderFactory;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Optional;

import static com.yangdb.fuse.model.OntologyTestUtils.createDragonsOntologyLong;

/**
 * Created by lior.perry on 6/4/2017.
 */
public class DragonsOntologyGraphLayoutProviderFactoryIT implements BaseITMarker {

    @Test
    public void testSingleRedundantProp() throws IOException {
        DragonsOntologyGraphLayoutProviderFactory factory = new DragonsOntologyGraphLayoutProviderFactory("GraphLayoutProviderFactory.conf");
        GraphLayoutProvider layoutProvider = factory.get(createDragonsOntologyLong());
        Optional<GraphRedundantPropertySchema> property = layoutProvider.getRedundantProperty("Freez", new GraphRedundantPropertySchema.Impl("name", "name", "string"));

        Assert.assertEquals(property.get().getPropertyRedundantName(),"entityB.name");
        Assert.assertEquals(property.get().getType(),"string");
        Assert.assertEquals(property.get().getName(),"name");
        System.out.println("testSingleRedundantProp-Completed");
    }

    @Test
    public void testDoubleRedundantProp() throws IOException {
        DragonsOntologyGraphLayoutProviderFactory factory = new DragonsOntologyGraphLayoutProviderFactory("GraphLayoutProviderFactory.conf");
        GraphLayoutProvider layoutProvider = factory.get(createDragonsOntologyLong());
        Optional<GraphRedundantPropertySchema> property = layoutProvider.getRedundantProperty("Fire", new GraphRedundantPropertySchema.Impl("color", "color", "string"));

        Assert.assertEquals(property.get().getPropertyRedundantName(),"entityB.color");
        Assert.assertEquals(property.get().getType(),"string");
        Assert.assertEquals(property.get().getName(),"color");

        property = layoutProvider.getRedundantProperty("Fire", new GraphRedundantPropertySchema.Impl("name", "name", "string"));
        Assert.assertEquals(property.get().getPropertyRedundantName(),"entityB.name");
        Assert.assertEquals(property.get().getType(),"string");
        Assert.assertEquals(property.get().getName(),"name");
        System.out.println("testDoubleRedundantProp-Completed");
    }

}
