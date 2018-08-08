/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

/**
 * Defines the Java API for XML-Based Web Services (JAX-WS), and
 * the Web Services Metadata API.
 *
 *
 * @uses javax.xml.soap.MessageFactory
 * @uses javax.xml.soap.SAAJMetaFactory
 * @uses javax.xml.soap.SOAPConnectionFactory
 * @uses javax.xml.soap.SOAPFactory
 * @uses javax.xml.ws.spi.Provider
 *
 * @since 2.4.0
 */
module com.sun.xml.ws.jaxws {
    requires transitive java.xml.ws;
    requires transitive java.activation;
    requires transitive java.xml;
    requires transitive java.xml.bind;
    requires transitive java.annotation;

    requires com.sun.xml.bind;
    requires com.sun.xml.messaging.saaj;
    requires com.sun.xml.ws.policy;
    requires com.sun.istack.runtime;
    requires com.sun.xml.txw2;
    requires org.jvnet.staxex;
    requires org.jvnet.mimepull;
    requires com.sun.xml.streambuffer;
    requires com.sun.xml.fastinfoset;
    requires javax.jws;
    requires java.desktop;
    requires java.logging;
    requires java.management;
    requires jdk.httpserver;

    exports com.oracle.webservices.api;
    exports com.oracle.webservices.api.databinding;
    exports com.oracle.webservices.api.message;
    exports com.sun.xml.ws.api;
    exports com.sun.xml.ws.api.addressing;
    exports com.sun.xml.ws.api.databinding;
    exports com.sun.xml.ws.api.ha;
    exports com.sun.xml.ws.api.message;
    exports com.sun.xml.ws.api.model;
    exports com.sun.xml.ws.api.model.wsdl;
    exports com.sun.xml.ws.api.server;
    exports com.sun.xml.ws.api.streaming;
    exports com.sun.xml.ws.api.wsdl.parser;
    exports com.sun.xml.ws.api.wsdl.writer;
    exports com.sun.xml.ws.api.pipe;
    exports com.sun.xml.ws.developer;
    exports com.sun.xml.ws.resources;
    exports com.sun.xml.ws.message.stream;
    exports com.sun.xml.ws.addressing;
    exports com.sun.xml.ws.addressing.v200408;
    exports com.sun.xml.ws.binding;
    exports com.sun.xml.ws.db;
    exports com.sun.xml.ws.model;
    exports com.sun.xml.ws.server;
    exports com.sun.xml.ws.spi.db;
    exports com.sun.xml.ws.streaming;
    exports com.sun.xml.ws.transport;
    exports com.sun.xml.ws.transport.http;
    exports com.sun.xml.ws.util;
    exports com.sun.xml.ws.util.exception;
    exports com.sun.xml.ws.util.xml;
    exports com.sun.xml.ws.wsdl.parser;
    exports com.sun.xml.ws.wsdl.writer;
    exports com.sun.xml.ws.encoding;

    exports com.sun.xml.ws.transport.httpspi.servlet;

    exports com.sun.xml.ws.developer.servlet;
    exports com.sun.xml.ws.server.servlet;
    exports com.sun.xml.ws.transport.http.servlet;

    // XML document content needs to be exported
    opens com.sun.xml.ws.runtime.config to java.xml.bind;

    // com.sun.xml.ws.fault.SOAPFaultBuilder uses JAXBContext.newInstance
    opens com.sun.xml.ws.fault to java.xml.bind;

    // com.sun.xml.ws.addressing.WsaTubeHelperImpl uses JAXBContext.newInstance
    opens com.sun.xml.ws.addressing to java.xml.bind;

    // com.sun.xml.ws.addressing.v200408.WsaTubeHelperImpl uses JAXBContext.newInstance
    opens com.sun.xml.ws.addressing.v200408 to java.xml.bind;

    // com.sun.xml.ws.developer.MemberSubmissionEndpointReference uses JAXBContext.newInstance
    opens com.sun.xml.ws.developer to java.xml.bind;

    // com.sun.xml.ws.model.ExternalMetadataReader uses JAXBContext.newInstance
    opens com.oracle.xmlns.webservices.jaxws_databinding to java.xml.bind;


    uses javax.xml.ws.spi.Provider;
    uses javax.xml.soap.MessageFactory;
    uses javax.xml.soap.SAAJMetaFactory;
    uses javax.xml.soap.SOAPConnectionFactory;
    uses javax.xml.soap.SOAPFactory;
}