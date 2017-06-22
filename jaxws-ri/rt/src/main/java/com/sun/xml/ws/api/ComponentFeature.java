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

package com.sun.xml.ws.api;

import com.sun.xml.ws.api.server.Container;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.client.Stub;
import javax.xml.ws.WebServiceFeature;

/**
 * Allows registration of a {@link Component} against the {@link ComponentRegistry} implementations
 * of the {@link Container}, {@link WSEndpoint}, {@link WSService}, or {@link Stub}.  The
 * registration is guaranteed to occur early in the initialization of these objects prior to tubeline creation
 * (applicable to endpoint and stub only).
 * <p>
 * Because the Container is shared among all Stubs created from a common WSService object, this feature must 
 * be passed during WSService initialization in order to register a Component against the client-side Container.
 * <p>
 * IllegalArgumentException will be thrown if the feature is used with an inappropriate target, e.g. stub target
 * used during WSEndpoint initialization.
 * 
 * @since 2.2.6
 */
public class ComponentFeature extends WebServiceFeature implements ServiceSharedFeatureMarker {
    /**
     * Targets the object on which the Component will be registered
     * 
     */
    public static enum Target {
        CONTAINER, ENDPOINT, SERVICE, STUB
    }
        
    private final Component component;
    private final Target target;

    /**
     * Constructs ComponentFeature with indicated component and that is targeted at the Container.
     * @param component component
     */
    public ComponentFeature(Component component) {
        this(component, Target.CONTAINER);
    }
    
    /**
     * Constructs ComponentFeature with indicated component and target
     * @param component component
     * @param target target
     */
    public ComponentFeature(Component component, Target target) {
        this.enabled = true;
        this.component = component;
        this.target = target;
    }

    @Override
    public String getID() {
        return ComponentFeature.class.getName();
    }

    /**
     * Retrieves component
     * @return component
     */
    public Component getComponent() {
        return component;
    }

    /**
     * Retrieves target
     * @return target
     */
    public Target getTarget() {
        return target;
    }
}
