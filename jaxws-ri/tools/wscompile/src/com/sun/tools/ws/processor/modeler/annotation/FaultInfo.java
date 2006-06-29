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
package com.sun.tools.ws.processor.modeler.annotation;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;
import com.sun.tools.ws.processor.modeler.annotation.*;

/**
 *
 * @author  dkohlert
 */
public class FaultInfo {
    public String beanName;
    public TypeMoniker beanTypeMoniker;
    public boolean isWSDLException;
    public QName elementName;
    public List<MemberInfo> members;

    /** Creates a new instance of FaultInfo */
    public FaultInfo() {
    }
    public FaultInfo(String beanName) {
        this.beanName = beanName;
    }
    public FaultInfo(String beanName, boolean isWSDLException) {
        this.beanName = beanName;
        this.isWSDLException = isWSDLException;
    }
    public FaultInfo(TypeMoniker typeMoniker, boolean isWSDLException) {
        this.beanTypeMoniker = typeMoniker;
        this.isWSDLException = isWSDLException;
    }

    public void setIsWSDLException(boolean isWSDLException) {
        this.isWSDLException = isWSDLException;
    }

    public boolean isWSDLException() {
        return isWSDLException;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public String getBeanName() {
        return beanName;
    }

    public void setElementName(QName elementName) {
        this.elementName =  elementName;
    }

    public QName getElementName() {
        return elementName;
    }
    public void setBeanTypeMoniker(TypeMoniker typeMoniker) {
        this.beanTypeMoniker = typeMoniker;
    }
    public TypeMoniker getBeanTypeMoniker() {
        return beanTypeMoniker;
    }
    public List<MemberInfo> getMembers() {
        return members;
    }
    public void setMembers(List<MemberInfo> members) {
        this.members = members;
    }
    public void addMember(MemberInfo member) {
        if (members == null)
            members = new ArrayList<MemberInfo>();
        members.add(member);
    }
}
