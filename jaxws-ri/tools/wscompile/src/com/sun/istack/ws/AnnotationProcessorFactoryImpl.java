/**
 * $Id: AnnotationProcessorFactoryImpl.java,v 1.3 2005-07-29 19:54:47 kohlert Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.istack.ws;


import java.util.*;

import com.sun.mirror.apt.*;
import com.sun.mirror.declaration.*;

import com.sun.tools.ws.processor.modeler.annotation.AnnotationProcessorContext;


/*
 * The JAX-WS {@com.sun.mirror.apt.AnnotationProcessorFactory AnnotationProcessorFactory}
 * class used by the <a href="http://java.sun.com/j2se/1.5.0/docs/tooldocs/share/apt.html">APT</a>
 * framework.
 */
public class AnnotationProcessorFactoryImpl implements AnnotationProcessorFactory {

    private static int round = 0; 
    private static WSAP wsAP;
    /*
     * Processor doesn't examine any options.
     */
    static final Collection<String> supportedOptions = Collections.unmodifiableSet(new HashSet<String>());


    /*
     * Supportes javax.jws.*, javax.jws.soap.* and javax.xml.ws.* annotations.
     */
    static Collection<String> supportedAnnotations;
    static {
        Collection<String> types = new HashSet<String>();
        types.add("javax.jws.*");
        types.add("javax.jws.soap.*");
        types.add("javax.xml.ws.*");
        types.add("com.sun.xml.ws.*");
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
            wsAP = new WSAP(context);
        }
        wsAP.init(apEnv);
        return wsAP;
    }
}




