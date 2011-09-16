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

    private WebServiceAP wsAP;
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
        types.add("javax.xml.ws.BindingType");
        types.add("javax.xml.ws.RequestWrapper");
        types.add("javax.xml.ws.ResponseWrapper");
        types.add("javax.xml.ws.ServiceMode");
        types.add("javax.xml.ws.WebEndpoint");
        types.add("javax.xml.ws.WebFault");
        types.add("javax.xml.ws.WebServiceClient");
        types.add("javax.xml.ws.WebServiceProvider");
        types.add("javax.xml.ws.WebServiceRef");
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
            wsAP = new WebServiceAP(null, context, null, null);

        }
        wsAP.init(apEnv);
        return wsAP;
    }
}




