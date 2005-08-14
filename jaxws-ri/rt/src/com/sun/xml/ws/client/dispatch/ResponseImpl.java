/*
 * $Id: ResponseImpl.java,v 1.3 2005-08-14 17:55:17 kwalsh Exp $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */
package com.sun.xml.ws.client.dispatch;

import com.sun.xml.ws.client.CallbackQueue;
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


public class ResponseImpl<T> extends FutureTask<T> implements Response<T> {
    private static final Logger logger =
        Logger.getLogger(new StringBuffer().append(com.sun.xml.ws.util.Constants.LoggingDomain).append(".client.dispatch").toString());
    private UID uid;
    private Lock _lock;
    private CallbackQueue _callbackQueue;
    private ResponseContext _responseContext;

    public ResponseImpl(Callable<T> callable) {
        super(callable);
        _lock = new ReentrantLock();
    }

    public ResponseImpl(Runnable runable, T result) {
        super(runable, result);
        _lock = new ReentrantLock();
    }

    //protected method need to overide
    public void setException(Exception ex) {
        _lock.lock();
        try {
            super.setException(ex);
            //notify(_callbackQueue);
        } catch (Exception e) {
        } finally {
            _lock.unlock();
        }
    }

    public void set(T result) {
        _lock.lock();
        try {
            super.set(result);
            //notify(_callbackQueue);
        } catch (Exception e) {
        } finally {
            _lock.unlock();
        }
    }

    /**
     * Gets the contained response context.
     *
     * @return The contained response context. May be <code>null</code> if a
     *         response is not yet available.
     */
    public Map<String, Object> getContext() {
        if (!isDone())
            return null;
        else
            return (Map<String, Object>) (_responseContext = new ResponseContext(null));
    }

    public void setResponseContext(Map context) {
        _responseContext = (ResponseContext) context;
    }

    public synchronized void setUID(UID id) {
        uid = id;
    }

    public synchronized UID getUID() {
        return uid;
    }

    public void setCallbackService(CallbackQueue queue) {
        _callbackQueue = queue;
    }

    //got to lock

    protected void done() {
        _lock.lock();
        try {
            if (!isCancelled())
               _callbackQueue.run();
        } catch (Exception e) {
        } finally {
            _lock.unlock();
        }

    }
}
