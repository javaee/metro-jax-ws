/*
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All Rights Reserved.
 */
package com.sun.xml.ws.client;

import com.sun.xml.ws.client.dispatch.ResponseImpl;
import com.sun.xml.ws.spi.runtime.WSConnection;
import com.sun.xml.ws.util.SOAPConnectionUtil;
import com.sun.pept.ept.MessageInfo;
import com.sun.pept.presentation.MessageStruct;

import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;
import javax.xml.ws.WebServiceException;
import javax.xml.soap.SOAPMessage;
import java.rmi.server.UID;
import java.util.concurrent.Executor;
import java.util.concurrent.Callable;

public class AsyncHandlerService {

    private AsyncHandler _handler;
    private UID _uid;
    private Executor _executor;
    private WSFuture wsfuture;
    private Response response;

    public AsyncHandlerService(AsyncHandler handler, Executor executor){
        _uid = new UID();
        _handler = handler;
        _executor = executor;
    }

    public synchronized UID getUID(){
        return _uid;
    }

    public void executeWSFuture(){

       _executor.execute((Runnable) wsfuture);
    }

     public WSFuture<Object> setupAsyncCallback(final Response<Object> result) {
        response = result;

        wsfuture = new WSFuture<Object>(new Callable<Object>() {

            public Object call() throws Exception {
                _handler.handleResponse(response);
                return null;
            }
        });
        return wsfuture;
    }
}
