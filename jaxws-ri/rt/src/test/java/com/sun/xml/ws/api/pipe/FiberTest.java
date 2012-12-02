/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.xml.ws.api.pipe;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.xml.ws.Holder;
import javax.xml.ws.WebServiceException;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.EngineTest.InlineExecutor;
import com.sun.xml.ws.api.pipe.EngineTest.SimpleCompletionCallback;
import com.sun.xml.ws.api.pipe.EngineTest.TestTube;
import com.sun.xml.ws.api.pipe.EngineTest.TubeCall;
import com.sun.xml.ws.api.pipe.EngineTest.TubeCallType;
import com.sun.xml.ws.api.pipe.Fiber.Listener;
import com.sun.xml.ws.api.pipe.helper.AbstractFilterTubeImpl;
import com.sun.xml.ws.api.server.Container;
import com.sun.xml.ws.api.server.ContainerResolver;

import junit.framework.TestCase;

public class FiberTest extends TestCase {
    private Engine engine = null;
    private Engine threadPoolEngine = null;
    private Container testContainer = null;
    private SimpleInlineExecutor executor = null;
    
    public void setUp() {
        testContainer = new Container() {};
        String id = "engine1";
        executor = new SimpleInlineExecutor();
        engine = new Engine(id, testContainer, executor);
        threadPoolEngine = new Engine(id, testContainer);
    }
    
    private static class SimpleInlineExecutor extends InlineExecutor {
        private ThreadLocal<Boolean> isInExecutor = new ThreadLocal<Boolean>() {
            @Override
            protected Boolean initialValue() {
                return Boolean.FALSE;
            }
        };

        @Override
        public void execute(Runnable command) {
            isInExecutor.set(Boolean.TRUE);
            try {
                super.execute(command);
            } finally {
                isInExecutor.set(Boolean.FALSE);
            }
        }
        
        public Boolean isInExecutor() {
            return isInExecutor.get();
        }
    }

    @SuppressWarnings("deprecation")
    private static class TestListener implements Listener {
        @Override
        public void fiberSuspended(Fiber fiber) {
        }

        @Override
        public void fiberResumed(Fiber fiber) {
        }
    }
    
    @SuppressWarnings("deprecation")
    public void testAddRemoveListener() {
        Fiber fiber = engine.createFiber();
        
        Listener testListener = new TestListener();
        
        List<Listener> listeners = fiber.getCurrentListeners();
        
        assertNotNull(listeners);
        assertTrue(listeners.isEmpty());
        
        fiber.addListener(testListener);
        listeners = fiber.getCurrentListeners();
        
        assertNotNull(listeners);
        assertEquals(1, listeners.size());
        assertTrue(listeners.contains(testListener));  
        
        Listener testListener2 = new TestListener();
        fiber.addListener(testListener2);
        listeners = fiber.getCurrentListeners();
        
        assertNotNull(listeners);
        assertEquals(2, listeners.size());
        assertTrue(listeners.contains(testListener2));  
        
        fiber.removeListener(testListener);
        listeners = fiber.getCurrentListeners();
        
        assertNotNull(listeners);
        assertEquals(1, listeners.size());
        assertTrue(listeners.contains(testListener2));  
    }

    public void testStartTubePacketCompletionCallback() {
        final Holder<Boolean> isInExecutor = new Holder<Boolean>(Boolean.FALSE);
        TestTube testTube = new TestTube() {

            @Override
            @NotNull
            public NextAction processRequest(@NotNull Packet request) {
                isInExecutor.value = executor.isInExecutor();
                return super.processRequest(request);
            }
        };
        Packet request = new Packet();
        SimpleCompletionCallback callback = new SimpleCompletionCallback();
        
        Fiber fiber = engine.createFiber();
        
        assertNotNull(fiber);
        
        fiber.start(testTube, request, callback);
        
        assertEquals(request, callback.response);
        assertNull(callback.error);
        
        List<TubeCall> calls = testTube.getCalls();
        
        assertNotNull(calls);
        assertEquals(1, calls.size());
        
        TubeCall firstCall = calls.get(0);
        
        assertNotNull(firstCall);
        assertEquals(TubeCallType.REQUEST, firstCall.callType);
        assertEquals(testContainer, firstCall.container);
        assertTrue(isInExecutor.value);
    }

    public void testStartTubePacketCompletionCallbackBoolean() {
        final Holder<Boolean> isInExecutor = new Holder<Boolean>(Boolean.FALSE);
        TestTube testTube = new TestTube() {

            @Override
            @NotNull
            public NextAction processRequest(@NotNull Packet request) {
                isInExecutor.value = executor.isInExecutor();
                return super.processRequest(request);
            }
        };
        Packet request = new Packet();
        SimpleCompletionCallback callback = new SimpleCompletionCallback();
        
        Fiber fiber = engine.createFiber();
        
        assertNotNull(fiber);
        
        fiber.start(testTube, request, callback, true);
        
        assertEquals(request, callback.response);
        assertNull(callback.error);
        
        List<TubeCall> calls = testTube.getCalls();
        
        assertNotNull(calls);
        assertEquals(1, calls.size());
        
        TubeCall firstCall = calls.get(0);
        
        assertNotNull(firstCall);
        assertEquals(TubeCallType.REQUEST, firstCall.callType);
        assertEquals(testContainer, firstCall.container);
        assertFalse(isInExecutor.value);
    }
    
    //////////////////////////////
    // NextAction / Flow-controlling tests
    
    static class FilterTestTube extends AbstractFilterTubeImpl {
        private List<TubeCall> calls = new ArrayList<TubeCall>();

