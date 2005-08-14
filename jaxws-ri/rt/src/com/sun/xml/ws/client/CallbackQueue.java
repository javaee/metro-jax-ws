/*
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All Rights Reserved.
 */
package com.sun.xml.ws.client;

import com.sun.xml.ws.client.dispatch.ResponseImpl;

import java.rmi.server.UID;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;

public class CallbackQueue {
    private HashMap _waiters;
    private ArrayBlockingQueue<ResponseImpl> _responses;


    public CallbackQueue() {
        _waiters = new HashMap();
        _responses = new ArrayBlockingQueue(50, true);
    }

    public void addWaiter(AsyncHandlerService service) {
        _waiters.put(service.getUID(), service);
    }

    public Object removeWaiter(UID uid) {
        return _waiters.remove(uid);
    }

    public void addResponse(ResponseImpl response) {
        try {
             response.setCallbackService(this);
            _responses.put(response);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void run() {
       
        for (ResponseImpl r : _responses) {
            if (r != null) {
                if (r.isDone()) {
                    AsyncHandlerService service = (AsyncHandlerService) _waiters.get(r.getUID());
                    service.executeHandler(r);
                    _waiters.remove(r.getUID());
                    _responses.remove(r.getUID());
                }
            }
        }
    }

    public void run(UID uid) {
        for (ResponseImpl r : _responses) {
            if (r != null) {
                if (r.getUID() == uid) {
                    if (r.isDone()) {
                        AsyncHandlerService service = (AsyncHandlerService) _waiters.get(r.getUID());
                        service.executeHandler(r);
                        _waiters.remove(r.getUID());
                        _responses.remove(r);
                        return;
                    }
                }
            }
        }
    }
}