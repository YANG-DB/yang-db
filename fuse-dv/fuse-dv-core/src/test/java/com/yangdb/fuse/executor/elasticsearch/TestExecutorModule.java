package com.yangdb.fuse.executor.elasticsearch;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.PrivateModule;
import com.google.inject.Provider;
import com.yangdb.fuse.dispatcher.modules.ModuleBase;
import com.yangdb.fuse.executor.elasticsearch.logging.LoggingClient;
import com.yangdb.fuse.model.transport.ExecutionScope;
import com.typesafe.config.Config;
import org.opensearch.action.ActionFuture;
import org.opensearch.client.Client;
import org.jooby.Env;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.inject.name.Names.named;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestExecutorModule extends ModuleBase {
    private long timeout;
    private long mockSleepTime;

    public TestExecutorModule(long timeout,long mockSleepTime) {
        this.timeout = timeout;
        this.mockSleepTime = mockSleepTime;
    }

    public void configureInner(Env env, Config conf, Binder binder) {
        binder.bind(ExecutionScope.class).toInstance(new ExecutionScope(timeout));
        bindElasticClient(env, conf, binder);
    }

    protected void bindElasticClient(Env env, Config conf, Binder binder) {
        binder.install(new PrivateModule() {
            @Override
            protected void configure() {
                boolean createMock = conf.hasPath("fuse.elasticsearch.mock") && conf.getBoolean("fuse.elasticsearch.mock");
                this.bindConstant()
                        .annotatedWith(named(ClientProvider.createMockParameter))
                        .to(createMock);
                this.bind(Client.class)
                        .annotatedWith(named(LoggingClient.clientParameter))
                        .toProvider(new MockClientProvider(mockSleepTime)).asEagerSingleton();
                this.bind(Logger.class)
                        .annotatedWith(named(LoggingClient.loggerParameter))
                        .toInstance(LoggerFactory.getLogger(LoggingClient.class));
                this.bind(Client.class)
                        .to(TimeoutClientAdvisor.class);

                this.expose(Client.class);
            }
        });
    }

    public static class MockClientProvider implements Provider<Client> {
        private Client mock;

        @Inject
        public MockClientProvider(long mockSleepTime) {
            mock = mock(Client.class);
            when(mock.search(any())).thenAnswer(invocationOnMock -> {
                Thread.sleep(mockSleepTime);
                return mock(ActionFuture.class);
            });
        }

        @Override
        public Client get() {
            return mock;
        }
    }
}