        public FilterTestTube(Tube next) {
            super(next);
        }
        
        public FilterTestTube(FilterTestTube that, TubeCloner cloner) {
            super(that, cloner);
        }
        
        public List<TubeCall> getCalls() { return calls; }
        
        @Override
        @NotNull
        public NextAction processRequest(@NotNull Packet request) {
            Container c = ContainerResolver.getDefault().getContainer();
            calls.add(new TubeCall(TubeCallType.REQUEST, request, c));
            
            return doInvoke(next, request);
        }

        @Override
        @NotNull
        public NextAction processResponse(@NotNull Packet response) {
            Container c = ContainerResolver.getDefault().getContainer();
            calls.add(new TubeCall(TubeCallType.RESPONSE, response, c));
            
            return doReturnWith(response);
        }

        @Override
        @NotNull
        public NextAction processException(@NotNull Throwable t) {
            Packet packet = Fiber.current().getPacket();
            Container c = ContainerResolver.getDefault().getContainer();
            calls.add(new TubeCall(TubeCallType.EXCEPTION, packet, c));
            
            return doThrow(t);
        }

        @Override
        public void preDestroy() {
        }

        @Override
        public FilterTestTube copy(TubeCloner cloner) {
            return new FilterTestTube(this, cloner);
        }
        
    }

    public void testNextActionInvoke() {
        TestTube tubeC = new TestTube();
        FilterTestTube tubeB = new FilterTestTube(tubeC);
        FilterTestTube tubeA = new FilterTestTube(tubeB);
        
        Packet request = new Packet();
        SimpleCompletionCallback callback = new SimpleCompletionCallback();
        
        Fiber fiber = engine.createFiber();
        
        assertNotNull(fiber);
        
        fiber.start(tubeA, request, callback, true);
        
        assertEquals(request, callback.response);
        assertNull(callback.error);
        
        List<TubeCall> calls = tubeA.getCalls();
        
        assertNotNull(calls);
        assertEquals(2, calls.size());
        
        TubeCall firstCall = calls.get(0);
        
        assertNotNull(firstCall);
        assertEquals(TubeCallType.REQUEST, firstCall.callType);
        assertEquals(testContainer, firstCall.container);
        
        TubeCall secondCall = calls.get(1);
        
        assertNotNull(secondCall);
        assertEquals(TubeCallType.RESPONSE, secondCall.callType);
        assertEquals(testContainer, secondCall.container);

        calls = tubeB.getCalls();
        
        assertNotNull(calls);
        assertEquals(2, calls.size());
        
        firstCall = calls.get(0);
        
        assertNotNull(firstCall);
        assertEquals(TubeCallType.REQUEST, firstCall.callType);
        assertEquals(testContainer, firstCall.container);
        
        secondCall = calls.get(1);
        
        assertNotNull(secondCall);
        assertEquals(TubeCallType.RESPONSE, secondCall.callType);
        assertEquals(testContainer, secondCall.container);

        calls = tubeC.getCalls();
        
        assertNotNull(calls);
        assertEquals(1, calls.size());
        
        firstCall = calls.get(0);
        
        assertNotNull(firstCall);
        assertEquals(TubeCallType.REQUEST, firstCall.callType);
        assertEquals(testContainer, firstCall.container);
    }

    public void testNextActionInvokeAndForget() {
        TestTube tubeC = new TestTube();
        FilterTestTube tubeB = new FilterTestTube(tubeC) {
            @Override
            @NotNull
            public NextAction processRequest(@NotNull Packet request) {
                super.processRequest(request);
                return doInvokeAndForget(next, request);
            }
        };
        FilterTestTube tubeA = new FilterTestTube(tubeB);
        
        Packet request = new Packet();
        SimpleCompletionCallback callback = new SimpleCompletionCallback();
        
        Fiber fiber = engine.createFiber();
        
        assertNotNull(fiber);
        
        fiber.start(tubeA, request, callback, true);
        
        assertEquals(request, callback.response);
        assertNull(callback.error);
        
        List<TubeCall> calls = tubeA.getCalls();
        
        assertNotNull(calls);
        assertEquals(2, calls.size());
        
        TubeCall firstCall = calls.get(0);
        
        assertNotNull(firstCall);
        assertEquals(TubeCallType.REQUEST, firstCall.callType);
        assertEquals(testContainer, firstCall.container);
        
        TubeCall secondCall = calls.get(1);
        
        assertNotNull(secondCall);
        assertEquals(TubeCallType.RESPONSE, secondCall.callType);
        assertEquals(testContainer, secondCall.container);

        calls = tubeB.getCalls();
        
        assertNotNull(calls);
        assertEquals(1, calls.size());
        
        firstCall = calls.get(0);
        
        assertNotNull(firstCall);
        assertEquals(TubeCallType.REQUEST, firstCall.callType);
        assertEquals(testContainer, firstCall.container);
        
        calls = tubeC.getCalls();
        
        assertNotNull(calls);
        assertEquals(1, calls.size());
        
        firstCall = calls.get(0);
        
        assertNotNull(firstCall);
        assertEquals(TubeCallType.REQUEST, firstCall.callType);
        assertEquals(testContainer, firstCall.container);
    }

