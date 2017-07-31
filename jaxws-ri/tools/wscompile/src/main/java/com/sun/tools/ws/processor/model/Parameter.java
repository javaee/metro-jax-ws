/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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
