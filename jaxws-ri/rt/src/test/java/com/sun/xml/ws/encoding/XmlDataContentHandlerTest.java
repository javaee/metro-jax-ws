/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.xml.ws.encoding;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import javax.activation.DataSource;
import junit.framework.TestCase;

/**
 * Test JAF data handler for XML content.
 */
public class XmlDataContentHandlerTest extends TestCase {

    /**
     * Data source mock-up for {@link XmlDataContentHandler} class test.
     */
    private static class DataSourceMockUp implements DataSource {

        /** Content type. */
        private final String contentType;

        /** Data source name. */
        private final String name;

        // ByteArrayInputStream does not need to be closed.
        /** Simple input stream. */
        private final ByteArrayInputStream is;

        /**
         * Creates an instance of dummy data source.
         * @param name Data source name.
         * @param contentType Content type.
         */
        private DataSourceMockUp(final String name, final String contentType) {
            this.name = name;
            this.contentType = contentType;
            final byte[] buffIn = ("Content type: " + contentType).getBytes(Charset.forName("UTF-8"));
            is = new ByteArrayInputStream(buffIn);
        }

        /**
         * Returns simple input stream.
         * @return simple input stream.
         */
        @Override
        public InputStream getInputStream() throws IOException {
            return is;
        }

        /**
         * Not supported.
         * @return nothing.
         * @throws IOException is never thrown.
         * @throws UnsupportedOperationException on every call.
         */
        @Override
        public OutputStream getOutputStream() throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        /**
         * Returns stored content type.
         * @return stored content type.
         */
        @Override
        public String getContentType() {
            return contentType;
        }

        /**
         * Returns stored name.
         * @return stored name.
         */
        @Override
        public String getName() {
            return name;
        }

    }

    /**
     * Creates an instance of this test.
     * @param testName Test name.
     */
    public XmlDataContentHandlerTest(String testName) {
        super(testName);
    }

    /**
     * Test MIME type processing in {@code isXml(ContentType)} method
     * of {@link XmlDataContentHandler} class with lower case mime type.
     * @throws ClassNotFoundException
     */
    public void testIsXmlLowerCase() throws Exception {
        checkIsXml("application/xml");
    }

    /**
     * Test MIME type processing in {@code isXml(ContentType)} method
     * of {@link XmlDataContentHandler} class with upper case mime type.
     * @throws ClassNotFoundException
     */
    public void testIsXmlUpperCase() throws ClassNotFoundException, IOException {
        checkIsXml("APPLICATION/XML");
    }

    /**
     * Test MIME type processing in {@code isXml(ContentType)} method
     * of {@link XmlDataContentHandler} class with mixed case mime type.
     * @throws ClassNotFoundException
     */
    public void testIsXmlMixedCase() throws ClassNotFoundException, IOException {
        checkIsXml("application/XML");
    }

    /**
     * Test MIME type processing in {@code isXml(ContentType)} method of {@link XmlDataContentHandler} class.
     * @param mimeType MIME type to be tested.
     */
    private void checkIsXml(final String mimeType) throws ClassNotFoundException {
        XmlDataContentHandler handler = new XmlDataContentHandler();
        DataSource ds = new DataSourceMockUp("dummy", mimeType);
        try {
            handler.getContent(ds);
        } catch (IOException ex) {
            fail(ex.getMessage());
        }
    }

}
