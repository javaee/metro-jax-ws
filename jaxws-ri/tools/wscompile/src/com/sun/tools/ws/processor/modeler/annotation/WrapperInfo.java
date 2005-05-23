/**
 * $Id: WrapperInfo.java,v 1.1 2005-05-23 23:23:52 bbissett Exp $
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
public class WrapperInfo {
    public String wrapperName;
    public List<MemberInfo> members;

    /** Creates a new instance of FaultInfo */
    public WrapperInfo() {
    }
    public WrapperInfo(String wrapperName) {
        this.wrapperName = wrapperName;
    }

    public void setWrapperName(String wrapperName) {
        this.wrapperName = wrapperName;
    }

    public String getWrapperName() {
        return wrapperName;
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
