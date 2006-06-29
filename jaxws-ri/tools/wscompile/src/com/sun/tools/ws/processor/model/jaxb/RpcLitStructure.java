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

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import com.sun.tools.ws.processor.model.AbstractType;

/**
 * @author Vivek Pandey
 *
 * RPC Structure that will be used to create RpcLitPayload latter
 */
public class RpcLitStructure extends AbstractType {
    private List<RpcLitMember> members;
    private JAXBModel jaxbModel;

    /**
     *
     */
    public RpcLitStructure() {
        super();
        // TODO Auto-generated constructor stub
    }
    public RpcLitStructure(QName name, JAXBModel jaxbModel){
        setName(name);
        this.jaxbModel = jaxbModel;
        this.members = new ArrayList<RpcLitMember>();

    }
    public RpcLitStructure(QName name, JAXBModel jaxbModel, List<RpcLitMember> members){
        setName(name);
        this.members = members;
    }

    public void accept(JAXBTypeVisitor visitor) throws Exception {
        visitor.visit(this);
    }

    public List<RpcLitMember> getRpcLitMembers(){
        return members;
    }

    public List<RpcLitMember> setRpcLitMembers(List<RpcLitMember> members){
        return this.members = members;
    }

    public void addRpcLitMember(RpcLitMember member){
        members.add(member);
    }
    /**
     * @return Returns the jaxbModel.
     */
    public JAXBModel getJaxbModel() {
        return jaxbModel;
    }
    /**
     * @param jaxbModel The jaxbModel to set.
     */
    public void setJaxbModel(JAXBModel jaxbModel) {
        this.jaxbModel = jaxbModel;
    }

    public boolean isLiteralType() {
        return true;
    }
}
