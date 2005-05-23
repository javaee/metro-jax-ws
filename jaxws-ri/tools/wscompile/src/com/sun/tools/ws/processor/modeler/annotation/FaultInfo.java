/**
 * $Id: FaultInfo.java,v 1.1 2005-05-23 23:23:50 bbissett Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
