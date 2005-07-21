/**
 * $Id: AnnotationProcessorFactoryImpl.java,v 1.2 2005-07-21 23:13:23 kohlert Exp $
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
 * Returns an annotation processor that lists class names.  The
 * functionality of the processor is analogous to the ListClass doclet
 * in the Doclet Overview.
 */
public class AnnotationProcessorFactoryImpl implements AnnotationProcessorFactory {

    private static int round = 0; 
    private static WSAP wsAP;
    /*
     * Processor doesn't examine any options.
     */
    static final Collection<String> supportedOptions = Collections.unmodifiableSet(new HashSet<String>());


    /*
     * All annotation types are supported.
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
     * Return the same processor independent of what annotations are
     * present, if any.
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