    public void testNextActionReturn() {
        TestTube tubeC = new TestTube();
        FilterTestTube tubeB = new FilterTestTube(tubeC) {
            @Override
            @NotNull
            public NextAction processRequest(@NotNull Packet request) {
                super.processRequest(request);
                return doReturnWith(request);
            }
        };
        FilterTestTube tubeA = new FilterTestTube(tubeB);
        
        Packet request = new Packet();
        SimpleCompletionCallback callback = new SimpleCompletionCallback();
        
        Fiber fiber = engine.createFiber();
        
        assertNotNull(fiber);
        
        fiber.start(tubeA, request, callback, true);
        
        assertEquals(request, callback.response);
        assertNull(callback.error);
        
        List<TubeCall> calls = tubeA.getCalls();
        
        assertNotNull(calls);
        assertEquals(2, calls.size());
        
        TubeCall firstCall = calls.get(0);
        
        assertNotNull(firstCall);
        assertEquals(TubeCallType.REQUEST, firstCall.callType);
        assertEquals(testContainer, firstCall.container);
        
        TubeCall secondCall = calls.get(1);
        
        assertNotNull(secondCall);
        assertEquals(TubeCallType.RESPONSE, secondCall.callType);
        assertEquals(testContainer, secondCall.container);

        calls = tubeB.getCalls();
        
        assertNotNull(calls);
        assertEquals(1, calls.size());
        
        firstCall = calls.get(0);
        
        assertNotNull(firstCall);
        assertEquals(TubeCallType.REQUEST, firstCall.callType);
        assertEquals(testContainer, firstCall.container);
        
        calls = tubeC.getCalls();
        
        assertNotNull(calls);
        assertEquals(0, calls.size());
    }

    public void testNextActionThrow() {
        TestTube tubeC = new TestTube() {
            @Override
            @NotNull
            public NextAction processRequest(@NotNull Packet request) {
                super.processRequest(request);
                throw new RuntimeException();
            }
        };
        FilterTestTube tubeB = new FilterTestTube(tubeC);
        FilterTestTube tubeA = new FilterTestTube(tubeB);
        
        Packet request = new Packet();
        SimpleCompletionCallback callback = new SimpleCompletionCallback();
        
        Fiber fiber = engine.createFiber();
        
        assertNotNull(fiber);
        
        fiber.start(tubeA, request, callback, true);
        
        assertNull(callback.response);
        assertTrue(callback.error instanceof RuntimeException);
        
        List<TubeCall> calls = tubeA.getCalls();
        
        assertNotNull(calls);
        assertEquals(2, calls.size());
        
        TubeCall firstCall = calls.get(0);
        
        assertNotNull(firstCall);
        assertEquals(TubeCallType.REQUEST, firstCall.callType);
        assertEquals(testContainer, firstCall.container);
        
        TubeCall secondCall = calls.get(1);
        
        assertNotNull(secondCall);
        assertEquals(TubeCallType.EXCEPTION, secondCall.callType);
        assertEquals(testContainer, secondCall.container);

        calls = tubeB.getCalls();
        
        assertNotNull(calls);
        assertEquals(2, calls.size());
        
        firstCall = calls.get(0);
        
        assertNotNull(firstCall);
        assertEquals(TubeCallType.REQUEST, firstCall.callType);
        assertEquals(testContainer, firstCall.container);
        
        secondCall = calls.get(1);
        
        assertNotNull(secondCall);
        assertEquals(TubeCallType.EXCEPTION, secondCall.callType);
        assertEquals(testContainer, secondCall.container);

        calls = tubeC.getCalls();
        
        assertNotNull(calls);
        assertEquals(1, calls.size());
        
        firstCall = calls.get(0);
        
        assertNotNull(firstCall);
        assertEquals(TubeCallType.REQUEST, firstCall.callType);
        assertEquals(testContainer, firstCall.container);
    }
    
    public void testNextActionAbortResponse() {
        TestTube tubeC = new TestTube() {
            @Override
            @NotNull
            public NextAction processRequest(@NotNull Packet request) {
                super.processRequest(request);
                NextAction na = new NextAction();
                na.abortResponse(request);
                return na;
            }
        };
        FilterTestTube tubeB = new FilterTestTube(tubeC);
        FilterTestTube tubeA = new FilterTestTube(tubeB);
        
        Packet request = new Packet();
        SimpleCompletionCallback callback = new SimpleCompletionCallback();
        
        Fiber fiber = engine.createFiber();
        
        assertNotNull(fiber);
        
        fiber.start(tubeA, request, callback, true);
        
        assertEquals(request, callback.response);
        assertNull(callback.error);
        
        List<TubeCall> calls = tubeA.getCalls();
        
        assertNotNull(calls);
        assertEquals(1, calls.size());
        
        TubeCall firstCall = calls.get(0);
        
        assertNotNull(firstCall);
        assertEquals(TubeCallType.REQUEST, firstCall.callType);
        assertEquals(testContainer, firstCall.container);
        
        calls = tubeB.getCalls();
        
        assertNotNull(calls);
        assertEquals(1, calls.size());
        
        firstCall = calls.get(0);
        
        assertNotNull(firstCall);
        assertEquals(TubeCallType.REQUEST, firstCall.callType);
        assertEquals(testContainer, firstCall.container);
        
        calls = tubeC.getCalls();
        
        assertNotNull(calls);
        assertEquals(1, calls.size());
        
        firstCall = calls.get(0);
        
        assertNotNull(firstCall);
        assertEquals(TubeCallType.REQUEST, firstCall.callType);
        assertEquals(testContainer, firstCall.container);
    }

