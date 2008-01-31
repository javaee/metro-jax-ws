/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

package com.sun.tools.ws.impl;

import com.sun.codemodel.ClassType;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JDefinedClass;
import com.sun.tools.ws.wscompile.ErrorReceiver;
import com.sun.tools.ws.wscompile.WsimportOptions;
import com.sun.tools.xjc.api.S2JJAXBModel;
import com.sun.tools.xjc.api.XJC;
import org.jvnet.wom.api.WSDLDefinitions;
import org.jvnet.wom.api.WSDLSet;
import org.jvnet.wom.api.WSDLTypes;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Vivek Pandey
 */
public final class ModelerContext {
    private final ErrorReceiver errReceiver;
    private final WsimportOptions options;
    private final WSDLSet wsdlSet;
    private final Map<String, String> pkgMap = new HashMap<String, String>();
    private S2JJAXBModel jaxbModel;


    public ModelerContext(WSDLSet wsdl, WsimportOptions options, ErrorReceiver errReceiver) {
        this.errReceiver = errReceiver;
        this.options = options;
        this.wsdlSet = wsdl;
        Iterator<WSDLTypes> types = wsdlSet.types();

        if (types.hasNext()) {
            WSDLTypes type = types.next();
            XMLSchemaExtensionHandler.XMLSchema schema = type.getFirstExtension(XMLSchemaExtensionHandler.XMLSchema.class);
            jaxbModel = schema.getJAXBModel();
        }
    }

    public ErrorReceiver getErrReceiver() {
        return errReceiver;
    }

    public WsimportOptions getOptions() {
        return options;
    }

    public String getPackageName(String uri) {
        if (pkgMap.get(uri) != null)
            return pkgMap.get(uri);

        WSDLDefinitions def = wsdlSet.getWSDL(uri);
        String packageName = XJC.getDefaultPackageName(uri);

        //apply package customization, if any
        if (def != null) {
            BindingInfo bindingInfo = def.getFirstExtension(BindingInfo.class);
            if (bindingInfo != null) {
                BPackage pkgBinding = bindingInfo.getChild(BPackage.class);
                if (pkgBinding != null) {
                    packageName = pkgBinding.getPackageName();
                }
            }
        }
        //user defined package name defined thru -p option
        //overrides anything else.
        if (options.defaultPackage != null) {
            packageName = options.defaultPackage;
        }
        pkgMap.put(uri, packageName);
        return packageName;
    }

    public WSDLSet getWsdlSet() {
        return wsdlSet;
    }

    public JDefinedClass getClass(String className, ClassType type) throws JClassAlreadyExistsException {
        JDefinedClass cls;
        try {
            cls = options.getCodeModel()._class(className, type);
        } catch (JClassAlreadyExistsException e) {
            cls = options.getCodeModel()._getClass(className);
            if (cls == null)
                throw e;
        }
        return cls;
    }

    public S2JJAXBModel getJaxbModel() {
        return jaxbModel;
    }
}
