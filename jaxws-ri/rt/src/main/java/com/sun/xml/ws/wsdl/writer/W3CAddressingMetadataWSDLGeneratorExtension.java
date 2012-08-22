/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2012 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.wsdl.writer;

import com.sun.xml.ws.api.wsdl.writer.WSDLGeneratorExtension;
import com.sun.xml.ws.api.wsdl.writer.WSDLGenExtnContext;
import com.sun.xml.ws.api.model.JavaMethod;
import com.sun.xml.ws.api.model.CheckedException;
import com.sun.xml.txw2.TypedXmlWriter;
import static com.sun.xml.ws.addressing.W3CAddressingMetadataConstants.*;
import com.sun.xml.ws.model.JavaMethodImpl;
import com.sun.xml.ws.model.CheckedExceptionImpl;
import com.sun.xml.ws.addressing.WsaActionUtil;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;

/**
 * This extension class generates wsam:Action values for input, output and faults in the generated wsdl.
 *
 * @author Rama Pulavarthi
 */
public class W3CAddressingMetadataWSDLGeneratorExtension extends
        WSDLGeneratorExtension {

    @Override
    public void start(WSDLGenExtnContext ctxt) {
        TypedXmlWriter root = ctxt.getRoot();
        root._namespace(WSAM_NAMESPACE_NAME, WSAM_PREFIX_NAME);
    }

    @Override
    public void addOperationInputExtension(TypedXmlWriter input,
                                           JavaMethod method) {
        input._attribute(WSAM_ACTION_QNAME, getInputAction(method));
    }

    @Override
    public void addOperationOutputExtension(TypedXmlWriter output,
                                            JavaMethod method) {
        output._attribute(WSAM_ACTION_QNAME, getOutputAction(method));
    }

    @Override
    public void addOperationFaultExtension(TypedXmlWriter fault,
                                           JavaMethod method, CheckedException ce) {
        fault._attribute(WSAM_ACTION_QNAME, getFaultAction(method, ce));
    }


    private static final String getInputAction(JavaMethod method) {
        String inputaction = ((JavaMethodImpl)method).getInputAction();
        if (inputaction.equals("")) {
            // Calculate default action
            inputaction = getDefaultInputAction(method);
        }
        return inputaction;
    }

    protected static final String getDefaultInputAction(JavaMethod method) {
        String tns = method.getOwner().getTargetNamespace();
        String delim = getDelimiter(tns);
        if (tns.endsWith(delim))
            tns = tns.substring(0, tns.length() - 1);
        //this assumes that fromjava case there won't be input name.
        // if there is input name in future, then here name=inputName
        //else use operation name as follows.
        String name = (method.getMEP().isOneWay()) ?
                method.getOperationName() : method.getOperationName() + "Request";

        return new StringBuilder(tns).append(delim).append(
                method.getOwner().getPortTypeName().getLocalPart()).append(
                delim).append(name).toString();
    }

    private static final String getOutputAction(JavaMethod method) {
        String outputaction = ((JavaMethodImpl)method).getOutputAction();
        if(outputaction.equals(""))
            outputaction = getDefaultOutputAction(method);
        return outputaction;
    }

    protected static final String getDefaultOutputAction(JavaMethod method) {
        String tns = method.getOwner().getTargetNamespace();
        String delim = getDelimiter(tns);
        if (tns.endsWith(delim))
            tns = tns.substring(0, tns.length() - 1);
        //this assumes that fromjava case there won't be output name.
        // if there is input name in future, then here name=outputName
        //else use operation name as follows.
        String name = method.getOperationName() + "Response";

        return new StringBuilder(tns).append(delim).append(
                method.getOwner().getPortTypeName().getLocalPart()).append(
                delim).append(name).toString();
    }


    private static final String getDelimiter(String tns) {
        String delim = "/";
        // TODO: is this the correct way to find the separator ?
        try {
            URI uri = new URI(tns);
            if ((uri.getScheme() != null) && uri.getScheme().equalsIgnoreCase("urn"))
                delim = ":";
        } catch (URISyntaxException e) {
            LOGGER.warning("TargetNamespace of WebService is not a valid URI");
        }
        return delim;

    }

    private static final String getFaultAction(JavaMethod method,
                                               CheckedException ce) {
        String faultaction = ((CheckedExceptionImpl)ce).getFaultAction();
        if (faultaction.equals("")) {
            faultaction = getDefaultFaultAction(method,ce);
        }
        return faultaction;
    }

    protected static final String getDefaultFaultAction(JavaMethod method, CheckedException ce) {
        return WsaActionUtil.getDefaultFaultAction(method,ce);
    }

    private static final Logger LOGGER =
            Logger.getLogger(W3CAddressingMetadataWSDLGeneratorExtension.class.getName());
}