    public void testNextActionThrowAbortResponse() {
        TestTube tubeC = new TestTube() {
            @Override
            @NotNull
            public NextAction processRequest(@NotNull Packet request) {
                super.processRequest(request);
                NextAction na = new NextAction();
                na.throwExceptionAbortResponse(new RuntimeException());
                return na;
            }
        };
        FilterTestTube tubeB = new FilterTestTube(tubeC);
        FilterTestTube tubeA = new FilterTestTube(tubeB);
        
        Packet request = new Packet();
        SimpleCompletionCallback callback = new SimpleCompletionCallback();
        
        Fiber fiber = engine.createFiber();
        
        assertNotNull(fiber);
        
        fiber.start(tubeA, request, callback, true);
        
        assertNull(callback.response);
        assertTrue(callback.error instanceof RuntimeException);
        
        List<TubeCall> calls = tubeA.getCalls();
        
        assertNotNull(calls);
        assertEquals(1, calls.size());
        
        TubeCall firstCall = calls.get(0);
        
        assertNotNull(firstCall);
        assertEquals(TubeCallType.REQUEST, firstCall.callType);
        assertEquals(testContainer, firstCall.container);
        
        calls = tubeB.getCalls();
        
        assertNotNull(calls);
        assertEquals(1, calls.size());
        
        firstCall = calls.get(0);
        
        assertNotNull(firstCall);
        assertEquals(TubeCallType.REQUEST, firstCall.callType);
        assertEquals(testContainer, firstCall.container);
        
        calls = tubeC.getCalls();
        
        assertNotNull(calls);
        assertEquals(1, calls.size());
        
        firstCall = calls.get(0);
        
        assertNotNull(firstCall);
        assertEquals(TubeCallType.REQUEST, firstCall.callType);
        assertEquals(testContainer, firstCall.container);
    }
    
    //////////////////////////////
    // Suspend / resume tests
    
    public void testSuspendNextRunnableResume() throws InterruptedException {
        final Semaphore atSuspend = new Semaphore(0);
        final Semaphore checkCompleted = new Semaphore(0);
        final Semaphore atEnd = new Semaphore(0);
        
        final TestTube tubeC = new TestTube();
        final FilterTestTube tubeB = new FilterTestTube(tubeC) {
            @Override
            @NotNull
            public NextAction processRequest(@NotNull Packet request) {
                super.processRequest(request);
                return doSuspend(next, new Runnable() {
                    @Override
                    public void run() {
                        atSuspend.release();
                        try {
                            checkCompleted.acquire();
                        } catch (InterruptedException e) {
                        }
                    }
                });
            }
        };
        final FilterTestTube tubeA = new FilterTestTube(tubeB);
        
        final Packet request = new Packet();
        final SimpleCompletionCallback callback = new SimpleCompletionCallback() {
            @Override
            public void onCompletion(@NotNull Packet response) {
                super.onCompletion(response);
                atEnd.release();
            }

            @Override
            public void onCompletion(@NotNull Throwable error) {
                super.onCompletion(error);
                atEnd.release();
            }
        };
        
        final Fiber fiber = threadPoolEngine.createFiber();
        
        assertNotNull(fiber);
        
        fiber.start(tubeA, request, callback);
        
        if (!atSuspend.tryAcquire(3, TimeUnit.MINUTES))
            fail("timeout");
        
        assertEquals(0, atEnd.availablePermits()); // ensure test thread really blocked
        
        // thread is suspended
        
        assertNull(callback.response);
        assertNull(callback.error);
        
        List<TubeCall> calls = tubeA.getCalls();
        
        assertNotNull(calls);
        assertEquals(1, calls.size());
        
        TubeCall firstCall = calls.get(0);
        
        assertNotNull(firstCall);
        assertEquals(TubeCallType.REQUEST, firstCall.callType);
        assertEquals(testContainer, firstCall.container);
        
        calls = tubeB.getCalls();
        
        assertNotNull(calls);
        assertEquals(1, calls.size());
        
        firstCall = calls.get(0);
        
        assertNotNull(firstCall);
        assertEquals(TubeCallType.REQUEST, firstCall.callType);
        assertEquals(testContainer, firstCall.container);
        
        calls = tubeC.getCalls();
        
        assertNotNull(calls);
        assertEquals(0, calls.size());
        
        checkCompleted.release();
        fiber.resume(request);
        
        if (!atEnd.tryAcquire(3, TimeUnit.MINUTES))
            fail("timeout");

        assertEquals(request, callback.response);
        assertNull(callback.error);
        
        calls = tubeA.getCalls();
        
        assertNotNull(calls);
        assertEquals(2, calls.size());
        
        firstCall = calls.get(0);
        
        assertNotNull(firstCall);
        assertEquals(TubeCallType.REQUEST, firstCall.callType);
        assertEquals(testContainer, firstCall.container);
        
        TubeCall secondCall = calls.get(1);
        
        assertNotNull(secondCall);
        assertEquals(TubeCallType.RESPONSE, secondCall.callType);
        assertEquals(testContainer, secondCall.container);

        calls = tubeB.getCalls();
        
        assertNotNull(calls);
        assertEquals(2, calls.size());
        
        firstCall = calls.get(0);
        
        assertNotNull(firstCall);
        assertEquals(TubeCallType.REQUEST, firstCall.callType);
        assertEquals(testContainer, firstCall.container);
        
        secondCall = calls.get(1);
        
        assertNotNull(secondCall);
        assertEquals(TubeCallType.RESPONSE, secondCall.callType);
        assertEquals(testContainer, secondCall.container);

        calls = tubeC.getCalls();
        
        assertNotNull(calls);
        assertEquals(1, calls.size());
        
        firstCall = calls.get(0);
        
        assertNotNull(firstCall);
        assertEquals(TubeCallType.REQUEST, firstCall.callType);
        assertEquals(testContainer, firstCall.container);
    }

