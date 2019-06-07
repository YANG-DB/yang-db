package com.kayhut.fuse.executor;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.kayhut.fuse.dispatcher.modules.ModuleBase;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.jooby.Env;
import org.jooby.internal.RequestScope;
import org.jooby.scope.RequestScoped;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;

import javax.inject.Scope;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.util.stream.Collectors.toList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;

/**
 * Created by lior.perry on 2/19/2018.
 */
public abstract class BaseModuleInjectionTest {
    protected Injector injector;
    protected Config config;
    protected List<ModuleBase> modules;

    /**
     * init injector via configuration file containing modules class list
     *
     * @param fileName
     */
    protected void init(String fileName) {
        config = ConfigFactory.load(fileName);
        List<String> list = config.getStringList("modules.activeProfile");
        modules = list.stream().map(m -> {
            try {
                return (ModuleBase) Class.forName(m).newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }).collect(toList());
    }


    protected void init(ModuleBase[] modules) {
        this.modules = Arrays.asList(modules);
        config = Mockito.mock(Config.class);
        Mockito.when(config.getString(argThat(new ArgumentMatcher<String>() {
            @Override
            public boolean matches(Object argument) {
                return argument.toString().equals("application.profile");
            }
        }))).thenReturn("test");

        Mockito.when(config.getStringList(argThat(new ArgumentMatcher<String>() {
            @Override
            public boolean matches(Object argument) {
                return argument.toString().equals("modules.test");
            }
        }))).thenReturn(Arrays.stream(modules).map(m -> m.getClass().getName()).collect(toList()));
    }

    public RequestScope setup() {
        Env env = Mockito.mock(Env.class);
        Mockito.when(env.config()).thenAnswer(invocation -> config);
        final RequestScope requestScope = new RequestScope();
        requestScope.enter(new HashMap<>());

        injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                try {
                    //bind annotation to request scope
                    this.binder().bind(Config.class).toInstance(config);
                    this.binder().bindScope(RequestScoped.class,requestScope);
                    modules.forEach(m -> {
                        try {
                            m.configure(env, config, this.binder());
                        } catch (Throwable throwable) {
                            throwable.printStackTrace();
                        }
                    });
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        });
        injector.injectMembers(this);
        return requestScope;
    }

}
