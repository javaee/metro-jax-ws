/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.api.fastinfoset;

import com.sun.xml.ws.api.FeatureConstructor;

import javax.xml.ws.WebServiceFeature;

import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.ManagedData;

/**
 * Enable or disable Fast Infoset on a Web service.
 * <p>
 * The following describes the affects of this feature with respect
 * to being enabled or disabled:
 * <ul>
 *  <li> ENABLED: In this Mode, Fast Infoset will be enabled.
 *  <li> DISABLED: In this Mode, Fast Infoset will be disabled and the 
 *       Web service will not process incoming messages or produce outgoing
 *       messages encoded using Fast Infoset.
 * </ul>
 * <p>
 * If this feature is not present on a Web service then the default behaviour
 * is equivalent to this feature being present and enabled.
 * @author Paul.Sandoz@Sun.Com
 */
@ManagedData
public class FastInfosetFeature extends WebServiceFeature {
    /**
     * Constant value identifying the {@link FastInfosetFeature}
     */
    public static final String ID = "http://java.sun.com/xml/ns/jaxws/fastinfoset";

    /**
     * Create a {@link FastInfosetFeature}. The instance created will be enabled.
     */
    public FastInfosetFeature() {
        this.enabled = true;
    }

    /**
     * Create a {@link FastInfosetFeature}
     *
     * @param enabled specifies whether this feature should
     *                be enabled or not.
     */
    @FeatureConstructor({"enabled"})
    public FastInfosetFeature(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * {@inheritDoc}
     */
    @ManagedAttribute
    public String getID() {
        return ID;
    }
}
