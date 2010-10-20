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

package com.sun.tools.ws.processor.model.jaxb;

import com.sun.tools.xjc.api.J2SJAXBModel;
import com.sun.tools.xjc.api.Mapping;
import com.sun.tools.xjc.api.S2JJAXBModel;

import javax.xml.namespace.QName;
import java.util.*;

/**
 * Root of the JAXB Model.
 *
 * <p>
 * This is just a wrapper around a list of {@link JAXBMapping}s.
 *
 * @author Kohsuke Kawaguchi, Vivek Pandey
 */
public class JAXBModel {

    /**
     * All the mappings known to this model.
     */
    private List<JAXBMapping> mappings;   

    // index for faster access.
    private final Map<QName,JAXBMapping> byQName = new HashMap<QName,JAXBMapping>();
    private final Map<String,JAXBMapping> byClassName = new HashMap<String,JAXBMapping>();

    private com.sun.tools.xjc.api.JAXBModel rawJAXBModel;

    public com.sun.tools.xjc.api.JAXBModel getRawJAXBModel() {
        return rawJAXBModel;
    }

    /**
     * @return Schema to Java model
     */
    public S2JJAXBModel getS2JJAXBModel(){
        if(rawJAXBModel instanceof S2JJAXBModel)
            return (S2JJAXBModel)rawJAXBModel;
        return null;
    }

    /**
     * @return Java to Schema JAXBModel
     */
    public J2SJAXBModel getJ2SJAXBModel(){
        if(rawJAXBModel instanceof J2SJAXBModel)
            return (J2SJAXBModel)rawJAXBModel;
        return null;
    }


    /**
     * Default constructor for the persistence.
     */
    public JAXBModel() {}

    /**
     * Constructor that fills in the values from the given raw model
     */
    public JAXBModel( com.sun.tools.xjc.api.JAXBModel rawModel ) {
        this.rawJAXBModel = rawModel;
        if(rawModel instanceof S2JJAXBModel){
            S2JJAXBModel model = (S2JJAXBModel)rawModel;
            List<JAXBMapping> ms = new ArrayList<JAXBMapping>(model.getMappings().size());
            for( Mapping m : model.getMappings())
                ms.add(new JAXBMapping(m));
            setMappings(ms);
        }
    }

    /**
     */
    public List<JAXBMapping> getMappings() {
        return mappings;
    }

    //public void setMappings(List<JAXBMapping> mappings) {
    public void setMappings(List<JAXBMapping> mappings) {
        this.mappings = mappings;
        byQName.clear();
        byClassName.clear();
        for( JAXBMapping m : mappings ) {
            byQName.put(m.getElementName(),m);
            byClassName.put(m.getType().getName(),m);
        }
    }

    /**
     */
    public JAXBMapping get( QName elementName ) {
        return byQName.get(elementName);
    }

    /**
     */
    public JAXBMapping get( String className ) {
        return byClassName.get(className);
    }


    /**
     *
     * @return set of full qualified class names that jaxb will generate
     */
    public Set<String> getGeneratedClassNames() {
        return generatedClassNames;
    }

    public void setGeneratedClassNames(Set<String> generatedClassNames) {
        this.generatedClassNames = generatedClassNames;
    }

    private Set<String> generatedClassNames;
}
