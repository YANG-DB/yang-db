package com.kayhut.fuse.utils;


import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(METHOD)
public @interface ConcurrentTest {

    int threads() default 1;
    int requests() default 2;
    long timeoutMillis() default Long.MAX_VALUE;

}