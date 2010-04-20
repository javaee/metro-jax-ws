/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
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

package com.sun.tools.ws.wsdl.parser;

import com.sun.tools.ws.processor.modeler.wsdl.ConsoleErrorReporter;
import com.sun.tools.ws.wscompile.AbortException;
import com.sun.tools.ws.wscompile.ErrorReceiverFilter;
import com.sun.tools.ws.wscompile.WsimportListener;
import com.sun.tools.ws.wscompile.WsimportOptions;
import com.sun.tools.ws.wsdl.document.WSDLDocument;

import java.net.URL;

import junit.framework.TestCase;

import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

/**
 *
 * @author Fabian Ritzmann
 */
public class WSDLParserTest extends TestCase {
    
    public WSDLParserTest(String testName) {
        super(testName);
    }

    public void testParseNull() throws Exception {
        final WSDLParser instance = new WSDLParser(null, null);
        try {
            instance.parse();
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    public void testParseEmpty() throws Exception {
        final ErrorReceiverFilter errorReceiver = new ErrorReceiverFilter();
        final InputSource source = getResourceSource("com/sun/tools/ws/wsdl/parser/empty.wsdl");
        final WsimportOptions options = new WsimportOptions();
        options.addWSDL(source);
        final WSDLParser instance = new WSDLParser(options, errorReceiver);
        final WSDLDocument wsdl = instance.parse();
        assertNull(wsdl);
        assertTrue(errorReceiver.hadError());
    }

    public void testParseSimpleSystemIdNull() throws Exception {
        final ErrorReceiverFilter errorReceiver = new ErrorReceiverFilter();
        final InputSource source = getResourceSource("com/sun/tools/ws/wsdl/parser/simple.wsdl");
        source.setSystemId(null);
        final WsimportOptions options = new WsimportOptions();
        options.addWSDL(source);
        final WSDLParser instance = new WSDLParser(options, errorReceiver);
        try {
            final WSDLDocument wsdl = instance.parse();
            fail("Expected IllegalArgumentException, instead got " + wsdl);
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    public void testParseSimple() throws Exception {
        final ErrorReceiverFilter errorReceiver = createErrorReceiver();
        final InputSource source = getResourceSource("com/sun/tools/ws/wsdl/parser/simple.wsdl");
        final WsimportOptions options = new WsimportOptions();
        options.addWSDL(source);
        final WSDLParser instance = new WSDLParser(options, errorReceiver);
        final WSDLDocument wsdl = instance.parse();
        assertNotNull(wsdl);
        assertFalse(errorReceiver.hadError());
    }

    public void testParsePolicy12() throws Exception {
        final ErrorReceiverFilter errorReceiver = createErrorReceiver();
        final InputSource source = getResourceSource("com/sun/tools/ws/wsdl/parser/policy12.wsdl");
        final WsimportOptions options = new WsimportOptions();
        options.addWSDL(source);
        final WSDLParser instance = new WSDLParser(options, errorReceiver);
        final WSDLDocument wsdl = instance.parse();
        assertNotNull(wsdl);
        assertFalse(errorReceiver.hadError());
    }

    public void testParsePolicy15() throws Exception {
        final ErrorReceiverFilter errorReceiver = createErrorReceiver();
        final InputSource source = getResourceSource("com/sun/tools/ws/wsdl/parser/policy15.wsdl");
        final WsimportOptions options = new WsimportOptions();
        options.addWSDL(source);
        final WSDLParser instance = new WSDLParser(options, errorReceiver);
        final WSDLDocument wsdl = instance.parse();
        assertNotNull(wsdl);
        assertFalse(errorReceiver.hadError());
    }

    public void testParseUsingPolicy() throws Exception {
        final ErrorReceiverFilter errorReceiver = createErrorReceiver();
        final InputSource source = getResourceSource("com/sun/tools/ws/wsdl/parser/usingpolicy.wsdl");
        final WsimportOptions options = new WsimportOptions();
        options.addWSDL(source);
        final WSDLParser instance = new WSDLParser(options, errorReceiver);
        final WSDLDocument wsdl = instance.parse();
        assertNotNull(wsdl);
        assertFalse(errorReceiver.hadError());
    }

    public void testParseUsingPolicyRequired() throws Exception {
        final ErrorReceiverFilter errorReceiver = createErrorReceiver();
        final InputSource source = getResourceSource("com/sun/tools/ws/wsdl/parser/usingpolicy-required.wsdl");
        final WsimportOptions options = new WsimportOptions();
        options.addWSDL(source);
        final WSDLParser instance = new WSDLParser(options, errorReceiver);
        final WSDLDocument wsdl = instance.parse();
        assertNotNull(wsdl);
        assertFalse(errorReceiver.hadError());
    }

    private static URL getResourceAsUrl(String resourceName) throws RuntimeException {
        URL input = Thread.currentThread().getContextClassLoader().getResource(resourceName);
        if (input == null) {
            throw new RuntimeException("Failed to find resource \"" + resourceName + "\"");
        }
        return input;
    }

    private static InputSource getResourceSource(String resourceName) throws RuntimeException {
        // We go through the URL because that sets the system id of the InputSource.
        // The WSDLParser throws an exception if the system id is not set.
        return new InputSource(getResourceAsUrl(resourceName).toExternalForm());
    }

    private static ErrorReceiverFilter createErrorReceiver() {
        class Listener extends WsimportListener {

            ConsoleErrorReporter cer = new ConsoleErrorReporter(System.err);

            @Override
            public void generatedFile(String fileName) {
                message(fileName);
            }

            @Override
            public void message(String msg) {
                System.out.println(msg);
            }

            @Override
            public void error(SAXParseException exception) {
                cer.error(exception);
            }

            @Override
            public void fatalError(SAXParseException exception) {
                cer.fatalError(exception);
            }

            @Override
            public void warning(SAXParseException exception) {
                cer.warning(exception);
            }

            @Override
            public void debug(SAXParseException exception) {
                cer.debug(exception);
            }

            @Override
            public void info(SAXParseException exception) {
                cer.info(exception);
            }

            public void enableDebugging() {
                cer.enableDebugging();
            }
        }
        final Listener listener = new Listener();
        ErrorReceiverFilter errorReceiver = new ErrorReceiverFilter(listener) {

            public void info(SAXParseException exception) {
                super.info(exception);
            }

            public void warning(SAXParseException exception) {
                super.warning(exception);
            }

            @Override
            public void pollAbort() throws AbortException {
                if (listener.isCanceled()) {
                    throw new AbortException();
                }
            }

            @Override
            public void debug(SAXParseException exception) {
                listener.debug(exception);
            }
        };
        return errorReceiver;
    }

}