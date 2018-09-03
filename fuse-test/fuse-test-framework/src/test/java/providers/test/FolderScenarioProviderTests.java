package providers.test;

import com.kayhut.fuse.test.framework.providers.FileJsonDataProvider;
import javaslang.collection.Stream;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * Created by moti on 3/19/2017.
 */
public class FolderScenarioProviderTests {
    private String docsFile = Paths.get("src", "test", "resources", "IndexDocs", "docs.txt").toString();


    @Test
    public void testDeserializeFolder() throws IOException {

        FileJsonDataProvider provider = new FileJsonDataProvider(docsFile);
        Iterable<Map<String, Object>> scenarioDocuments = provider.getDocuments();
        List<Map<String, Object>> documents = Stream.ofAll(scenarioDocuments).toJavaList();
        Assert.assertEquals(2, documents.size());
    }





}
