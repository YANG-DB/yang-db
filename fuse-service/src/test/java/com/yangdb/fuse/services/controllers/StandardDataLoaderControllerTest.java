package com.yangdb.fuse.services.controllers;

import com.yangdb.fuse.executor.ontology.schema.load.CSVDataLoader;
import com.yangdb.fuse.executor.ontology.schema.load.GraphDataLoader;
import com.yangdb.fuse.model.resourceInfo.FuseError;
import com.yangdb.fuse.model.results.LoadResponse;
import com.yangdb.fuse.model.transport.ContentResponse;
import com.yangdb.fuse.model.transport.Status;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * A load that fails as a whole (KNOWN_ISSUES A2) is reported as a failed load: one failure carrying the reason in the
 * per-commit lists (not "0 of 0"), the top-level error kept for existing consumers, and a failed status.
 */
public class StandardDataLoaderControllerTest {

    @Test
    public void wholeFileCsvFailureIsReportedAsAFailure() throws IOException {
        CSVDataLoader csvLoader = mock(CSVDataLoader.class);
        FuseError.FuseErrorException failure = new FuseError.FuseErrorException("Error while building graph Element from csv row ",
                new IOException("EOF reached before encapsulated token finished"));
        when(csvLoader.load(anyString(), anyString(), anyString(), any())).thenThrow(failure);
        when(csvLoader.load(anyString(), anyString(), any(File.class), any())).thenThrow(failure);
        StandardDataLoaderController controller = new StandardDataLoaderController(null, null, csvLoader, null);

        assertFailedLoad(controller.loadCsv("Dragons", "vertex", "Dragon", "id\n\"broken", GraphDataLoader.Directive.INSERT));
        assertFailedLoad(controller.loadCsv("Dragons", "vertex", "Dragon", new File("Dragon.csv"), GraphDataLoader.Directive.INSERT));
    }

    @Test
    public void wholeFileGraphFailureIsReportedAsAFailure() throws IOException {
        GraphDataLoader graphLoader = mock(GraphDataLoader.class);
        FuseError.FuseErrorException failure = new FuseError.FuseErrorException("Error while building graph Element from csv row ",
                new IOException("EOF reached before encapsulated token finished"));
        when(graphLoader.load(anyString(), any(File.class), any())).thenThrow(failure);
        StandardDataLoaderController controller = new StandardDataLoaderController(null, null, null, graphLoader);

        assertFailedLoad(controller.loadGraph("Dragons", new File("graph.json"), GraphDataLoader.Directive.INSERT));
    }

    private static void assertFailedLoad(ContentResponse<LoadResponse<String, FuseError>> response) {
        Assert.assertNotEquals(Status.OK, response.status());
        Assert.assertNotNull("the top-level error is kept", response.getError());
        Assert.assertNotNull("the load result is reported", response.getData());
        Assert.assertEquals(1, response.getData().getResponses().size());
        LoadResponse.CommitResponse<String, FuseError> commit = response.getData().getResponses().get(0);
        Assert.assertTrue(commit.getSuccesses().isEmpty());
        Assert.assertEquals(1, commit.getFailures().size());
        Assert.assertEquals("EOF reached before encapsulated token finished", commit.getFailures().get(0).getErrorDescription());
    }
}
