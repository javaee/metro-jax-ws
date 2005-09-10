/*
 * $Id: JAXWSClassFactory.java,v 1.2 2005-09-10 19:49:50 kohsuke Exp $
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

package com.sun.tools.ws.util;

import java.util.Properties;

import com.sun.tools.ws.processor.config.WSDLModelInfo;
import com.sun.tools.ws.processor.generator.Names;
import com.sun.tools.ws.processor.modeler.wsdl.WSDLModeler20;
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
            wsdlModeler =
                (WSDLModelerBase) new WSDLModeler20(modelInfo, options);
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
