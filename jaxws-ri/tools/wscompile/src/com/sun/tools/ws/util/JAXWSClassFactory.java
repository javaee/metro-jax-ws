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

package com.sun.tools.ws.util;

import java.util.Properties;

import com.sun.tools.ws.processor.config.WSDLModelInfo;
import com.sun.tools.ws.processor.generator.Names;
import com.sun.tools.ws.processor.modeler.wsdl.WSDLModeler;
import com.sun.tools.ws.processor.modeler.wsdl.WSDLModelerBase;
import com.sun.tools.ws.wsdl.framework.AbstractDocument;
import com.sun.xml.ws.util.VersionUtil;

/**
 * Singleton factory class to instantiate concrete classes based on the jaxws version
 * to be used to generate the code.
 *
 * @author WS Development Team
 */
public class JAXWSClassFactory {
    private static final JAXWSClassFactory factory = new JAXWSClassFactory();

    private static String classVersion = VersionUtil.JAXWS_VERSION_DEFAULT;

    private JAXWSClassFactory() {
    }

    /**
     * Get the factory instance for the default version.
     * @return        JAXWSClassFactory instance
     */
    public static JAXWSClassFactory newInstance() {
        return factory;
    }

    /**
     * Sets the version to a static classVersion
     * @param version
     */
    public void setSourceVersion(String version) {
        if (version == null)
            version = VersionUtil.JAXWS_VERSION_DEFAULT;

        if (!VersionUtil.isValidVersion(version)) {
            // TODO: throw exception
        } else
            classVersion = version;
    }

    /**
     * Returns the WSDLModeler for specific target version.
     *
     * @param modelInfo
     * @param options
     * @return the WSDLModeler for specific target version.
     */
    public WSDLModelerBase createWSDLModeler(
        WSDLModelInfo modelInfo,
        Properties options) {
        WSDLModelerBase wsdlModeler = null;
        if (classVersion.equals(VersionUtil.JAXWS_VERSION_20))
            wsdlModeler = new WSDLModeler(modelInfo, options);
        else {
            // TODO: throw exception
        }
        return wsdlModeler;
    }

    /**
     * Returns the Names for specific target version.
     * //bug fix:4904604
     * @return instance of Names
     */
    public Names createNames() {
        Names names = new Names();
        return names;
    }

    public String getVersion() {
        return classVersion;
    }
}
