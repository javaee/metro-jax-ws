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

package com.sun.xml.ws.api.model.wsdl;


import com.sun.istack.NotNull;
import com.sun.xml.ws.api.server.Container;
import com.sun.xml.ws.api.wsdl.parser.WSDLParserExtension;
import com.sun.xml.ws.api.wsdl.parser.XMLEntityResolver;
import com.sun.xml.ws.api.policy.PolicyResolver;
import com.sun.xml.ws.api.policy.PolicyResolverFactory;
import com.sun.xml.ws.wsdl.parser.RuntimeWSDLParser;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.Map;

/**
 * Provides abstraction of wsdl:definitions.
 *
 * @author Vivek Pandey
 */
public interface WSDLModel extends WSDLExtensible {
    /**
     * Gets {@link WSDLPortType} that models <code>wsdl:portType</code>
     *
     * @param name non-null quaified name of wsdl:message, where the localName is the value of <code>wsdl:portType@name</code> and
     *             the namespaceURI is the value of wsdl:definitions@targetNamespace
     * @return A {@link com.sun.xml.ws.api.model.wsdl.WSDLPortType} or null if no wsdl:portType found.
     */
    WSDLPortType getPortType(@NotNull QName name);

    /**
     * Gets {@link WSDLBoundPortType} that models <code>wsdl:binding</code>
     *
     * @param name non-null quaified name of wsdl:binding, where the localName is the value of <code>wsdl:binding@name</code> and
     *             the namespaceURI is the value of wsdl:definitions@targetNamespace
     * @return A {@link WSDLBoundPortType} or null if no wsdl:binding found
     */
    WSDLBoundPortType getBinding(@NotNull QName name);

    /**
     * Give a {@link WSDLBoundPortType} for the given wsdl:service and wsdl:port names.
     *
     * @param serviceName service QName
     * @param portName    port QName
     * @return A {@link WSDLBoundPortType}. null if the Binding for the given wsd:service and wsdl:port name are not
     *         found.
     */
    WSDLBoundPortType getBinding(@NotNull QName serviceName, @NotNull QName portName);

    /**
     * Gets {@link WSDLService} that models <code>wsdl:service</code>
     *
     * @param name non-null quaified name of wsdl:service, where the localName is the value of <code>wsdl:service@name</code> and
     *             the namespaceURI is the value of wsdl:definitions@targetNamespace
     * @return A {@link WSDLService} or null if no wsdl:service found
     */
    WSDLService getService(@NotNull QName name);

    /**
     * Gives a {@link Map} of wsdl:portType {@link QName} and {@link WSDLPortType}
     *
     * @return an empty Map if the wsdl document has no wsdl:portType
     */
    @NotNull Map<QName, ? extends WSDLPortType> getPortTypes();

    /**
     * Gives a {@link Map} of wsdl:binding {@link QName} and {@link WSDLBoundPortType}
     *
     * @return an empty Map if the wsdl document has no wsdl:binding
     */
    @NotNull Map<QName, WSDLBoundPortType> getBindings();

    /**
     * Gives a {@link Map} of wsdl:service qualified name and {@link com.sun.xml.ws.api.model.wsdl.WSDLService}
     *
     * @return an empty Map if the wsdl document has no wsdl:service
     */
    @NotNull Map<QName, ? extends WSDLService> getServices();

    /**
     * Main purpose of this class is to  parsing of a WSDL and get the {@link WSDLModel} from it.
     */
    public class WSDLParser{
       /**
         * Parses WSDL from the given wsdlLoc and gives a {@link WSDLModel} built from it.
         *
         * @param wsdlEntityParser  Works like an entityResolver to resolve WSDLs
         * @param resolver  {@link XMLEntityResolver}, works at XML infoset level
         * @param isClientSide  true - its invoked on the client, false means its invoked on the server
         * @param extensions var args of {@link com.sun.xml.ws.api.wsdl.parser.WSDLParserExtension}s
         * @return A {@link WSDLModel} built from the given wsdlLocation}
         * @throws java.io.IOException
         * @throws javax.xml.stream.XMLStreamException
         * @throws org.xml.sax.SAXException
         */
        public static @NotNull WSDLModel parse(XMLEntityResolver.Parser wsdlEntityParser, XMLEntityResolver resolver, boolean isClientSide, WSDLParserExtension... extensions) throws IOException, XMLStreamException, SAXException {
            return parse(wsdlEntityParser, resolver, isClientSide, Container.NONE, extensions);
        }

        /**
         * Parses WSDL from the given wsdlLoc and gives a {@link WSDLModel} built from it.
         *
         * @param wsdlEntityParser  Works like an entityResolver to resolve WSDLs
         * @param resolver  {@link XMLEntityResolver}, works at XML infoset level
         * @param isClientSide  true - its invoked on the client, false means its invoked on the server
         * @param container - container in which the parser is run
         * @param extensions var args of {@link com.sun.xml.ws.api.wsdl.parser.WSDLParserExtension}s
         * @return A {@link WSDLModel} built from the given wsdlLocation}
         * @throws java.io.IOException
         * @throws javax.xml.stream.XMLStreamException
         * @throws org.xml.sax.SAXException
         */
        public static @NotNull WSDLModel parse(XMLEntityResolver.Parser wsdlEntityParser, XMLEntityResolver resolver, boolean isClientSide, @NotNull Container container, WSDLParserExtension... extensions) throws IOException, XMLStreamException, SAXException {
            return parse(wsdlEntityParser, resolver, isClientSide, container, PolicyResolverFactory.create(),extensions);
        }


        /**
         * Parses WSDL from the given wsdlLoc and gives a {@link WSDLModel} built from it.
         *
         * @param wsdlEntityParser  Works like an entityResolver to resolve WSDLs
         * @param resolver  {@link XMLEntityResolver}, works at XML infoset level
         * @param isClientSide  true - its invoked on the client, false means its invoked on the server
         * @param container - container in which the parser is run
         * @param policyResolver - PolicyResolver for resolving effective Policy
         * @param extensions var args of {@link com.sun.xml.ws.api.wsdl.parser.WSDLParserExtension}s
         * @return A {@link WSDLModel} built from the given wsdlLocation}
         * @throws java.io.IOException
         * @throws javax.xml.stream.XMLStreamException
         * @throws org.xml.sax.SAXException
         */
        public static @NotNull WSDLModel parse(XMLEntityResolver.Parser wsdlEntityParser, XMLEntityResolver resolver, boolean isClientSide, @NotNull Container container, PolicyResolver policyResolver,  WSDLParserExtension... extensions) throws IOException, XMLStreamException, SAXException {
            return RuntimeWSDLParser.parse(wsdlEntityParser, resolver, isClientSide, container, policyResolver, extensions);
        }

    }
}
