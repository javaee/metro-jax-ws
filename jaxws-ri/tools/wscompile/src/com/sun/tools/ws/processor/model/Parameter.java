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

package com.sun.tools.ws.processor.model;

import com.sun.tools.ws.processor.model.java.JavaParameter;
import com.sun.tools.ws.wsdl.framework.Entity;
import com.sun.tools.ws.wsdl.document.MessagePart;

import javax.jws.WebParam.Mode;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author WS Development Team
 */
public class Parameter extends ModelObject {
    private final String entityName;

    public Parameter(String name, Entity entity) {
        super(entity);
        this.name = name;
        if(entity instanceof com.sun.tools.ws.wsdl.document.Message){
            this.entityName = ((com.sun.tools.ws.wsdl.document.Message)entity).getName();
        }else if(entity instanceof MessagePart){
            this.entityName = ((MessagePart)entity).getName();
        }else{
            this.entityName = name;
        }

    }


    public String getEntityName() {
        return entityName;
    }

    public String getName() {
        return name;
    }

    public void setName(String s) {
        name = s;
    }

    public JavaParameter getJavaParameter() {
        return javaParameter;
    }

    public void setJavaParameter(JavaParameter p) {
        javaParameter = p;
    }

    public AbstractType getType() {
        return type;
    }

    public void setType(AbstractType t) {
        type = t;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String t) {
        typeName = t;
    }

    public Block getBlock() {
        return block;
    }

    public void setBlock(Block d) {
        block = d;
    }

    public Parameter getLinkedParameter() {
        return link;
    }

    public void setLinkedParameter(Parameter p) {
        link = p;
    }

    public boolean isEmbedded() {
        return embedded;
    }

    public void setEmbedded(boolean b) {
        embedded = b;
    }

    public void accept(ModelVisitor visitor) throws Exception {
        visitor.visit(this);
    }

    private String name;
    private JavaParameter javaParameter;
    private AbstractType type;
    private Block block;
    private Parameter link;
    private boolean embedded;
    private String typeName;
    private String customName;
    private Mode mode;

    public int getParameterIndex() {
        return parameterOrderPosition;
    }

    public void setParameterIndex(int parameterOrderPosition) {
        this.parameterOrderPosition = parameterOrderPosition;
    }

    public boolean isReturn(){
        return (parameterOrderPosition == -1);
    }

    // 0 is the first parameter, -1 is the return type
    private int parameterOrderPosition;
    /**
     * @return Returns the customName.
     */
    public String getCustomName() {
        return customName;
    }
    /**
     * @param customName The customName to set.
     */
    public void setCustomName(String customName) {
        this.customName = customName;
    }

    private List<String> annotations = new ArrayList<String>();

    /**
     * @return Returns the annotations.
     */
    public List<String> getAnnotations() {
        return annotations;
    }


    /**
     * @param annotations The annotations to set.
     */
    public void setAnnotations(List<String> annotations) {
        this.annotations = annotations;
    }

    public void setMode(Mode mode){
        this.mode = mode;
    }

    public boolean isIN(){
        return (mode == Mode.IN);
    }

    public boolean isOUT(){
        return (mode == Mode.OUT);
    }

    public boolean isINOUT(){
        return (mode == Mode.INOUT);
    }



}
