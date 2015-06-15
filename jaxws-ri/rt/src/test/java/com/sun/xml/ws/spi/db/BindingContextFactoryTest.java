/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.xml.ws.spi.db;

import com.sun.istack.NotNull;
import junit.framework.TestCase;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * Test for BindingContextFactory.
 *
 * @author yaroska
 */
public class BindingContextFactoryTest extends TestCase {

    //<editor-fold desc="setUp">
    private Method getBindingContextFromSpi;

    public void setUp() throws Exception {
        Class<BindingContextFactory> bcf = BindingContextFactory.class;
        getBindingContextFromSpi = bcf.getDeclaredMethod("getBindingContextFromSpi", List.class, BindingInfo.class);
        getBindingContextFromSpi.setAccessible(true);
    }
    //</editor-fold>

//---------- Testing getBindingContextFromSpi --------------

    public void test_receivedOnlyBadImpls() throws Exception {
        BindingContextFactory[] bcf = {new Bad(), new Bad()};
        BindingContext bc = (BindingContext) getBindingContextFromSpi.invoke(null, Arrays.asList(bcf), null);
        // two exceptions should be caught
        assertNull("Null should be returned.", bc);
    }

    public void test_receivedJaxbImpl() throws Exception {
        BindingContextFactory[] bcf = {new Bad(), new Good(), new Jaxb()};
        BindingContext bc = (BindingContext) getBindingContextFromSpi.invoke(null, Arrays.asList(bcf), null);
        assertNotNull("Not null should be returned.", bc);
        assertEquals("BindingContext from JaxbBcf is expected", Jaxb.BC, bc);
    }

    public void test_receivedMoxyImpl() throws Exception {
        BindingContextFactory[] bcf = {new Bad(), new Good(), new Moxy()};
        BindingContext bc = (BindingContext) getBindingContextFromSpi.invoke(null, Arrays.asList(bcf), null);
        assertNotNull("Not null should be returned.", bc);
        assertEquals("BindingContext from MoxyBcf is expected", Moxy.BC, bc);
    }

    public void test_receivedUnexpectedGood() throws Exception {
        BindingContextFactory[] bcf = {new Bad(), new Good()};
        BindingContext bc = (BindingContext) getBindingContextFromSpi.invoke(null, Arrays.asList(bcf), null);
        // one exception should be caught
        assertNotNull("Not null should be returned.", bc);
        assertEquals("BindingContext from GoodBcf is expected", Good.BC, bc);
    }

    public void test_exceptionToBeThrown() {
        boolean expectedExceptionWasThrown = false;
        BindingContextFactory[] bcf = {new Bad(), new JaxbWithException()};
        try {
            getBindingContextFromSpi.invoke(null, Arrays.asList(bcf), null);
        } catch (InvocationTargetException e) {
            Throwable realCause = e.getCause();
            if (realCause instanceof IllegalStateException) {
                expectedExceptionWasThrown = true;
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        assertTrue("Illegal state exception should be thrown", expectedExceptionWasThrown);
    }
//---------- end --------------

    //<editor-fold desc="Tools">

    /**
     * Dummy implementation for test.
     * Used to mark BindingContextFactory.
     */
    private static class Marker implements BindingContext {

        @Override
        public Marshaller createMarshaller() throws JAXBException {
            return null;
        }

        @Override
        public Unmarshaller createUnmarshaller() throws JAXBException {
            return null;
        }

        @Override
        public JAXBContext getJAXBContext() {
            return null;
        }

        @Override
        public Object newWrapperInstace(Class<?> wrapperType) throws InstantiationException, IllegalAccessException {
            return null;
        }

        @Override
        public boolean hasSwaRef() {
            return false;
        }

        @Override
        public QName getElementName(@NotNull Object o) throws JAXBException {
            return null;
        }

        @Override
        public QName getElementName(@NotNull Class o) throws JAXBException {
            return null;
        }

        @Override
        public XMLBridge createBridge(@NotNull TypeInfo ref) {
            return null;
        }

        @Override
        public XMLBridge createFragmentBridge() {
            return null;
        }

        @Override
        public <B, V> PropertyAccessor<B, V> getElementPropertyAccessor(Class<B> wrapperBean, String nsUri, String localName) throws JAXBException {
            return null;
        }

        @Override
        public List<String> getKnownNamespaceURIs() {
            return null;
        }

        @Override
        public void generateSchema(@NotNull SchemaOutputResolver outputResolver) throws IOException {

        }

        @Override
        public QName getTypeName(@NotNull TypeInfo tr) {
            return null;
        }

        @Override
        public String getBuildId() {
            return null;
        }
    }

    private static class Bad extends BindingContextFactory {

        @Override
        protected BindingContext newContext(JAXBContext context) {
            throw new IllegalStateException();
        }

        @Override
        protected BindingContext newContext(BindingInfo bi) {
            throw new IllegalStateException();
        }

        @Override
        protected boolean isFor(String databinding) {
            return false;
        }

        @Override
        protected BindingContext getContext(Marshaller m) {
            throw new IllegalStateException();
        }
    }

    private static class JaxbWithException extends BindingContextFactory {
        private static final Marker BC = new Marker();

        @Override
        protected BindingContext newContext(JAXBContext context) {
            throw new IllegalStateException("Bad context");
        }

        @Override
        protected BindingContext newContext(BindingInfo bi) {
            throw new IllegalStateException("Bad context");
        }

        @Override
        protected BindingContext getContext(Marshaller m) {
            return null;
        }

        @Override
        protected boolean isFor(String s) {
            return "com.sun.xml.bind.v2.runtime".equals(s);
        }
    }

    private static class Good extends BindingContextFactory {

        private static final Marker BC = new Marker();

        protected BindingContext get() {
            return BC;
        }

        @Override
        protected BindingContext newContext(JAXBContext context) {
            return get();
        }

        @Override
        protected BindingContext newContext(BindingInfo bi) {
            return get();
        }

        @Override
        protected boolean isFor(String s) {
            return false;
        }

        @Override
        protected BindingContext getContext(Marshaller m) {
            return null;
        }
    }

    private static class Jaxb extends Good {

        private static final Marker BC = new Marker();

        @Override
        protected BindingContext get() {
            return BC;
        }

        @Override
        protected boolean isFor(String s) {
            return "com.sun.xml.bind.v2.runtime".equals(s);
        }
    }

    private static class Moxy extends Jaxb {

        private static final Marker BC = new Marker();

        @Override
        protected BindingContext get() {
            return BC;
        }

        @Override
        protected boolean isFor(String s) {
            return "org.eclipse.persistence.jaxb".equals(s);
        }
    }
    //</editor-fold>
}