    public void testSuspendRunnableResume() throws InterruptedException {
        final Semaphore atSuspend = new Semaphore(0);
        final Semaphore checkCompleted = new Semaphore(0);
        final Semaphore atEnd = new Semaphore(0);
        
        final TestTube tubeC = new TestTube();
        final FilterTestTube tubeB = new FilterTestTube(tubeC) {
            @Override
            @NotNull
            public NextAction processRequest(@NotNull Packet request) {
                super.processRequest(request);
                return doSuspend(new Runnable() {
                    @Override
                    public void run() {
                        atSuspend.release();
                        try {
                            checkCompleted.acquire();
                        } catch (InterruptedException e) {
                        }
                    }
                });
            }
        };
        final FilterTestTube tubeA = new FilterTestTube(tubeB);
        
        final Packet request = new Packet();
        final SimpleCompletionCallback callback = new SimpleCompletionCallback() {
            @Override
            public void onCompletion(@NotNull Packet response) {
                super.onCompletion(response);
                atEnd.release();
            }

            @Override
            public void onCompletion(@NotNull Throwable error) {
                super.onCompletion(error);
                atEnd.release();
            }
        };
        
        final Fiber fiber = threadPoolEngine.createFiber();
        
        assertNotNull(fiber);
        
        fiber.start(tubeA, request, callback);
        
        if (!atSuspend.tryAcquire(3, TimeUnit.MINUTES))
            fail("timeout");
        
        assertEquals(0, atEnd.availablePermits()); // ensure test thread really blocked
        
        // thread is suspended
        
        assertNull(callback.response);
        assertNull(callback.error);
        
        List<TubeCall> calls = tubeA.getCalls();
        
        assertNotNull(calls);
        assertEquals(1, calls.size());
        
        TubeCall firstCall = calls.get(0);
        
        assertNotNull(firstCall);
        assertEquals(TubeCallType.REQUEST, firstCall.callType);
        assertEquals(testContainer, firstCall.container);
        
        calls = tubeB.getCalls();
        
        assertNotNull(calls);
        assertEquals(1, calls.size());
        
        firstCall = calls.get(0);
        
        assertNotNull(firstCall);
        assertEquals(TubeCallType.REQUEST, firstCall.callType);
        assertEquals(testContainer, firstCall.container);
        
        calls = tubeC.getCalls();
        
        assertNotNull(calls);
        assertEquals(0, calls.size());
        
        checkCompleted.release();
        fiber.resume(request);
        
        if (!atEnd.tryAcquire(3, TimeUnit.MINUTES))
            fail("timeout");

        assertEquals(request, callback.response);
        assertNull(callback.error);
        
        calls = tubeA.getCalls();
        
        assertNotNull(calls);
        assertEquals(2, calls.size());
        
        firstCall = calls.get(0);
        
        assertNotNull(firstCall);
        assertEquals(TubeCallType.REQUEST, firstCall.callType);
        assertEquals(testContainer, firstCall.container);
        
        TubeCall secondCall = calls.get(1);
        
        assertNotNull(secondCall);
        assertEquals(TubeCallType.RESPONSE, secondCall.callType);
        assertEquals(testContainer, secondCall.container);

        calls = tubeB.getCalls();
        
        assertNotNull(calls);
        assertEquals(2, calls.size());
        
        firstCall = calls.get(0);
        
        assertNotNull(firstCall);
        assertEquals(TubeCallType.REQUEST, firstCall.callType);
        assertEquals(testContainer, firstCall.container);
        
        secondCall = calls.get(1);
        
        assertNotNull(secondCall);
        assertEquals(TubeCallType.RESPONSE, secondCall.callType);
        assertEquals(testContainer, secondCall.container);

        calls = tubeC.getCalls();
        
        assertNotNull(calls);
        assertEquals(0, calls.size());
    }

    public void testSuspendNextRunnableResumeThrowable() throws InterruptedException {
        final Semaphore atSuspend = new Semaphore(0);
        final Semaphore checkCompleted = new Semaphore(0);
        final Semaphore atEnd = new Semaphore(0);
        
        final TestTube tubeC = new TestTube();
        final FilterTestTube tubeB = new FilterTestTube(tubeC) {
            @Override
            @NotNull
            public NextAction processRequest(@NotNull Packet request) {
                super.processRequest(request);
                return doSuspend(next, new Runnable() {
                    @Override
                    public void run() {
                        atSuspend.release();
                        try {
                            checkCompleted.acquire();
                        } catch (InterruptedException e) {
                        }
                    }
                });
            }
        };
        final FilterTestTube tubeA = new FilterTestTube(tubeB);
        
        final Packet request = new Packet();
        final SimpleCompletionCallback callback = new SimpleCompletionCallback() {
            @Override
            public void onCompletion(@NotNull Packet response) {
                super.onCompletion(response);
                atEnd.release();
            }

            @Override
            public void onCompletion(@NotNull Throwable error) {
                super.onCompletion(error);
                atEnd.release();
            }
        };
        
        final Fiber fiber = threadPoolEngine.createFiber();
        
        assertNotNull(fiber);
        
        fiber.start(tubeA, request, callback);
        
        if (!atSuspend.tryAcquire(3, TimeUnit.MINUTES))
            fail("timeout");
        
        assertEquals(0, atEnd.availablePermits()); // ensure test thread really blocked
        
        // thread is suspended
        
        assertNull(callback.response);
        assertNull(callback.error);
        
        List<TubeCall> calls = tubeA.getCalls();
        
        assertNotNull(calls);
        assertEquals(1, calls.size());
        
        TubeCall firstCall = calls.get(0);
        
        assertNotNull(firstCall);
        assertEquals(TubeCallType.REQUEST, firstCall.callType);
        assertEquals(testContainer, firstCall.container);
        
        calls = tubeB.getCalls();
        
        assertNotNull(calls);
        assertEquals(1, calls.size());
        
        firstCall = calls.get(0);
        
        assertNotNull(firstCall);
        assertEquals(TubeCallType.REQUEST, firstCall.callType);
        assertEquals(testContainer, firstCall.container);
        
        calls = tubeC.getCalls();
        
        assertNotNull(calls);
        assertEquals(0, calls.size());
        
        checkCompleted.release();
        fiber.resume(new RuntimeException());
        
        if (!atEnd.tryAcquire(3, TimeUnit.MINUTES))
            fail("timeout");

        assertNull(callback.response);
        assertTrue(callback.error instanceof RuntimeException);
        
        calls = tubeA.getCalls();
        
        assertNotNull(calls);
        assertEquals(2, calls.size());
        
        firstCall = calls.get(0);
        
        assertNotNull(firstCall);
        assertEquals(TubeCallType.REQUEST, firstCall.callType);
        assertEquals(testContainer, firstCall.container);
        
        TubeCall secondCall = calls.get(1);
        
        assertNotNull(secondCall);
        assertEquals(TubeCallType.EXCEPTION, secondCall.callType);
        assertEquals(testContainer, secondCall.container);

        calls = tubeB.getCalls();
        
        assertNotNull(calls);
        assertEquals(2, calls.size());
        
        firstCall = calls.get(0);
        
        assertNotNull(firstCall);
        assertEquals(TubeCallType.REQUEST, firstCall.callType);
        assertEquals(testContainer, firstCall.container);
        
        secondCall = calls.get(1);
        
        assertNotNull(secondCall);
        assertEquals(TubeCallType.EXCEPTION, secondCall.callType);
        assertEquals(testContainer, secondCall.container);

        calls = tubeC.getCalls();
        
        assertNotNull(calls);
        assertEquals(0, calls.size());
    }

