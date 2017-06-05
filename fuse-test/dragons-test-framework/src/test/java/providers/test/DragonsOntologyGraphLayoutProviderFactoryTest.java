package providers.test;

import com.kayhut.fuse.unipop.schemaProviders.GraphLayoutProvider;
import com.kayhut.fuse.unipop.schemaProviders.GraphRedundantPropertySchema;
import com.kayhut.test.data.DragonsOntologyGraphLayoutProviderFactory;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Optional;

import static com.kayhut.fuse.model.OntologyTestUtils.createDragonsOntologyLong;

/**
 * Created by liorp on 6/4/2017.
 */
public class DragonsOntologyGraphLayoutProviderFactoryTest {

    @Test
    public void testSingleRedundantProp() throws IOException {
        DragonsOntologyGraphLayoutProviderFactory factory = new DragonsOntologyGraphLayoutProviderFactory("GraphLayoutProviderFactory.conf");
        GraphLayoutProvider layoutProvider = factory.get(createDragonsOntologyLong());
        Optional<GraphRedundantPropertySchema> property = layoutProvider.getRedundantProperty("Freez", new DragonsOntologyGraphLayoutProviderFactory.DragonLayout("name", "name"));

        Assert.assertEquals(property.get().getPropertyRedundantName(),"entityB.name");
        Assert.assertEquals(property.get().getType(),"String");
        Assert.assertEquals(property.get().getName(),"name");
    }

    @Test
    public void testDoubleRedundantProp() throws IOException {
        DragonsOntologyGraphLayoutProviderFactory factory = new DragonsOntologyGraphLayoutProviderFactory("GraphLayoutProviderFactory.conf");
        GraphLayoutProvider layoutProvider = factory.get(createDragonsOntologyLong());
        Optional<GraphRedundantPropertySchema> property = layoutProvider.getRedundantProperty("Fire", new DragonsOntologyGraphLayoutProviderFactory.DragonLayout("color", "color"));

        Assert.assertEquals(property.get().getPropertyRedundantName(),"entityB.color");
        Assert.assertEquals(property.get().getType(),"String");
        Assert.assertEquals(property.get().getName(),"color");

        property = layoutProvider.getRedundantProperty("Fire", new DragonsOntologyGraphLayoutProviderFactory.DragonLayout("name", "name"));
        Assert.assertEquals(property.get().getPropertyRedundantName(),"entityB.name");
        Assert.assertEquals(property.get().getType(),"String");
        Assert.assertEquals(property.get().getName(),"name");
    }

}
