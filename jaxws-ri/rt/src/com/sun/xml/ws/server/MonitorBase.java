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

package com.sun.xml.ws.server;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.WSFeatureList;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.EndpointAddress;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.addressing.WSEndpointReference;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundPortType;
import com.sun.xml.ws.api.model.wsdl.WSDLService;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.api.pipe.Engine;
import com.sun.xml.ws.api.pipe.Fiber;
import com.sun.xml.ws.api.pipe.FiberContextSwitchInterceptor;
import com.sun.xml.ws.api.pipe.ServerPipeAssemblerContext;
import com.sun.xml.ws.api.pipe.ServerTubeAssemblerContext;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.api.pipe.TubelineAssembler;
import com.sun.xml.ws.api.pipe.TubelineAssemblerFactory;
import com.sun.xml.ws.api.server.*;
import com.sun.xml.ws.fault.SOAPFaultBuilder;
import com.sun.xml.ws.model.wsdl.WSDLProperties;
import com.sun.xml.ws.model.wsdl.WSDLPortImpl;
import com.sun.xml.ws.resources.HandlerMessages;
import com.sun.xml.ws.util.RuntimeVersion;
import org.glassfish.external.amx.AMXGlassfish;
import org.glassfish.gmbal.AMXMetadata;
import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.InheritedAttribute;
import org.glassfish.gmbal.InheritedAttributes;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.ManagedData;
import org.glassfish.gmbal.ManagedObject;
import org.glassfish.gmbal.ManagedObjectManager;
import org.glassfish.gmbal.ManagedObjectManagerFactory;
import java.net.URL;
import javax.management.ObjectName;


import javax.annotation.PreDestroy;
import javax.xml.namespace.QName;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.Handler;
import javax.xml.stream.XMLStreamException;
import javax.management.InstanceAlreadyExistsException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Harold Carr
 */
public abstract class MonitorBase {

    private static final Logger logger = Logger.getLogger(
        com.sun.xml.ws.util.Constants.LoggingDomain + ".monitoring");

    //
    // Service-side ManagedObjectManager creation
    //

    @NotNull public ManagedObjectManager createManagedObjectManager(final boolean isEndpoint, final String rootName) {
        if (!allMonitoring 
            || (isEndpoint && !endpointMonitoring)
            || (!isEndpoint && !clientMonitoring)) 
        {
            return ManagedObjectManagerFactory.createNOOP();
        }
        return createMOMLoop(rootName, 0);
    }

    private @NotNull ManagedObjectManager createMOMLoop(final String rootName, final int unique) {
        ManagedObjectManager mom = createMOM(isFederated());
        mom = initMOM(mom);
        mom = createRoot(mom, rootName, unique);
        return mom;
    }

    private ObjectName isFederated() {
        try {
            final javax.management.MBeanServer mbeanServer = 
                java.lang.management.ManagementFactory.getPlatformMBeanServer();
            // FIX: get name from AMXGlassfish instead of hard-coded.
            final ObjectName amxName =
                // AMXGlassfish.DEFAULT.serverMon(AMXGlassfish.DEFAULT.dasName());
                new ObjectName("amx:pp=/mon,type=server-mon,name=server");
            return mbeanServer.isRegistered(amxName) ? amxName : null;
        } catch (Throwable t) {
            logger.log(Level.WARNING, "GlassFish AMX monitoring root not available.  Trying standalone.", t);
            return null;
        }
    }

    private @NotNull ManagedObjectManager createMOM(final ObjectName amxName) {
        final boolean isFederated = amxName != null;
        try {
            return
                isFederated ?
                ManagedObjectManagerFactory.createFederated(amxName)
                :
                ManagedObjectManagerFactory.createStandalone("com.sun.metro");
        } catch (Throwable t) {
            if (isFederated) {
                logger.log(Level.WARNING, "Problem while attempting to federate with GlassFish AMX monitoring..  Trying standalone.", t);
                return createMOM(null);
            } else {
                logger.log(Level.WARNING, "TBD - Ignoring exception - starting up without monitoring", t);
                return ManagedObjectManagerFactory.createNOOP();
            }
        }
    }

