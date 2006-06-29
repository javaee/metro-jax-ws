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
package com.sun.tools.ws.processor.model.jaxb;

import javax.xml.namespace.QName;

import com.sun.tools.ws.processor.model.java.JavaStructureMember;
/**
 * @author Kathy Walsh, Vivek Pandey
 *
 * 
 */

public class JAXBElementMember {
    public JAXBElementMember() {
    }
    public JAXBElementMember(QName name, JAXBType type) {
        this(name, type, null);
    }
    public JAXBElementMember(QName name, JAXBType type,
            JavaStructureMember javaStructureMember) {
        _name = name;
        _type = type;
        _javaStructureMember = javaStructureMember;
    }
    public QName getName() {
        return _name;
    }
    public void setName(QName n) {
        _name = n;
    }
    public JAXBType getType() {
        return _type;
    }
    public void setType(JAXBType t) {
        _type = t;        
    }    
    public boolean isRepeated() {
        return _repeated;
    }
    public void setRepeated(boolean b) {
        _repeated = b;
    }
    public JavaStructureMember getJavaStructureMember() {
        return _javaStructureMember;
    }
    public void setJavaStructureMember(JavaStructureMember javaStructureMember) {
        _javaStructureMember = javaStructureMember;
    }
    public boolean isInherited() {
        return isInherited;
    }
    public void setInherited(boolean b) {
        isInherited = b;
    }
    public JAXBProperty getProperty() {
        if(_prop == null && _type != null) {
            for (JAXBProperty prop: _type.getWrapperChildren()){
                if(prop.getElementName().equals(_name))
                    setProperty(prop);
            }
        }
        return _prop;
    }
    public void setProperty(JAXBProperty prop) {
        _prop = prop;
    }
    
    private QName _name;
    private JAXBType _type;
    private JavaStructureMember _javaStructureMember;
    private boolean _repeated;
    private boolean isInherited = false;
    private JAXBProperty _prop;
    private static final String JAXB_UNIQUE_PARRAM = "__jaxbUniqueParam_";
}