/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.client;

import java.net.URL;

import javax.xml.ws.Holder;
import javax.xml.ws.WebServiceException;

import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.ComponentFeature;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.ClientTubeAssemblerContext;
import com.sun.xml.ws.api.pipe.NextAction;
import com.sun.xml.ws.api.pipe.ServerTubeAssemblerContext;
import com.sun.xml.ws.api.pipe.TransportTubeFactory;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.api.pipe.TubelineAssembler;
import com.sun.xml.ws.api.pipe.TubelineAssemblerFactory;
import com.sun.xml.ws.api.pipe.helper.AbstractTubeImpl;
import com.sun.xml.ws.client.test.Echo;
import com.sun.xml.ws.client.test.EchoService;
import com.sun.xml.ws.client.test.NumbersRequest;

import junit.framework.TestCase;

public class ClientProxyTest extends TestCase {

    static class EchoTube extends AbstractTubeImpl {
        @Override
        public NextAction processRequest(Packet request) {
            NextAction na = new NextAction();
            na.returnWith(request);
            return na;
        }
        @Override
        public NextAction processResponse(Packet response) { return null; }
        @Override
        public NextAction processException(Throwable t) { return null; }
        @Override
        public void preDestroy() {}
        @Override
        public AbstractTubeImpl copy(TubeCloner cloner) { return null; }
    }

    @SuppressWarnings("unchecked")
    public void testNullResponseFromTransprt() throws Exception {
        URL wsdlURL = Thread.currentThread().getContextClassLoader().getResource("etc/EchoService.wsdl");
        EchoService srv = new EchoService(wsdlURL, new ComponentFeature( new com.sun.xml.ws.api.Component() {
            public <S> S getSPI(Class<S> spiType) {
                if (TransportTubeFactory.class.equals(spiType)) return (S) new TransportTubeFactory() {
                    public Tube doCreate( ClientTubeAssemblerContext context) {
                        return new EchoTube() {
                            public NextAction processRequest(Packet request) {
                                NextAction na = new NextAction();
                                na.returnWith(new Packet());
                                return na;
                            }
                        };
                    }
                };
                return null;
            }
        }));
        Echo echo = srv.getEchoPort();
        try {
            int res = echo.add(new NumbersRequest());
            fail();
        } catch (Exception e) {
            assertFalse(e instanceof NullPointerException);
            assertTrue(e instanceof WebServiceException);
        }
        try {
            echo.echoString(new Holder<String>(wsdlURL.toString()));
            fail();
        } catch (Exception e) {
            assertFalse(e instanceof NullPointerException);
            assertTrue(e instanceof WebServiceException);
        }
    }

    @SuppressWarnings("unchecked")
    public void testNullResponseFromTube() throws Exception {
        URL wsdlURL = Thread.currentThread().getContextClassLoader().getResource("etc/EchoService.wsdl");
        EchoService srv = new EchoService(wsdlURL, new ComponentFeature(new com.sun.xml.ws.api.Component() {
            public <S> S getSPI(Class<S> spiType) {
                if (TransportTubeFactory.class.equals(spiType)) return (S) new TransportTubeFactory() {
                    public Tube doCreate(ClientTubeAssemblerContext context) {
                        return new EchoTube();
                    }
                };
                if (TubelineAssemblerFactory.class.equals(spiType)) return (S) new TubelineAssemblerFactory() {
                    public TubelineAssembler doCreate(BindingID bindingId) {
                        return new TubelineAssembler() {
                            public Tube createClient(ClientTubeAssemblerContext context) {
                                final Tube head = context.createTransportTube();
                                return new EchoTube() {
                                    public NextAction processRequest(Packet request) {
                                        NextAction na = new NextAction();
                                        na.invoke(head, request);
                                        return na;
                                    }
                                    public NextAction processResponse(Packet response) {
                                        NextAction na = new NextAction();
                                        na.returnWith(new Packet());
                                        return na;
                                    }
                                };
                            }
                            public Tube createServer(ServerTubeAssemblerContext context) { return null; }
                        };
                    }
                };
                return null;
            }
        }));
        Echo echo = srv.getEchoPort();
        try {
            int res = echo.add(new NumbersRequest());
            fail();
        } catch (Exception e) {
            assertFalse(e instanceof NullPointerException);
            assertTrue(e instanceof WebServiceException);
        }
        try {
            echo.echoString(new Holder(wsdlURL.toString()));
            fail();
        } catch (Exception e) {
            assertFalse(e instanceof NullPointerException);
            assertTrue(e instanceof WebServiceException);
        }
    }
}