    private @NotNull ManagedObjectManager initMOM(final ManagedObjectManager mom) {
        try {
            if (typelibDebug != -1) {
                mom.setTypelibDebug(typelibDebug);
            }
            if (registrationDebug.equals("FINE")) {
                mom.setRegistrationDebug(ManagedObjectManager.RegistrationDebugLevel.FINE);
            } else if (registrationDebug.equals("NORMAL")) {
                mom.setRegistrationDebug(ManagedObjectManager.RegistrationDebugLevel.NORMAL);
            } else {
                mom.setRegistrationDebug(ManagedObjectManager.RegistrationDebugLevel.NONE);
            }
            mom.setRuntimeDebug(runtimeDebug);

            mom.stripPrefix(
                "com.sun.xml.ws.server",
                "com.sun.xml.ws.rx.rm.runtime.sequence");

            // Add annotations to a standard class
            mom.addAnnotation(javax.xml.ws.WebServiceFeature.class, DummyWebServiceFeature.class.getAnnotation(ManagedData.class));
            mom.addAnnotation(javax.xml.ws.WebServiceFeature.class, DummyWebServiceFeature.class.getAnnotation(Description.class));
            mom.addAnnotation(javax.xml.ws.WebServiceFeature.class, DummyWebServiceFeature.class.getAnnotation(InheritedAttributes.class));

            // Defer so we can register "this" as root from
            // within constructor.
            mom.suspendJMXRegistration();

        } catch (Throwable t) {
            try {
                mom.close();
            } catch (java.io.IOException e) {
                logger.log(Level.WARNING, "Ignoring exception caught when closing unused ManagedObjectManager", e);
            }
            logger.log(Level.WARNING, "TBD - Ignoring exception - starting up without monitoring", t);
            return ManagedObjectManagerFactory.createNOOP();
        }
        return mom;
    }

    private ManagedObjectManager createRoot(final ManagedObjectManager mom, final String rootName, int unique) {
        final String name = rootName + (unique == 0 ? "" : "-" + String.valueOf(unique));
        try {
            mom.createRoot(this, name);
            logger.log(Level.WARNING, "Monitoring rootname successfully set to: " + name);
            return mom;
        } catch (Throwable t) {
            try {
                mom.close();
            } catch (java.io.IOException e) {
                logger.log(Level.WARNING, "Ignoring exception caught when closing unused ManagedObjectManager", e);
            }
            if (t.getCause() instanceof InstanceAlreadyExistsException) {
                final String basemsg ="duplicate monitoring rootname: " + name + " : ";
                if (unique > maxUniqueEndpointRootNameRetries) {
                    final String msg = basemsg + "Giving up.";
                    logger.log(Level.WARNING, msg);
                    return ManagedObjectManagerFactory.createNOOP();
                }
                final String msg = basemsg + "Will try to make unique";
                logger.log(Level.WARNING, msg);
                return createMOMLoop(rootName, ++unique);
            } else {
                logger.log(Level.WARNING, "Error while creating monitoring root with name: " + rootName, t);
                return ManagedObjectManagerFactory.createNOOP();
            }
        }
    }

    private static boolean allMonitoring                    = true;
    private static boolean clientMonitoring                 = true;
    private static boolean endpointMonitoring               = true;
    private static int     typelibDebug                     = -1;
    private static String  registrationDebug                = "NONE";
    private static boolean runtimeDebug                     = false;
    private static int     maxUniqueEndpointRootNameRetries = 100;

    static {
        try {
            // You can control "endpoint" independently of "client" monitoring.
            String s = System.getProperty("com.sun.xml.ws.monitoring.endpoint");
            if (s != null && s.toLowerCase().equals("false")) {
                endpointMonitoring = false;
            }

            s = System.getProperty("com.sun.xml.ws.monitoring.client");
            if (s != null && s.toLowerCase().equals("false")) {
                clientMonitoring = false;
            }

            // You can turn off both "endpoint" and "client" monitoring
            // at once.
            s = System.getProperty("com.sun.xml.ws.monitoring");
            if (s != null && s.toLowerCase().equals("false")) {
                allMonitoring = false;
            }

            Integer i = Integer.getInteger("com.sun.xml.ws.monitoring.typelibDebug");
            if (i != null) {
                typelibDebug = i;
            }

            s = System.getProperty("com.sun.xml.ws.monitoring.registrationDebug");
            if (s != null) {
                registrationDebug = s.toUpperCase();
            }

            s = System.getProperty("com.sun.xml.ws.monitoring.runtimeDebug");
            if (s != null && s.toLowerCase().equals("true")) {
                runtimeDebug = true;
            }

            i = Integer.getInteger("com.sun.xml.ws.monitoring.maxUniqueEndpointRootNameRetries");
            if (i != null) {
                maxUniqueEndpointRootNameRetries = i;
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "TBD", e);
        }
    }
}


// This enables us to annotate the WebServiceFeature class even thought
// we can't explicitly put the annotations in the class itself.
@ManagedData
@Description("WebServiceFeature")
@InheritedAttributes({
        @InheritedAttribute(methodName="getID", description="unique id for this feature"),
        @InheritedAttribute(methodName="isEnabled", description="true if this feature is enabled")
})
interface DummyWebServiceFeature {}

// End of file.
