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

package com.sun.xml.ws.client;

import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.pipe.NextAction;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.api.pipe.helper.AbstractTubeImpl;
import com.sun.xml.ws.api.server.SDDocument;
import com.sun.xml.ws.util.MetadataUtil;
import com.sun.xml.ws.util.pipe.AbstractSchemaValidationTube;
import org.xml.sax.SAXException;

import javax.xml.transform.Source;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;
import javax.xml.ws.WebServiceException;
import java.util.Map;
import java.util.logging.Logger;

/**
 * {@link Tube} that does the schema validation on the client side.
 *
 * @author Jitendra Kotamraju
 */
public class ClientSchemaValidationTube extends AbstractSchemaValidationTube {

    private static final Logger LOGGER = Logger.getLogger(ClientSchemaValidationTube.class.getName());

    private final Schema schema;
    private final Validator validator;
    private final boolean noValidation;
    private final WSDLPort port;

    public ClientSchemaValidationTube(WSBinding binding, WSDLPort port, Tube next) {
        super(binding, next);
        this.port = port;
        if (port != null) {
            String primaryWsdl = port.getOwner().getParent().getLocation().getSystemId();
            MetadataResolverImpl mdresolver = new MetadataResolverImpl();
            Map<String, SDDocument> docs = MetadataUtil.getMetadataClosure(primaryWsdl, mdresolver, true);
            mdresolver = new MetadataResolverImpl(docs.values());
            Source[] sources = getSchemaSources(docs.values(), mdresolver);
            for(Source source : sources) {
                LOGGER.fine("Constructing client validation schema from = "+source.getSystemId());
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

    protected ClientSchemaValidationTube(ClientSchemaValidationTube that, TubeCloner cloner) {
        super(that,cloner);
        this.port = that.port;
        this.schema = that.schema;
        this.validator = schema.newValidator();
        this.noValidation = that.noValidation;
    }

    public AbstractTubeImpl copy(TubeCloner cloner) {
        return new ClientSchemaValidationTube(this,cloner);
    }

    @Override
    public NextAction processRequest(Packet request) {
        if (isNoValidation() || !request.getMessage().hasPayload() || request.getMessage().isFault()) {
            return super.processRequest(request);
        }
        try {
            doProcess(request);
        } catch(SAXException se) {
            throw new WebServiceException(se);
        }
        return super.processRequest(request);
    }

    @Override
    public NextAction processResponse(Packet response) {
        if (isNoValidation() || response.getMessage() == null || !response.getMessage().hasPayload() || response.getMessage().isFault()) {
            return super.processResponse(response);
        }
        try {
            doProcess(response);
        } catch(SAXException se) {
            throw new WebServiceException(se);
        }
        return super.processResponse(response);
    }

}