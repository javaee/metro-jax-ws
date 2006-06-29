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
package com.sun.tools.ws.processor.modeler.wsdl;

import com.sun.tools.xjc.api.ClassNameAllocator;
import com.sun.tools.ws.processor.util.ClassNameCollector;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

/**
 * @author Vivek Pandey
 *         <p/>
 *         Implementation of Callback interface that allows the driver of the XJC API to rename JAXB-generated classes/interfaces/enums.
 */
public class ClassNameAllocatorImpl implements ClassNameAllocator {
    public ClassNameAllocatorImpl(ClassNameCollector classNameCollector) {
        this.classNameCollector = classNameCollector;
        this.jaxbClasses = new HashSet<String>();
    }

    public String assignClassName(String packageName, String className) {
        if(packageName== null || className == null){
            //TODO: throw Exception
            return className;
        }

        //if either of the values are empty string return the default className
        if(packageName.equals("") || className.equals(""))
            return className;

        String fullClassName = packageName+"."+className;

        // Check if there is any conflict with jaxws generated classes
        Set<String> seiClassNames = classNameCollector.getSeiClassNames();
        if(seiClassNames != null && seiClassNames.contains(fullClassName)){
            className += TYPE_SUFFIX;
        }

        jaxbClasses.add(packageName+"."+className);
        return className;
    }

    /**
     *
     * @return jaxbGenerated classNames
     */
    public Set<String> getJaxbGeneratedClasses() {
        return jaxbClasses;
    }

    private final static String TYPE_SUFFIX = "_Type";
    private ClassNameCollector classNameCollector;
    private Set<String> jaxbClasses;
}
