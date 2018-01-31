package com.kayhut.fuse.dispatcher.logging;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by roman.margolis on 31/01/2018.
 */
public class HostName {
    public static class Converter extends ClassicConverter {
        @Override
        public String convert(ILoggingEvent iLoggingEvent) {
            return getHostName();
        }
    }

    private static String getHostName() {
        if (hostName == null) {
            try {
                hostName = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                hostName = "UNKNOWN";
            }
        }

        return hostName;
    }

    private static String hostName;
}
