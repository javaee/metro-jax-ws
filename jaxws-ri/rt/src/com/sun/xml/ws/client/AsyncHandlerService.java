/*
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All Rights Reserved.
 */
package com.sun.xml.ws.client;

import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;
import java.rmi.server.UID;
import java.util.concurrent.Executor;

public class AsyncHandlerService {

    private AsyncHandler _handler;
    private UID _uid;
    private Executor _executor;

    public AsyncHandlerService(AsyncHandler handler, Executor executor){
        _uid = new UID();
        _handler = handler;
        _executor = executor;
    }

    public synchronized UID getUID(){
        return _uid;
    }

    public void executeHandler(Response response){

       _executor.execute(new WaiterRunnable(response, _handler));
    }

    private class WaiterRunnable implements Runnable {
        Response r;
        AsyncHandler handler;

        private WaiterRunnable(Response res, AsyncHandler ahandler){
            r = res;
            handler = ahandler;
        }

        /**
         * When an object implementing interface <code>Runnable</code> is used
         * to create a thread, starting the thread causes the object's
         * <code>run</code> method to be called in that separately executing
         * thread.
         * <p/>
         * The general contract of the method <code>run</code> is that it may
         * take any action whatsoever.
         *
         * @see Thread#run()
         */
        public void run() {
           handler.handleResponse(r);
        }
    }
}
