/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */
package com.sun.istack.ws;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.sun.mirror.apt.*;
import com.sun.mirror.declaration.*;


import com.sun.tools.ws.processor.modeler.annotation.AnnotationProcessorContext;
import com.sun.tools.ws.processor.modeler.annotation.WebServiceAP;

/*
 * The JAX-WS {@com.sun.mirror.apt.AnnotationProcessorFactory AnnotationProcessorFactory}
 * class used by the <a href="http://java.sun.com/j2se/1.5.0/docs/tooldocs/share/apt.html">APT</a>
 * framework.
 */
public class AnnotationProcessorFactoryImpl implements AnnotationProcessorFactory {

    private static WebServiceAP wsAP;
    /*
     * Processor doesn't examine any options.
     */
    static final Collection<String> supportedOptions = Collections.unmodifiableSet(new HashSet<String>());


    /*
     * Supports javax.jws.*, javax.jws.soap.* and javax.xml.ws.* annotations.
     */
    static Collection<String> supportedAnnotations;
    static {
        Collection<String> types = new HashSet<String>();
        types.add("javax.jws.HandlerChain");
        types.add("javax.jws.Oneway");
        types.add("javax.jws.WebMethod");
        types.add("javax.jws.WebParam");
        types.add("javax.jws.WebResult");
        types.add("javax.jws.WebService");
        types.add("javax.jws.soap.InitParam");
        types.add("javax.jws.soap.SOAPBinding");
        types.add("javax.jws.soap.SOAPMessageHandler");
        types.add("javax.jws.soap.SOAPMessageHandlers");
        types.add("javax.xml.ws.BeginService");
        types.add("javax.xml.ws.EndService");
        types.add("javax.xml.ws.BindingType");
        types.add("javax.xml.ws.ParameterIndex");
        types.add("javax.xml.ws.RequestWrapper");
        types.add("javax.xml.ws.ResponseWrapper");
        types.add("javax.xml.ws.ServiceMode");
        types.add("javax.xml.ws.WebEndpoint");
        types.add("javax.xml.ws.WebFault");
        types.add("javax.xml.ws.WebServiceClient");
        types.add("javax.xml.ws.WebServiceProvider");
        types.add("javax.xml.ws.WebServiceRef");
        
        types.add("javax.xml.ws.security.MessageSecurity");
        supportedAnnotations = Collections.unmodifiableCollection(types);
    }

    public AnnotationProcessorFactoryImpl() {
    }

    
    public Collection<String> supportedOptions() {
        return supportedOptions;
    }

    public Collection<String> supportedAnnotationTypes() { 
        return supportedAnnotations;
    }
    
    /*
     * Return an instance of the {@link com.sun.istack.ws.WSAP WSAP} AnnotationProcesor.
     */
    public AnnotationProcessor getProcessorFor(Set<AnnotationTypeDeclaration> atds,
					AnnotationProcessorEnvironment apEnv) {
                                            
        if (wsAP == null) {
            AnnotationProcessorContext context = new AnnotationProcessorContext();
            wsAP = new WebServiceAP(null, null, null, context);    

        }
        wsAP.init(apEnv);
        return wsAP;
    }
}




