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
        try {
            final WSDLParser instance = new WSDLParser(null, null);
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

    // testJaxws994
    private class MyLocator implements org.xml.sax.Locator {
      boolean hasEntry;
      MyLocator(boolean hasEntry) {
        this.hasEntry = hasEntry;
      }
      public java.lang.String getPublicId() { return "id";}
      public java.lang.String getSystemId() { 
        if (hasEntry) {
          return "jar:file:my.jar!/wsdls/simple.wsdl";
        } else {
         return "jar:file:my.jar";
        }
      }
      public int getLineNumber() { return 0;}
      public int getColumnNumber() { return 0;}
    }
    private class MyFinder extends com.sun.tools.ws.wsdl.parser.AbstractReferenceFinderImpl {
      MyFinder(boolean hasEntry) { 
        super(new MyDOMForest(null,null, null, null, hasEntry)); 
      }
      protected String findExternalResource( String nsURI, String localName, org.xml.sax.Attributes atts) {
        return nsURI;
      }
    }
    private class MyDOMForest extends com.sun.tools.ws.wsdl.parser.DOMForest {
      boolean hasEntry;
      MyDOMForest(InternalizationLogic logic, org.xml.sax.EntityResolver entityResolver, WsimportOptions options, com.sun.tools.ws.wscompile.ErrorReceiver errReceiver, boolean hasEntry) {
       super(logic, entityResolver, options, errReceiver);
       this.hasEntry = hasEntry;
     }

     public org.w3c.dom.Document parse(String systemId, boolean root) throws org.xml.sax.SAXException, java.io.IOException {
       if (hasEntry) {
         assertEquals("fail to resolve jar:file resource", "jar:file:my.jar!/wsdls/simple.wsdl", systemId);
       } else {
         assertEquals("fail to resolve jar:file resource", "externalResource", systemId);
       }
       return null;
     }
    }
    public void testJaxws994() throws Exception {
      boolean hasEntry = false;
      MyLocator locator = new MyLocator(hasEntry);
      MyFinder finder = new MyFinder(hasEntry);
      finder.setDocumentLocator(locator);
      finder.startElement(null, null, null, null);

      hasEntry = true;
      locator = new MyLocator(hasEntry);
      finder = new MyFinder(hasEntry);
      finder.setDocumentLocator(locator);
      finder.startElement(null, null, null, null);
    }

    public void testParseSimpleSystemIdNull() throws Exception {
        final ErrorReceiverFilter errorReceiver = new ErrorReceiverFilter();
        final InputSource source = getResourceSource("com/sun/tools/ws/wsdl/parser/simple.wsdl");
        source.setSystemId(null);
        final WsimportOptions options = new WsimportOptions();
        options.addWSDL(source);
        try {   
            final WSDLParser instance = new WSDLParser(options, errorReceiver);
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
