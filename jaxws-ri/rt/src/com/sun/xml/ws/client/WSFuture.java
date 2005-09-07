/*
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All Rights Reserved.
 */
package com.sun.xml.ws.client;


import com.sun.xml.ws.client.ResponseContext;

import javax.xml.ws.Response;
import java.rmi.server.UID;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

/**
 * The <code>Response</code> interface provides methods used to obtain the
 * payload and context a message sent in response to an operation invocation.
 * For asynchronous operation invocations it provides additional methods to
 * check the status of the request.
 *
 * @author JAXWS Development Team
 * @version 1.0
 */


public class WSFuture<T> extends FutureTask<T> {
    private static final Logger logger =
        Logger.getLogger(new StringBuffer().append(com.sun.xml.ws.util.Constants.LoggingDomain).append(".client.dispatch").toString());

    private Lock _lock;

    public WSFuture(Callable<T> callable) {
        super(callable);
        _lock = new ReentrantLock();
    }

    public WSFuture(Runnable runable, T result) {
        super(runable, result);
        _lock = new ReentrantLock();
    }
}
