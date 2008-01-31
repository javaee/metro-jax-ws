/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.tools.ws.impl;

import com.sun.tools.ws.processor.generator.Names;
import com.sun.tools.ws.wsdl.document.jaxws.JAXWSBindingsConstants;
import org.jvnet.wom.api.WSDLExtension;
import org.jvnet.wom.api.parser.WSDLExtensionHandler;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.XMLFilterImpl;

import java.util.Collection;
import java.util.Stack;

/**
 * @author Vivek Pandey
 */
public class JAXWSBindingParser implements WSDLExtensionHandler {
    private Locator locator;
    private BindingInfo root;
    private BindingInfo activeBindingInfo;

    private final Stack<BindingInfo> stack = new Stack<BindingInfo>();

    public Collection<WSDLExtension> getExtensions() {
        return null;
    }

    public Collection<WSDLExtension> parseAttribute(Attributes atts) {
        return null;
    }

    private int chState = 0;

    public ContentHandler getContentHandlerFor(String nsUri, String localName) {
        if (nsUri.equals(JAXWSBindingsConstants.JAXWS_BINDINGS)) {
            return bindingsContentHandler;
        } else if (nsUri.equals(JAXWSBindingsConstants.CLASS)) {
            chState = 1;
            return classContentHandler;
        } else if (nsUri.equals(JAXWSBindingsConstants.METHOD)) {
            chState = 2;
            return methodContentHandler;
        } else if (nsUri.equals(JAXWSBindingsConstants.PACKAGE)) {
            chState = 3;
            return packageContentHandler;
        } else if (nsUri.equals(JAXWSBindingsConstants.JAVADOC)) {
            switch (chState) {
                case 1:
                    return classContentHandler;
                case 2:
                    return methodContentHandler;
                case 3:
                    return packageContentHandler;
            }
        }
        return null;
    }

    private final JAXWSBindingCH bindingsContentHandler = new JAXWSBindingCH();
    private final JAXWSClassCH classContentHandler = new JAXWSClassCH();
    private final JAXWSMethodCH methodContentHandler = new JAXWSMethodCH();
    private final JAXWSPackageCH packageContentHandler = new JAXWSPackageCH();

    private class JAXWSBindingCH extends XMLFilterImpl {
        int state = 0;

        @Override
        public void setDocumentLocator(Locator loc) {
            locator = loc;
            super.setDocumentLocator(loc);
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            if (!uri.equals(JAXWSBindingsConstants.JAXWS_BINDINGS.getNamespaceURI())) {
                //Throw error?
                return;
            }

            if (localName.equals(JAXWSBindingsConstants.JAXWS_BINDINGS.getLocalPart())) {
                if (root == null) {
                    root = new BindingInfo(locator);
                    stack.push(root);
                } else {
                    BindingInfo child = new BindingInfo(locator);
                    root.add(child);
                    stack.push(child);
                    root = child;
                }
            } else if (localName.equals(JAXWSBindingsConstants.PARAMETER.getLocalPart())) {
                String part = atts.getValue("part");
                String childElementName = atts.getValue("childElementName");
                String name = atts.getValue("name");
                validateName(name);
                root.add(new BParameter(part, name, childElementName, locator));
            } else if (localName.equals(JAXWSBindingsConstants.ENABLE_ASYNC_MAPPING.getLocalPart())) {
                state = 1;
            } else if (localName.equals(JAXWSBindingsConstants.ENABLE_WRAPPER_STYLE.getLocalPart())) {
                state = 2;
            } else if (localName.equals(JAXWSBindingsConstants.ENABLE_MIME_CONTENT.getLocalPart())) {
                state = 3;
            } else if (localName.equals(JAXWSBindingsConstants.PROVIDER.getLocalPart())) {
                root.add(new BProvider(true, locator));
            }
        }

        @Override
        public void characters(char ch[], int start, int length) throws SAXException {
            switch (state) {
                case 1: //enableAsync
                    String enableAsync = new String(ch, start, length);
                    root.setEnableAsync(Boolean.valueOf(enableAsync));
                    state = 0;
                    break;
                case 2: //enableWraper
                    String enableWrapper = new String(ch, start, length);
                    root.setEnableWrapperStyle(Boolean.valueOf(enableWrapper));
                    state = 0;
                    break;
                case 3: //enableMimeContent
                    String enableMimeContent = new String(ch, start, length);
                    BMimeContent mimeContent = new BMimeContent(Boolean.valueOf(enableMimeContent), locator);
                    root.add(mimeContent);
                    state = 0;
                    break;
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (!uri.equals(JAXWSBindingsConstants.JAXWS_BINDINGS.getNamespaceURI())) {
                return;
            }

            if (localName.equals(JAXWSBindingsConstants.JAXWS_BINDINGS.getLocalPart())) {
                root = stack.pop();
            }
        }
    }

