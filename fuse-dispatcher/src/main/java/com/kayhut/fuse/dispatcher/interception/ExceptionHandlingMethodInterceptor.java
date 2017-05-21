package com.kayhut.fuse.dispatcher.interception;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * Created by Roman on 17/05/2017.
 */
public class ExceptionHandlingMethodInterceptor implements MethodInterceptor {
    //region MethodInterceptor Implementation
    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        try {
            return methodInvocation.proceed();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }
    //endregion
}
