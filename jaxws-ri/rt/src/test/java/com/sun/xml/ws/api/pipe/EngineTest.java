/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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
        Packet packet;
        Container container;
        
        public TubeCall(TubeCallType callType, Packet packet, Container container) {
            this.callType = callType;
            this.packet = packet;
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
            calls.add(new TubeCall(TubeCallType.REQUEST, request, c));
            
            return doReturnWith(request);
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
        public TestTube copy(TubeCloner cloner) {
            return new TestTube(this, cloner);
        }
        
    }
}
