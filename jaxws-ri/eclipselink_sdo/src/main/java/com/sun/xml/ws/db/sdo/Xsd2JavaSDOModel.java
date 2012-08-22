/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.db.sdo;

import commonj.sdo.Property;
import commonj.sdo.Type;
import commonj.sdo.helper.HelperContext;
import commonj.sdo.helper.XSDHelper;
import org.eclipse.persistence.sdo.SDOType;
import org.eclipse.persistence.sdo.helper.CodeWriter;
import org.eclipse.persistence.sdo.helper.SDOClassGenerator;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: giglee
 * Date: May 14, 2009
 * Time: 11:11:25 AM
 * To change this template use File | Settings | File Templates.
 */

/**
 * This class contains the result of java type mapping on a set of schemas.
 * It is only aware of the global elements for now.
 */
public class Xsd2JavaSDOModel {


    private List<SDOType> types = null;
    private HelperContext context = null;

    public Xsd2JavaSDOModel(HelperContext context, List<SDOType> types) {
        this.context = context;
        this.types = types;        
    }

    /**
     * write the java class to the code writer, see toplink CodeWriter interface
     * @param cw
     */
    public void generateCode(CodeWriter cw) {
        if (types == null) {
            return;
        }
        SDOClassGenerator generator = new SDOClassGenerator(context);
        generator.generate(cw, types);
    }

    /**
     * Receive a list of java classes modeled by this xsd2java model
     * @return
     */
    public List<String> getClassList() {
        // the model only needs to know the interface class???
        List<String> list = new ArrayList<String>();
        for (SDOType type : types) {
            list.add(type.getInstanceClassName());
        }
        return list;
    }

    /**
     * Return the type qname used to define this java class
     *
     * @param javaClass
     * @return
     */
    public QName getXsdTypeName(String javaClass) {
        for (SDOType type : types) {
            if (type.getInstanceClassName().equals(javaClass)) {
                return ((SDOType) type).getQName();
            }
        }
        return null;
    }

    /**
     * return the java type used for the element, only Global elements can be located.
     * Containing types are not searched
     *
     * @param qname
     * @return
     */
    public String getJavaTypeForElementName(QName qname) {
        XSDHelper xsdHelper = context.getXSDHelper();
        Property globalProperty = xsdHelper.getGlobalProperty(qname.getNamespaceURI(), qname.getLocalPart(), true);
        if (globalProperty == null) {
           throw new RuntimeException("Given element with name: " + qname + "is not found."); 
        }
        Type elementType = globalProperty.getType();
        if (elementType == null) {
            throw new RuntimeException("Given element with name: " + qname + "is not found.");
        }
        return ((SDOType) elementType).getInstanceClassName();
    }

    /**
     * return the java type for a given xsd type
     * @param name
     * @return
     */
    public String getJavaTypeForElementType(QName name) {
        if (types != null) {
            for (SDOType type : types) {
                QName qname = type.getXsdType();
                if (qname != null && qname.equals(name)) {
                    return type.getInstanceClassName();
                }
            }
        }
        return null;
    }
    
}