    public void testSuspendNextRunnableResumeThrowablePacket() throws InterruptedException {
        final Semaphore atSuspend = new Semaphore(0);
        final Semaphore checkCompleted = new Semaphore(0);
        final Semaphore atEnd = new Semaphore(0);
        
        final TestTube tubeC = new TestTube();
        final FilterTestTube tubeB = new FilterTestTube(tubeC) {
            @Override
            @NotNull
            public NextAction processRequest(@NotNull Packet request) {
                super.processRequest(request);
                return doSuspend(next, new Runnable() {
                    @Override
                    public void run() {
                        atSuspend.release();
                        try {
                            checkCompleted.acquire();
                        } catch (InterruptedException e) {
                        }
                    }
                });
            }
        };
        final FilterTestTube tubeA = new FilterTestTube(tubeB);
        
        final Packet request = new Packet();
        final SimpleCompletionCallback callback = new SimpleCompletionCallback() {
            @Override
            public void onCompletion(@NotNull Packet response) {
                super.onCompletion(response);
                atEnd.release();
            }

            @Override
            public void onCompletion(@NotNull Throwable error) {
                super.onCompletion(error);
                atEnd.release();
            }
        };
        
        final Fiber fiber = threadPoolEngine.createFiber();
        
        assertNotNull(fiber);
        
        fiber.start(tubeA, request, callback);
        
        if (!atSuspend.tryAcquire(3, TimeUnit.MINUTES))
            fail("timeout");
        
        assertEquals(0, atEnd.availablePermits()); // ensure test thread really blocked
        
        // thread is suspended
        
        assertNull(callback.response);
        assertNull(callback.error);
        
        List<TubeCall> calls = tubeA.getCalls();
        
        assertNotNull(calls);
        assertEquals(1, calls.size());
        
        TubeCall firstCall = calls.get(0);
        
        assertNotNull(firstCall);
        assertEquals(TubeCallType.REQUEST, firstCall.callType);
        assertEquals(testContainer, firstCall.container);
        
        calls = tubeB.getCalls();
        
        assertNotNull(calls);
        assertEquals(1, calls.size());
        
        firstCall = calls.get(0);
        
        assertNotNull(firstCall);
        assertEquals(TubeCallType.REQUEST, firstCall.callType);
        assertEquals(testContainer, firstCall.container);
        
        calls = tubeC.getCalls();
        
        assertNotNull(calls);
        assertEquals(0, calls.size());
        
        checkCompleted.release();
        
        Packet secondPacket = new Packet();
        fiber.resume(new RuntimeException(), secondPacket);
        
        if (!atEnd.tryAcquire(3, TimeUnit.MINUTES))
            fail("timeout");

        assertNull(callback.response);
        assertTrue(callback.error instanceof RuntimeException);
        
        calls = tubeA.getCalls();
        
        assertNotNull(calls);
        assertEquals(2, calls.size());
        
        firstCall = calls.get(0);
        
        assertNotNull(firstCall);
        assertEquals(TubeCallType.REQUEST, firstCall.callType);
        assertEquals(testContainer, firstCall.container);
        
        TubeCall secondCall = calls.get(1);
        
        assertNotNull(secondCall);
        assertEquals(TubeCallType.EXCEPTION, secondCall.callType);
        assertEquals(testContainer, secondCall.container);
        assertEquals(secondPacket, secondCall.packet);

        calls = tubeB.getCalls();
        
        assertNotNull(calls);
        assertEquals(2, calls.size());
        
        firstCall = calls.get(0);
        
        assertNotNull(firstCall);
        assertEquals(TubeCallType.REQUEST, firstCall.callType);
        assertEquals(testContainer, firstCall.container);
        
        secondCall = calls.get(1);
        
        assertNotNull(secondCall);
        assertEquals(TubeCallType.EXCEPTION, secondCall.callType);
        assertEquals(testContainer, secondCall.container);

        calls = tubeC.getCalls();
        
        assertNotNull(calls);
        assertEquals(0, calls.size());
    }
    
