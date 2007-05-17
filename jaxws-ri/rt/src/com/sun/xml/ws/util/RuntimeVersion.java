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

package com.sun.xml.ws.util;

import com.sun.xml.ws.util.RuntimeVersionMBean;

import java.io.InputStream;
import java.io.IOException;

/**
 * Obtains the version number of the JAX-WS runtime.
 *
 * @author Kohsuke Kawaguchi
 * @author Jitendra Kotamraju
 */
public final class RuntimeVersion implements RuntimeVersionMBean {

    public static final Version VERSION;

    static {
        Version version = null;
        InputStream in = RuntimeVersion.class.getResourceAsStream("version.properties");
        try {
            version = Version.create(in);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch(IOException ioe) {
                    // Nothing to do
                }
            }
        }
        VERSION = version == null ? Version.create(null) : version;
    }

    /**
     * Get JAX-WS version
     */
    public String getVersion() {
        return VERSION.toString();
    }
    
}