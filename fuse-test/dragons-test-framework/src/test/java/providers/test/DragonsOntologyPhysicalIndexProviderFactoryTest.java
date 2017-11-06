package providers.test;

import com.kayhut.fuse.unipop.schemaProviders.PhysicalIndexProvider;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.IndexPartitions;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.StaticIndexPartitions;
import com.kayhut.fuse.unipop.schemaProviders.indexPartitions.TimeSeriesIndexPartitions;
import com.kayhut.fuse.unipop.structure.ElementType;
import com.kayhut.test.data.DragonsOntologyPhysicalIndexProviderFactory;
import javaslang.collection.Stream;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;

import static com.kayhut.fuse.model.OntologyTestUtils.createDragonsOntologyLong;

/**
 * Created by liorp on 6/5/2017.
 */
public class DragonsOntologyPhysicalIndexProviderFactoryTest {

    @Test
    @Ignore
    public void testTimeSeriesIndexPartition() throws IOException {
        DragonsOntologyPhysicalIndexProviderFactory factory = new DragonsOntologyPhysicalIndexProviderFactory("DragonsIndexProvider.conf");
        PhysicalIndexProvider provider = factory.get(createDragonsOntologyLong());

        TimeSeriesIndexPartitions freez = (TimeSeriesIndexPartitions) provider.getIndexPartitionsByLabel("Freez", ElementType.edge);

        Assert.assertEquals(Stream.ofAll(freez.getPartitions()).flatMap(IndexPartitions.Partition::getIndices).toJavaSet(),
                new HashSet<>(Arrays.asList("idx_freez_2017-2","idx_freez_2017-4","idx_freez_2017-1")));

        Assert.assertEquals(freez.getDateFormat(), "YYYY-W");
        Assert.assertEquals(freez.getIndexFormat(), "idx_freez_%s");
        Assert.assertEquals(freez.getIndexPrefix(), "idx_freez");
        Assert.assertEquals(freez.getTimeField(), "time");
        Assert.assertEquals(freez.getIndexName(new Date(2017-1900,1,4)), "idx_freez_2017-1");
        Assert.assertEquals(freez.getIndexName(new Date(2017-1900,1,25)), "idx_freez_2017-4");
    }

    @Test
    @Ignore
    public void testStaticIndexPartition() throws IOException {
        DragonsOntologyPhysicalIndexProviderFactory factory = new DragonsOntologyPhysicalIndexProviderFactory("DragonsIndexProvider.conf");
        PhysicalIndexProvider provider = factory.get(createDragonsOntologyLong());

        StaticIndexPartitions person = (StaticIndexPartitions) provider.getIndexPartitionsByLabel("Person", ElementType.vertex);

        Assert.assertEquals(Stream.ofAll(person.getPartitions()).flatMap(IndexPartitions.Partition::getIndices).toJavaSet(), Collections.singleton("persons1"));
    }

}
