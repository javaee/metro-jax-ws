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
package com.sun.xml.ws.server.provider;

import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Messages;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.encoding.xml.XMLMessage;
import com.sun.xml.ws.resources.ServerMessages;

import javax.activation.DataSource;
import javax.xml.transform.Source;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.http.HTTPException;

/**
 * @author Jitendra Kotamraju
 */
abstract class XMLProviderArgumentBuilder<T> extends ProviderArgumentsBuilder<T> {

    @Override
    protected Packet getResponse(Packet request, Exception e, WSDLPort port, WSBinding binding) {
        Packet response = super.getResponse(request, e, port, binding);
        if (e instanceof HTTPException) {
            if (response.supports(MessageContext.HTTP_RESPONSE_CODE)) {
                response.put(MessageContext.HTTP_RESPONSE_CODE, ((HTTPException)e).getStatusCode());
            }
        }
        return response;
    }

    static XMLProviderArgumentBuilder createBuilder(ProviderEndpointModel model, WSBinding binding) {
        if (model.mode == Service.Mode.PAYLOAD) {
            return new PayloadSource();
        } else {
            if(model.datatype==Source.class)
                return new PayloadSource();
            if(model.datatype== DataSource.class)
                return new DataSourceParameter(binding);
            throw new WebServiceException(ServerMessages.PROVIDER_INVALID_PARAMETER_TYPE(model.implClass,model.datatype));
        }
    }

    private static final class PayloadSource extends XMLProviderArgumentBuilder<Source> {
        public Source getParameter(Packet packet) {
            return packet.getMessage().readPayloadAsSource();
        }

        public Message getResponseMessage(Source source) {
            return Messages.createUsingPayload(source, SOAPVersion.SOAP_11);
        }

        protected Message getResponseMessage(Exception e) {
            return XMLMessage.create(e);
        }
    }

    private static final class DataSourceParameter extends XMLProviderArgumentBuilder<DataSource> {
        private final WSBinding binding;

        DataSourceParameter(WSBinding binding) {
            this.binding = binding;
        }
        public DataSource getParameter(Packet packet) {
            Message msg = packet.getMessage();
            return (msg instanceof XMLMessage.MessageDataSource)
                    ? ((XMLMessage.MessageDataSource) msg).getDataSource()
                    : XMLMessage.getDataSource(msg, binding);
        }

        public Message getResponseMessage(DataSource ds) {
            return XMLMessage.create(ds, binding);
        }

        protected Message getResponseMessage(Exception e) {
            return XMLMessage.create(e);
        }
    }

}
