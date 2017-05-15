package com.kayhut.fuse.services;

import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;

/**
 * Created by Roman on 14/05/2017.
 */
public class GlobalSubscriberExceptionHandler implements SubscriberExceptionHandler{
    //region SubscriberExceptionHandler implementation
    @Override
    public void handleException(Throwable throwable, SubscriberExceptionContext subscriberExceptionContext) {
        int x = 5;
    }
    //endregion
}