    public void testSuspendNextRunnableResumeAndReturn() throws InterruptedException {
        final Semaphore atSuspend = new Semaphore(0);
        final Semaphore checkCompleted = new Semaphore(0);
        final Semaphore atEnd = new Semaphore(0);
        
        final TestTube tubeC = new TestTube();
        final FilterTestTube tubeB = new FilterTestTube(tubeC) {
            @Override
            @NotNull
            public NextAction processRequest(@NotNull Packet request) {
                super.processRequest(request);
                return doSuspend(next, new Runnable() {
                    @Override
                    public void run() {
                        atSuspend.release();
                        try {
                            checkCompleted.acquire();
                        } catch (InterruptedException e) {
                        }
                    }
                });
            }
        };
        final FilterTestTube tubeA = new FilterTestTube(tubeB);
        
        final Packet request = new Packet();
        final SimpleCompletionCallback callback = new SimpleCompletionCallback() {
            @Override
            public void onCompletion(@NotNull Packet response) {
                super.onCompletion(response);
                atEnd.release();
            }

            @Override
            public void onCompletion(@NotNull Throwable error) {
                super.onCompletion(error);
                atEnd.release();
            }
        };
        
        final Fiber fiber = threadPoolEngine.createFiber();
        
        assertNotNull(fiber);
        
        fiber.start(tubeA, request, callback);
        
        if (!atSuspend.tryAcquire(3, TimeUnit.MINUTES))
            fail("timeout");
        
        assertEquals(0, atEnd.availablePermits()); // ensure test thread really blocked
        
        // thread is suspended
        
        assertNull(callback.response);
        assertNull(callback.error);
        
        List<TubeCall> calls = tubeA.getCalls();
        
        assertNotNull(calls);
        assertEquals(1, calls.size());
        
        TubeCall firstCall = calls.get(0);
        
        assertNotNull(firstCall);
        assertEquals(TubeCallType.REQUEST, firstCall.callType);
        assertEquals(testContainer, firstCall.container);
        
        calls = tubeB.getCalls();
        
        assertNotNull(calls);
        assertEquals(1, calls.size());
        
        firstCall = calls.get(0);
        
        assertNotNull(firstCall);
        assertEquals(TubeCallType.REQUEST, firstCall.callType);
        assertEquals(testContainer, firstCall.container);
        
        calls = tubeC.getCalls();
        
        assertNotNull(calls);
        assertEquals(0, calls.size());
        
        checkCompleted.release();
        fiber.resumeAndReturn(request, false);
        
        if (!atEnd.tryAcquire(3, TimeUnit.MINUTES))
            fail("timeout");

        assertEquals(request, callback.response);
        assertNull(callback.error);
        
        calls = tubeA.getCalls();
        
        assertNotNull(calls);
        assertEquals(2, calls.size());
        
        firstCall = calls.get(0);
        
        assertNotNull(firstCall);
        assertEquals(TubeCallType.REQUEST, firstCall.callType);
        assertEquals(testContainer, firstCall.container);
        
        TubeCall secondCall = calls.get(1);
        
        assertNotNull(secondCall);
        assertEquals(TubeCallType.RESPONSE, secondCall.callType);
        assertEquals(testContainer, secondCall.container);

        calls = tubeB.getCalls();
        
        assertNotNull(calls);
        assertEquals(2, calls.size());
        
        firstCall = calls.get(0);
        
        assertNotNull(firstCall);
        assertEquals(TubeCallType.REQUEST, firstCall.callType);
        assertEquals(testContainer, firstCall.container);
        
        secondCall = calls.get(1);
        
        assertNotNull(secondCall);
        assertEquals(TubeCallType.RESPONSE, secondCall.callType);
        assertEquals(testContainer, secondCall.container);

        calls = tubeC.getCalls();
        
        assertNotNull(calls);
        assertEquals(0, calls.size());
    }
    
    public void testCancel() throws InterruptedException {
        final Semaphore atResponse = new Semaphore(0);
        final Semaphore mayProceed = new Semaphore(0);
        
        TestTube tubeC = new TestTube();
        FilterTestTube tubeB = new FilterTestTube(tubeC) {

            @Override
            @NotNull
            public NextAction processResponse(@NotNull Packet response) {
                try {
                    atResponse.release();
                    mayProceed.acquire();
                } catch (InterruptedException e) {
                    throw new WebServiceException(e);
                }
                return super.processResponse(response);
            }
            
        };
        final FilterTestTube tubeA = new FilterTestTube(tubeB);
        
        final Packet request = new Packet();
        final SimpleCompletionCallback callback = new SimpleCompletionCallback();
        
        final Fiber fiber = engine.createFiber();
        
        assertNotNull(fiber);
        
        Thread testThread = new Thread(new Runnable() {
            @Override
            public void run() {
                fiber.start(tubeA, request, callback, true);
            }
        });
        
        testThread.start();
        
        if (!atResponse.tryAcquire(3, TimeUnit.MINUTES))
            fail("timeout");
        fiber.cancel(false);
        mayProceed.release();
        
        testThread.join(3 * 60 * 1000);
        
        assertNull(callback.response);
        assertNull(callback.error);
        
        List<TubeCall> calls = tubeA.getCalls();
        
        assertNotNull(calls);
        assertEquals(1, calls.size());
        
        TubeCall firstCall = calls.get(0);
        
        assertNotNull(firstCall);
        assertEquals(TubeCallType.REQUEST, firstCall.callType);
        assertEquals(testContainer, firstCall.container);
        
        calls = tubeB.getCalls();
        
        assertNotNull(calls);
        assertEquals(2, calls.size());
        
        firstCall = calls.get(0);
        
        assertNotNull(firstCall);
        assertEquals(TubeCallType.REQUEST, firstCall.callType);
        assertEquals(testContainer, firstCall.container);
        
        TubeCall secondCall = calls.get(1);
        
        assertNotNull(secondCall);
        assertEquals(TubeCallType.RESPONSE, secondCall.callType);
        assertEquals(testContainer, secondCall.container);

        calls = tubeC.getCalls();
        
        assertNotNull(calls);
        assertEquals(1, calls.size());
        
        firstCall = calls.get(0);
        
        assertNotNull(firstCall);
        assertEquals(TubeCallType.REQUEST, firstCall.callType);
        assertEquals(testContainer, firstCall.container);
    }
    
