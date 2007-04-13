/*
 The contents of this file are subject to the terms
 of the Common Development and Distribution License
 (the "License").  You may not use this file except
 in compliance with the License.
 
 You can obtain a copy of the license at
 https://jwsdp.dev.java.net/CDDLv1.0.html
 See the License for the specific language governing
 permissions and limitations under the License.
 
 When distributing Covered Code, include this CDDL
 HEADER in each file and include the License file at
 https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 add the following below this CDDL HEADER, with the
 fields enclosed by brackets "[]" replaced with your
 own identifying information: Portions Copyright [yyyy]
 [name of copyright owner]
*/
/*
 $Id: W3CAddressingWSDLGeneratorExtension.java,v 1.3 2007-04-13 00:32:40 jitu Exp $

 Copyright (c) 2006 Sun Microsystems, Inc.
 All rights reserved.
*/

package com.sun.xml.ws.wsdl.writer;

import javax.xml.ws.Action;
import javax.xml.ws.FaultAction;
import javax.xml.ws.soap.AddressingFeature;

import com.sun.istack.NotNull;
import com.sun.xml.txw2.TypedXmlWriter;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.model.CheckedException;
import com.sun.xml.ws.api.model.JavaMethod;
import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.api.server.Container;
import com.sun.xml.ws.api.wsdl.writer.WSDLGeneratorExtension;
import com.sun.xml.ws.api.wsdl.writer.WSDLGenExtnContext;

/**
 * @author Arun Gupta
 */
public class W3CAddressingWSDLGeneratorExtension extends WSDLGeneratorExtension {
    private boolean enabled;
    private boolean required = false;

    @Override
    public void start(WSDLGenExtnContext ctxt) {
        WSBinding binding = ctxt.getBinding();
        TypedXmlWriter root = ctxt.getRoot();
        enabled = binding.isFeatureEnabled(AddressingFeature.class);
        if (!enabled)
            return;
        AddressingFeature ftr = binding.getFeature(AddressingFeature.class);
        required = ftr.isRequired();
        root._namespace(AddressingVersion.W3C.wsdlNsUri, AddressingVersion.W3C.getWsdlPrefix());
    }

    @Override
    public void addOperationInputExtension(TypedXmlWriter input, JavaMethod method) {
        if (!enabled)
            return;

        Action a = method.getSEIMethod().getAnnotation(Action.class);
        if (a != null && !a.input().equals("")) {
            addAttribute(input, a.input());
        }
    }

    @Override
    public void addOperationOutputExtension(TypedXmlWriter output, JavaMethod method) {
        if (!enabled)
            return;

        Action a = method.getSEIMethod().getAnnotation(Action.class);
        if (a != null && !a.output().equals("")) {
            addAttribute(output, a.output());
        }
    }

    @Override
    public void addOperationFaultExtension(TypedXmlWriter fault, JavaMethod method, CheckedException ce) {
        if (!enabled)
            return;

        Action a = method.getSEIMethod().getAnnotation(Action.class);
        Class[] exs = method.getSEIMethod().getExceptionTypes();

        if (exs == null)
            return;

        if (a != null && a.fault() != null) {
            for (FaultAction fa : a.fault()) {
                if (fa.className().getName().equals(ce.getExceptionClass().getName())) {
                    if (fa.value().equals(""))
                        return;

                    addAttribute(fault, fa.value());
                    return;
                }
            }
        }
    }

    private void addAttribute(TypedXmlWriter writer, String attrValue) {
        writer._attribute(AddressingVersion.W3C.wsdlActionTag, attrValue);
    }

    @Override
    public void addBindingExtension(TypedXmlWriter binding) {
        if (!enabled)
            return;
        UsingAddressing ua = binding._element(AddressingVersion.W3C.wsdlExtensionTag, UsingAddressing.class);
        /*
        Do not generate wsdl:required=true
        if(required) {
            ua.required(true);
        }
        */
    }
}
