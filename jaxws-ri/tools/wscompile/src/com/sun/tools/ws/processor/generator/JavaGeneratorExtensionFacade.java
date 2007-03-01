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

package com.sun.tools.ws.processor.generator;

import com.sun.codemodel.JMethod;
import com.sun.tools.ws.api.TJavaGeneratorExtension;
import com.sun.tools.ws.api.wsdl.TWSDLOperation;

/**
 * @author Arun Gupta
 */
public final class JavaGeneratorExtensionFacade extends TJavaGeneratorExtension {
    private final TJavaGeneratorExtension[] extensions;

    JavaGeneratorExtensionFacade(TJavaGeneratorExtension... extensions) {
        assert extensions != null;
        this.extensions = extensions;
    }

    public void writeMethodAnnotations(TWSDLOperation wsdlOperation, JMethod jMethod) {
        for (TJavaGeneratorExtension e : extensions) {
            e.writeMethodAnnotations(wsdlOperation, jMethod);
        }
    }
}