    private class JAXWSClassCH extends XMLFilterImpl {
        int state = 0;
        BClass bclass;

        @Override
        public void setDocumentLocator(Locator loc) {
            locator = loc;
            super.setDocumentLocator(loc);
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            if (!uri.equals(JAXWSBindingsConstants.JAXWS_BINDINGS.getNamespaceURI())) {
                //Throw error?
                return;
            }

            if (localName.equals(JAXWSBindingsConstants.CLASS.getLocalPart())) {
                String name = atts.getValue("name");
                validateName(name);
                bclass = new BClass(name, locator);
            } else if (localName.equals(JAXWSBindingsConstants.JAVADOC.getLocalPart())) {
                state = 1;
            }
        }


        private StringBuffer javadoc = new StringBuffer();

        @Override
        public void characters(char ch[], int start, int length) throws SAXException {
            if (state == 1) {
                javadoc.append(ch, start, length);
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (localName.equals(JAXWSBindingsConstants.JAVADOC.getLocalPart())) {
                if (javadoc.length() > 0 && bclass != null) {
                    bclass.setJavaDoc(javadoc.toString());
                    javadoc.setLength(0);
                    state = 0;
                }
            } else if (localName.equals(JAXWSBindingsConstants.CLASS.getLocalPart())) {
                root.add(bclass);
            }
        }
    }

    private class JAXWSMethodCH extends XMLFilterImpl {
        int state = 0;
        BMethod bmethod;

        @Override
        public void setDocumentLocator(Locator loc) {
            locator = loc;
            super.setDocumentLocator(loc);
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            if (!uri.equals(JAXWSBindingsConstants.JAXWS_BINDINGS.getNamespaceURI())) {
                //Throw error?
                return;
            }

            if (localName.equals(JAXWSBindingsConstants.METHOD.getLocalPart())) {
                String name = atts.getValue("name");
                validateName(name);
                bmethod = new BMethod(name, locator);
            } else if (localName.equals(JAXWSBindingsConstants.JAVADOC.getLocalPart())) {
                state = 1;
            }
        }

        private StringBuffer javadoc = new StringBuffer();

        @Override
        public void characters(char ch[], int start, int length) throws SAXException {
            if (state == 1) {
                javadoc.append(ch, start, length);
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (localName.equals(JAXWSBindingsConstants.JAVADOC.getLocalPart())) {
                if (javadoc.length() > 0 && bmethod != null) {
                    bmethod.setJavaDoc(javadoc.toString());
                    javadoc.setLength(0);
                    state = 0;
                }
            } else if (localName.equals(JAXWSBindingsConstants.METHOD.getLocalPart())) {
                root.add(bmethod);
            }
        }
    }

    private class JAXWSPackageCH extends XMLFilterImpl {
        int state = 0;
        BPackage bpackage;

        @Override
        public void setDocumentLocator(Locator loc) {
            locator = loc;
            super.setDocumentLocator(loc);
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
            if (!uri.equals(JAXWSBindingsConstants.JAXWS_BINDINGS.getNamespaceURI())) {
                //Throw error?
                return;
            }

            if (localName.equals(JAXWSBindingsConstants.PACKAGE.getLocalPart())) {
                String name = atts.getValue("name");
                validateName(name);
                bpackage = new BPackage(name, locator);
            } else if (localName.equals(JAXWSBindingsConstants.JAVADOC.getLocalPart())) {
                state = 1;
            }
        }

        private StringBuffer javadoc = new StringBuffer();

        @Override
        public void characters(char ch[], int start, int length) throws SAXException {
            if (state == 1) {
                javadoc.append(ch, start, length);
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (localName.equals(JAXWSBindingsConstants.JAVADOC.getLocalPart())) {
                if (javadoc.length() > 0 && bpackage != null) {
                    bpackage.setJavaDoc(javadoc.toString());
                    javadoc.setLength(0);
                    state = 0;
                }
            } else if (localName.equals(JAXWSBindingsConstants.PACKAGE.getLocalPart())) {
                root.add(bpackage);
            }
        }
    }


    private void validateName(String name) throws SAXParseException {
        if ((name == null) ||
                Names.isJavaReservedWord(name)) {
            throw new SAXParseException("Invalid name customization: " + name + ".", locator);
        }
    }

}
