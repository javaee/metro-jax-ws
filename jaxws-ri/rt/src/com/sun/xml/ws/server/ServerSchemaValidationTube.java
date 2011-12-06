/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
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

package com.sun.xml.ws.server;

import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.pipe.NextAction;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.api.pipe.helper.AbstractTubeImpl;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.fault.SOAPFaultBuilder;
import com.sun.xml.ws.util.pipe.AbstractSchemaValidationTube;
import org.xml.sax.SAXException;

import javax.xml.transform.Source;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;
import javax.xml.ws.WebServiceException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link Tube} that does the schema validation on the server side.
 *
 * @author Jitendra Kotamraju
 */
public class ServerSchemaValidationTube extends AbstractSchemaValidationTube {

    private static final Logger LOGGER = Logger.getLogger(ServerSchemaValidationTube.class.getName());

    private final Schema schema;
    private final Validator validator;

    private final boolean noValidation;
    private final SEIModel seiModel;
    private final WSDLPort wsdlPort;

    public ServerSchemaValidationTube(WSEndpoint endpoint, WSBinding binding,
            SEIModel seiModel, WSDLPort wsdlPort, Tube next) {
        super(binding, next);
        this.seiModel = seiModel;
        this.wsdlPort = wsdlPort;

        if (endpoint.getServiceDefinition() != null) {
            MetadataResolverImpl mdresolver = new MetadataResolverImpl(endpoint.getServiceDefinition());
            Source[] sources = getSchemaSources(endpoint.getServiceDefinition(), mdresolver);
            for(Source source : sources) {
                LOGGER.fine("Constructing service validation schema from = "+source.getSystemId());
                //printDOM((DOMSource)source);
            }
            if (sources.length != 0) {
                noValidation = false;
                sf.setResourceResolver(mdresolver);
                try {
                    schema = sf.newSchema(sources);
                } catch(SAXException e) {
                    throw new WebServiceException(e);
                }
                validator = schema.newValidator();
                return;
            }
        }
        noValidation = true;
        schema = null;
        validator = null;
    }

    protected Validator getValidator() {
        return validator;
    }

    protected boolean isNoValidation() {
        return noValidation;
    }

    @Override
    public NextAction processRequest(Packet request) {
        if (isNoValidation() || !feature.isInbound() || !request.getMessage().hasPayload() || request.getMessage().isFault()) {
            return super.processRequest(request);
        }
        try {
            doProcess(request);
        } catch(SAXException se) {
            LOGGER.log(Level.WARNING, "Client Request doesn't pass Service's Schema Validation", se);
            // Client request is invalid. So sending specific fault code
            // Also converting this to fault message so that handlers may get
            // to see the message.
            SOAPVersion soapVersion = binding.getSOAPVersion();
            Message faultMsg = SOAPFaultBuilder.createSOAPFaultMessage(
                    soapVersion, null, se, soapVersion.faultCodeClient);
            return doReturnWith(request.createServerResponse(faultMsg,
                    wsdlPort, seiModel, binding));
        }
        return super.processRequest(request);
    }

    @Override
    public NextAction processResponse(Packet response) {
        if (isNoValidation() || !feature.isOutbound() || response.getMessage() == null || !response.getMessage().hasPayload() || response.getMessage().isFault()) {
            return super.processResponse(response);
        }
        try {
            doProcess(response);
        } catch(SAXException se) {
            // TODO: Should we convert this to fault Message ??
            throw new WebServiceException(se);
        }
        return super.processResponse(response);
    }

    protected ServerSchemaValidationTube(ServerSchemaValidationTube that, TubeCloner cloner) {
        super(that,cloner);
        //this.docs = that.docs;
        this.schema = that.schema;      // Schema is thread-safe
        this.validator = schema.newValidator();
        this.noValidation = that.noValidation;
        this.seiModel = that.seiModel;
        this.wsdlPort = that.wsdlPort;
    }

    public AbstractTubeImpl copy(TubeCloner cloner) {
        return new ServerSchemaValidationTube(this,cloner);
    }

}
