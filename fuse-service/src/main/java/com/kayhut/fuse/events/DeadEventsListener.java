package com.kayhut.fuse.events;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

/**
 * Created by lior on 21/02/2017.
 */
public class DeadEventsListener {
    @Subscribe
    public void gotDeadEvent(DeadEvent deadEvent) {
        EventBus eventBus = (EventBus) deadEvent.getSource();
        //todo manage dead events - log ?
        System.out.println("Got dead event " + deadEvent.getEvent() + ", from " + eventBus);
    }
}
