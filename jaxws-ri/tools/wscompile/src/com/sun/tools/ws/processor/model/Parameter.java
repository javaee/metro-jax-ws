/*
 * $Id: Parameter.java,v 1.3 2005-09-10 19:49:38 kohsuke Exp $
 */

/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */

package com.sun.tools.ws.processor.model;

import java.util.ArrayList;
import java.util.List;

import com.sun.tools.ws.processor.model.java.JavaParameter;

/**
 *
 * @author WS Development Team
 */
public class Parameter extends ModelObject {

    public Parameter() {}

    public Parameter(String name) {
        this.name = name;
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

    public int getParameterOrderPosition() {
        return parameterOrderPosition;
    }

    public void setParameterOrderPosition(int parameterOrderPosition) {
        this.parameterOrderPosition = parameterOrderPosition;
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

}
