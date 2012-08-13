/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2012 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.model;

import com.sun.xml.bind.api.Bridge;
import com.sun.xml.bind.api.TypeReference;
import com.sun.xml.ws.api.model.JavaMethod;
import com.sun.xml.ws.api.model.Parameter;
import com.sun.xml.ws.api.model.ParameterBinding;
import com.sun.xml.ws.spi.db.RepeatedElementBridge;
import com.sun.xml.ws.spi.db.WrapperComposite;
import com.sun.xml.ws.spi.db.XMLBridge;
import com.sun.xml.ws.spi.db.TypeInfo;

import javax.jws.WebParam.Mode;
import javax.xml.namespace.QName;
import javax.xml.ws.Holder;
import java.util.List;

/**
 * runtime Parameter that abstracts the annotated java parameter
 *
 * <p>
 * A parameter may be bound to a header, a body, or an attachment.
 * Note that when it's bound to a body, it's bound to a body,
 * it binds to the whole payload.
 *
 * <p>
 * Sometimes multiple Java parameters are packed into the payload,
 * in which case the subclass {@link WrapperParameter} is used.
 *
 * @author Vivek Pandey
 */
public class ParameterImpl implements Parameter {

    private ParameterBinding binding;
    private ParameterBinding outBinding;
    private String partName;
    private final int index;
    private final Mode mode;
    /** @deprecated */
    private TypeReference typeReference;
    private TypeInfo typeInfo;
    private QName name;
    private final JavaMethodImpl parent;
    
    WrapperParameter wrapper;
    TypeInfo itemTypeInfo;

    public ParameterImpl(JavaMethodImpl parent, TypeInfo type, Mode mode, int index) {
        assert type != null;

        this.typeInfo = type;
        this.name = type.tagName;
        this.mode = mode;
        this.index = index;
        this.parent = parent;
    }

    public AbstractSEIModelImpl getOwner() {
        return parent.owner;
    }

    public JavaMethod getParent() {
        return parent;
    }

    /**
     * @return Returns the name.
     */
    public QName getName() {
        return name;
    }
    
    public XMLBridge getXMLBridge() {
        return getOwner().getXMLBridge(typeInfo);
    }
    
    public XMLBridge getInlinedRepeatedElementBridge() {
        TypeInfo itemType = getItemType();
        if (itemType != null) {
            XMLBridge xb = getOwner().getXMLBridge(itemType);
            if (xb != null) return new RepeatedElementBridge(typeInfo, xb);
        }
        return null;
    }
    
    public TypeInfo getItemType() {
        if (itemTypeInfo != null) return itemTypeInfo;
        //RpcLit cannot inline repeated element in wrapper
        if (parent.getBinding().isRpcLit() || wrapper == null) return null;
        //InlinedRepeatedElementBridge is only used for dynamic wrapper (no wrapper class) 
        if (!WrapperComposite.class.equals(wrapper.getTypeInfo().type)) return null;
        if (!getBinding().isBody()) return null;
        itemTypeInfo = typeInfo.getItemType();
        return  itemTypeInfo;
    }

    /**  @deprecated  */
    public Bridge getBridge() {
        return getOwner().getBridge(typeReference);
    }
    /**  @deprecated  */
    protected Bridge getBridge(TypeReference typeRef) {
        return getOwner().getBridge(typeRef);
    }

    /**
     * TODO: once the model gets JAXBContext, shouldn't {@link Bridge}s
     * be made available from model objects?
     * @deprecated use getTypeInfo
     * @return Returns the TypeReference associated with this Parameter
     */
    public TypeReference getTypeReference() {
        return typeReference;
    }
    public TypeInfo getTypeInfo() {
        return typeInfo;
    }

    /**
     * Sometimes we need to overwrite the typeReferenc, such as during patching for rpclit
     * @see AbstractSEIModelImpl#applyParameterBinding(com.sun.xml.ws.model.wsdl.WSDLBoundPortTypeImpl)
     * @deprecated 
     */
    void setTypeReference(TypeReference type){
        typeReference = type;
        name = type.tagName;
    }


    public Mode getMode() {
        return mode;
    }

    public int getIndex() {
        return index;
    }

    /**
     * @return true if <tt>this instanceof {@link WrapperParameter}</tt>.
     */
    public boolean isWrapperStyle() {
        return false;
    }

    public boolean isReturnValue() {
        return index==-1;
    }

    /**
     * @return the Binding for this Parameter
     */
    public ParameterBinding getBinding() {
        if(binding == null)
            return ParameterBinding.BODY;
        return binding;
    }

    /**
     * @param binding
     */
    public void setBinding(ParameterBinding binding) {
        this.binding = binding;
    }

    public void setInBinding(ParameterBinding binding){
        this.binding = binding;
    }

    public void setOutBinding(ParameterBinding binding){
        this.outBinding = binding;
    }

    public ParameterBinding getInBinding(){
        return binding;
    }

    public ParameterBinding getOutBinding(){
        if(outBinding == null)
            return binding;
        return outBinding;
    }

    public boolean isIN() {
        return mode==Mode.IN;
    }

    public boolean isOUT() {
        return mode==Mode.OUT;
    }

    public boolean isINOUT() {
        return mode==Mode.INOUT;
    }

    /**
     * If true, this parameter maps to the return value of a method invocation.
     *
     * <p>
     * {@link JavaMethodImpl#getResponseParameters()} is guaranteed to have
     * at most one such {@link ParameterImpl}. Note that there coule be none,
     * in which case the method returns <tt>void</tt>.
     */
    public boolean isResponse() {
        return index == -1;
    }


    /**
     * Gets the holder value if applicable. To be called for inbound client side
     * message.
     * 
     * @param obj
     * @return the holder value if applicable.
     */
    public Object getHolderValue(Object obj) {
        if (obj != null && obj instanceof Holder)
            return ((Holder) obj).value;
        return obj;
    }

    public String getPartName() {
        if(partName == null)
            return name.getLocalPart();
        return partName;
    }

    public void setPartName(String partName) {
        this.partName = partName;
    }

    void fillTypes(List<TypeInfo> types) {
        TypeInfo itemType = getItemType();
        types.add((itemType != null) ? itemType : getTypeInfo());
    }
}
