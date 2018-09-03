package com.kayhut.fuse.executor.elasticsearch;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.kayhut.fuse.dispatcher.modules.ModuleBase;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.jooby.Env;
import org.jooby.internal.RequestScope;
import org.jooby.scope.RequestScoped;
import org.junit.Before;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.mockito.Matchers.argThat;

/**
 * Created by lior.perry on 2/19/2018.
 */
public abstract class BaseModuleInjectionTest {
    protected Injector injector;

    /**
     * init injector via configuration file containing modules class list
     * @param fileName
     */
    protected void init(String fileName) {
        Config config = ConfigFactory.load(fileName);
        List<String> list = config.getStringList("modules.activeProfile");
        List<ModuleBase> collect = list.stream().map(m -> {
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
        init(collect.toArray(new ModuleBase[collect.size()]));
    }


    protected void init(ModuleBase[] modules) {
        Env env = Mockito.mock(Env.class);
        Config conf = Mockito.mock(Config.class);
        Mockito.when(conf.getString(argThat(new ArgumentMatcher<String>() {
            @Override
            public boolean matches(Object argument) {
                return argument.toString().equals("application.profile");
            }
        }))).thenReturn("test");

        Mockito.when(conf.getStringList(argThat(new ArgumentMatcher<String>() {
            @Override
            public boolean matches(Object argument) {
                return argument.toString().equals("modules.test");
            }
        }))).thenReturn(Arrays.stream(modules).map(m -> m.getClass().getName()).collect(toList()));

        injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                try {
                    //bind annotation to request scope
                    this.binder().bindScope(RequestScoped.class, new RequestScope());
                    Arrays.stream(modules).forEach(m -> {
                        try {
                            m.configure(env, conf, this.binder());
                        } catch (Throwable throwable) {
                            throwable.printStackTrace();
                        }
                    });
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        });

    }

    public void setup() {
        injector.injectMembers(this);
    }

}
