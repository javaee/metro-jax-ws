package com.sun.xml.ws.api.pipe;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Fiber.CompletionCallback;
import com.sun.xml.ws.api.pipe.helper.AbstractTubeImpl;
import com.sun.xml.ws.api.server.Container;
import com.sun.xml.ws.api.server.ContainerResolver;

import junit.framework.TestCase;

public class EngineTest extends TestCase {

    public void testEngineString() {
        String id = "engine1";
        Engine e = new Engine(id);
        
        assertEquals(id, e.getId());
        assertEquals(Container.NONE, e.getContainer());
        
        Container testContainer = new Container() {};
        Engine f;
        
        Container old = ContainerResolver.getDefault().enterContainer(testContainer);
        try {
            f = new Engine(id);
        } finally {
            ContainerResolver.getDefault().exitContainer(old);
        }

        assertEquals(id, f.getId());
        assertEquals(testContainer, f.getContainer());
    }

    public void testEngineStringContainer() {
        Container testContainer = new Container() {};
        String id = "engine1";
        Engine e = new Engine(id, testContainer);
        
        assertEquals(id, e.getId());
        assertEquals(testContainer, e.getContainer());
        
        Engine f;
        Container otherContainer = new Container() {};
        
        Container old = ContainerResolver.getDefault().enterContainer(otherContainer);
        try {
            f = new Engine(id, testContainer);
        } finally {
            ContainerResolver.getDefault().exitContainer(old);
        }

        assertEquals(id, f.getId());
        assertEquals(testContainer, f.getContainer());
    }
    
    static class InlineExecutor implements Executor {
        @Override
        public void execute(Runnable command) {
            command.run();
        }
    }
    
    public void testSetExecutorAndCreateFiber() {
        Container testContainer = new Container() {};
        String id = "engine1";
        Engine e = new Engine(id, testContainer);
        
        Executor x = new InlineExecutor();
        e.setExecutor(x);
        
        // Not valid because executor would be wrapped
        //assertEquals(x, e.getExecutor());
        
        Fiber f = e.createFiber();
        
        assertNotNull(f);
        
        TestTube testTube = new TestTube();
        Packet request = new Packet();
        SimpleCompletionCallback callback = new SimpleCompletionCallback();
        
        f.start(testTube, request, callback);
        
        assertEquals(request, callback.response);
        assertNull(callback.error);
        
        List<TubeCall> calls = testTube.getCalls();
        
        assertNotNull(calls);
        assertEquals(1, calls.size());
        
        TubeCall firstCall = calls.get(0);
        
        assertNotNull(firstCall);
        assertEquals(TubeCallType.REQUEST, firstCall.callType);
        assertEquals(testContainer, firstCall.container);
    }
    
    static class SimpleCompletionCallback implements CompletionCallback {
        public Packet response = null;
        public Throwable error = null;

        @Override
        public void onCompletion(@NotNull Packet response) {
            this.response = response;
        }

        @Override
        public void onCompletion(@NotNull Throwable error) {
            this.error = error;
        }
    }
    
    enum TubeCallType {
        REQUEST,
        RESPONSE,
        EXCEPTION
    };
    
    static class TubeCall {
        TubeCallType callType;
        Container container;
        
        public TubeCall(TubeCallType callType, Container container) {
            this.callType = callType;
            this.container = container;
        }
    }

    static class TestTube extends AbstractTubeImpl {
        private List<TubeCall> calls = new ArrayList<TubeCall>();

        public TestTube() {}
        
        public TestTube(TestTube that, TubeCloner cloner) {
            super(that, cloner);
        }
        
        public List<TubeCall> getCalls() { return calls; }
        
        @Override
        @NotNull
        public NextAction processRequest(@NotNull Packet request) {
            Container c = ContainerResolver.getDefault().getContainer();
            calls.add(new TubeCall(TubeCallType.REQUEST, c));
            
            return doReturnWith(request);
        }

        @Override
        @NotNull
        public NextAction processResponse(@NotNull Packet response) {
            Container c = ContainerResolver.getDefault().getContainer();
            calls.add(new TubeCall(TubeCallType.RESPONSE, c));
            
            return doReturnWith(response);
        }

        @Override
        @NotNull
        public NextAction processException(@NotNull Throwable t) {
            Container c = ContainerResolver.getDefault().getContainer();
            calls.add(new TubeCall(TubeCallType.EXCEPTION, c));
            
            return doThrow(t);
        }

        @Override
        public void preDestroy() {
        }

        @Override
        public TestTube copy(TubeCloner cloner) {
            return new TestTube(this, cloner);
        }
        
    }
}
