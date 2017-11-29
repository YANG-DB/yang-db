package com.kayhut.fuse.services;

import com.codahale.metrics.MetricRegistry;
import com.google.common.eventbus.EventBus;
import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.kayhut.fuse.dispatcher.context.CursorCreationOperationContext;
import com.kayhut.fuse.dispatcher.context.PageCreationOperationContext;
import com.kayhut.fuse.dispatcher.context.QueryCreationOperationContext;
import com.kayhut.fuse.dispatcher.context.QueryValidationOperationContext;
import com.kayhut.fuse.dispatcher.utils.*;
import com.kayhut.fuse.events.DeadEventsListener;
import com.kayhut.fuse.model.asgQuery.AsgQuery;
import com.kayhut.fuse.dispatcher.descriptors.Descriptor;
import com.kayhut.fuse.model.execution.plan.composite.Plan;
import com.kayhut.fuse.model.execution.plan.PlanWithCost;
import com.kayhut.fuse.unipop.descriptor.GraphTraversalDescriptor;
import com.typesafe.config.Config;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.DefaultGraphTraversal;
import org.jooby.Env;
import org.jooby.Jooby;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lior on 22/02/2017.
 * <p>
 * This module is called by the fuse-service scanner class loader
 */
public class Bootstrap implements Jooby.Module {
    @Override
    public void configure(Env env, Config conf, Binder binder) throws Throwable {
        //register eventBus with service life cycle
        binder.bind(EventBus.class).toInstance(new EventBus(new GlobalSubscriberExceptionHandler()));
        binder.bind(DeadEventsListener.class).toInstance(new DeadEventsListener());

        binder.bindInterceptor(Matchers.any(), Matchers.annotatedWith(TimerAnnotation.class),
                new PerformanceStatistics(binder.getProvider(MetricRegistry.class)));

        //load modules according getTo configuration
        loadModules(env, conf, binder);
    }

    private void loadModules(Env env, Config conf, Binder binder) {
        String profile = conf.getString("application.profile");
        System.out.println("Active Profile " + profile);
        System.out.println("Loading modules: " + "modules." + profile);
        List<String> modules = conf.getStringList("modules." + profile);
        modules.forEach(value -> {
            try {
                Method method = Jooby.Module.class.getMethod("configure", Env.class, Config.class, Binder.class);
                method.invoke(Class.forName(value).newInstance(), env, conf, binder);
            } catch (Exception e) {
                //todo something usefull here - sbould the app break ???
                e.printStackTrace();
            }
        });
    }
}