    private static class TestFiberContextSwitchInterceptor implements FiberContextSwitchInterceptor {
        private int callCount = 0;
        
        public int getCallCount() { return callCount; }
        
        @Override
        public <R, P> R execute(Fiber f, P p, Work<R, P> work) {
            callCount++;
            return work.execute(p);
        }
        
    }

    public void testAddInterceptorDuringSuspendResume() throws InterruptedException {
        final Semaphore atSuspend = new Semaphore(0);
        final Semaphore checkCompleted = new Semaphore(0);
        final Semaphore atEnd = new Semaphore(0);
        
        final TestTube tubeC = new TestTube();
        final FilterTestTube tubeB = new FilterTestTube(tubeC) {
            @Override
            @NotNull
            public NextAction processRequest(@NotNull Packet request) {
                super.processRequest(request);
                return doSuspend(new Runnable() {
                    @Override
                    public void run() {
                        atSuspend.release();
                        try {
                            checkCompleted.acquire();
                        } catch (InterruptedException e) {
                        }
                    }
                });
            }
        };
        final FilterTestTube tubeA = new FilterTestTube(tubeB);
        
        final Packet request = new Packet();
        final SimpleCompletionCallback callback = new SimpleCompletionCallback() {
            @Override
            public void onCompletion(@NotNull Packet response) {
                super.onCompletion(response);
                atEnd.release();
            }

            @Override
            public void onCompletion(@NotNull Throwable error) {
                super.onCompletion(error);
                atEnd.release();
            }
        };
        
        final Fiber fiber = threadPoolEngine.createFiber();
        
        assertNotNull(fiber);
        
        TestFiberContextSwitchInterceptor interceptor1 = new TestFiberContextSwitchInterceptor();
        fiber.addInterceptor(interceptor1);

        fiber.start(tubeA, request, callback);
        
        if (!atSuspend.tryAcquire(3, TimeUnit.MINUTES))
            fail("timeout");
        
        assertEquals(0, atEnd.availablePermits()); // ensure test thread really blocked
        
        // thread is suspended
        assertEquals(1, interceptor1.getCallCount());
        
        assertNull(callback.response);
        assertNull(callback.error);
        
        List<TubeCall> calls = tubeA.getCalls();
        
        assertNotNull(calls);
        assertEquals(1, calls.size());
        
        TubeCall firstCall = calls.get(0);
        
        assertNotNull(firstCall);
        assertEquals(TubeCallType.REQUEST, firstCall.callType);
        assertEquals(testContainer, firstCall.container);
        
        calls = tubeB.getCalls();
        
        assertNotNull(calls);
        assertEquals(1, calls.size());
        
        firstCall = calls.get(0);
        
        assertNotNull(firstCall);
        assertEquals(TubeCallType.REQUEST, firstCall.callType);
        assertEquals(testContainer, firstCall.container);
        
        calls = tubeC.getCalls();
        
        assertNotNull(calls);
        assertEquals(0, calls.size());
        
        checkCompleted.release();
        
        TestFiberContextSwitchInterceptor interceptor2 = new TestFiberContextSwitchInterceptor();
        fiber.addInterceptor(interceptor2);
        
        fiber.resume(request);
        
        if (!atEnd.tryAcquire(3, TimeUnit.MINUTES))
            fail("timeout");

        assertEquals(2, interceptor1.getCallCount());
        assertEquals(1, interceptor2.getCallCount());
        
        assertEquals(request, callback.response);
        assertNull(callback.error);
        
        calls = tubeA.getCalls();
        
        assertNotNull(calls);
        assertEquals(2, calls.size());
        
        firstCall = calls.get(0);
        
        assertNotNull(firstCall);
        assertEquals(TubeCallType.REQUEST, firstCall.callType);
        assertEquals(testContainer, firstCall.container);
        
        TubeCall secondCall = calls.get(1);
        
        assertNotNull(secondCall);
        assertEquals(TubeCallType.RESPONSE, secondCall.callType);
        assertEquals(testContainer, secondCall.container);

        calls = tubeB.getCalls();
        
        assertNotNull(calls);
        assertEquals(2, calls.size());
        
        firstCall = calls.get(0);
        
        assertNotNull(firstCall);
        assertEquals(TubeCallType.REQUEST, firstCall.callType);
        assertEquals(testContainer, firstCall.container);
        
        secondCall = calls.get(1);
        
        assertNotNull(secondCall);
        assertEquals(TubeCallType.RESPONSE, secondCall.callType);
        assertEquals(testContainer, secondCall.container);

        calls = tubeC.getCalls();
        
        assertNotNull(calls);
        assertEquals(0, calls.size());
    }

}
