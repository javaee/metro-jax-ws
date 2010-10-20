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

package com.sun.xml.ws.server;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.ws.api.config.management.policy.ManagedClientAssertion;
import com.sun.xml.ws.api.config.management.policy.ManagedServiceAssertion;
import com.sun.xml.ws.api.config.management.policy.ManagementAssertion.Setting;
import com.sun.xml.ws.api.server.BoundEndpoint;
import com.sun.xml.ws.api.server.Container;
import com.sun.xml.ws.api.server.Module;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.client.Stub;
import org.glassfish.external.amx.AMXGlassfish;
import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.InheritedAttribute;
import org.glassfish.gmbal.InheritedAttributes;
import org.glassfish.gmbal.ManagedData;
import org.glassfish.gmbal.ManagedObjectManager;
import org.glassfish.gmbal.ManagedObjectManagerFactory;
import java.io.IOException;
import java.lang.reflect.*;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.ObjectName;
import javax.xml.namespace.QName;

// BEGIN IMPORTS FOR RewritingMOM
import java.util.ResourceBundle ;
import java.io.Closeable ;
import java.lang.reflect.AnnotatedElement ;
import java.lang.annotation.Annotation ;
import javax.management.ObjectName ;
import javax.management.MBeanServer ;
import org.glassfish.gmbal.AMXClient;
import org.glassfish.gmbal.GmbalMBean;
// END IMPORTS FOR RewritingMOM

/**
 * @author Harold Carr
 */
public abstract class MonitorBase {

    private static final Logger logger = Logger.getLogger(
        com.sun.xml.ws.util.Constants.LoggingDomain + ".monitoring");

    /**
     * Endpoint monitoring is ON by default.
     *
     * prop    |  no assert | assert/no mon | assert/mon off | assert/mon on
     * -------------------------------------------------------------------
     * not set |    on      |      on       |     off        |     on
     * false   |    off     |      off      |     off        |     off
     * true    |    on      |      on       |     off        |     on
     */
    @NotNull public ManagedObjectManager createManagedObjectManager(final WSEndpoint endpoint) {
        // serviceName + portName identifies the managed objects under it.
        // There can be multiple services in the container.
        // The same serviceName+portName can live in different apps at
        // different endpoint addresses.
        //
        // In general, monitoring will add -N, where N is unique integer,
        // in case of collisions.
        //
        // The endpoint address would be unique, but we do not know
        // the endpoint address until the first request comes in,
        // which is after monitoring is setup.

        String rootName = 
            endpoint.getServiceName().getLocalPart()
            + "-"
            + endpoint.getPortName().getLocalPart();

        if (rootName.equals("-")) {
            rootName = "provider";
        }

        // contextPath is not always available
        final String contextPath = getContextPath(endpoint);
        if (contextPath != null) {
            rootName = contextPath + "-" + rootName;
        }

        final ManagedServiceAssertion assertion =
            ManagedServiceAssertion.getAssertion(endpoint);
        if (assertion != null) {
            final String id = assertion.getId();
            if (id != null) {
                rootName = id;
            }
            if (assertion.monitoringAttribute() == Setting.OFF) {
                return disabled("This endpoint", rootName);
            }
        }

        if (endpointMonitoring.equals(Setting.OFF)) {
            return disabled("Global endpoint", rootName);
        }
        return createMOMLoop(rootName, 0);
    }

    private String getContextPath(final WSEndpoint endpoint) {
        try {
            Container container = endpoint.getContainer();
            Method getSPI = 
                container.getClass().getDeclaredMethod("getSPI", Class.class);
            getSPI.setAccessible(true);
            Class servletContextClass = 
                Class.forName("javax.servlet.ServletContext");
            Object servletContext =
                getSPI.invoke(container, servletContextClass);
            if (servletContext != null) {
                Method getContextPath = servletContextClass.getDeclaredMethod("getContextPath");
                getContextPath.setAccessible(true);
                return (String) getContextPath.invoke(servletContext);
            }
            return null;
        } catch (Throwable t) {
            logger.log(Level.FINEST, "getContextPath", t);
        }
        return null;
    }

    /**
     * Client monitoring is OFF by default because there is
     * no standard stub.close() method.  Therefore people do
     * not typically close a stub when they are done with it
     * (even though the RI does provide a .close).
     * <pre>
     * prop    |  no assert | assert/no mon | assert/mon off | assert/mon on
     * -------------------------------------------------------------------
     * not set |    off     |      off      |     off        |     on
     * false   |    off     |      off      |     off        |     off
     * true    |    on      |      on       |     off        |     on
     * </pre>
    */
    @NotNull public ManagedObjectManager createManagedObjectManager(final Stub stub) {
        String rootName = stub.requestContext.getEndpointAddress().toString();

        final ManagedClientAssertion assertion = 
            ManagedClientAssertion.getAssertion(stub.getPortInfo());
        if (assertion != null) {
            final String id = assertion.getId();
            if (id != null) {
                rootName = id;
            }
            if (assertion.monitoringAttribute() == Setting.OFF) {
                return disabled("This client", rootName);
            } else if (assertion.monitoringAttribute() == Setting.ON &&
                       clientMonitoring != Setting.OFF) {
                return createMOMLoop(rootName, 0);
            }
        }

        if (clientMonitoring == Setting.NOT_SET ||
            clientMonitoring == Setting.OFF)
        {
            return disabled("Global client", rootName);
        }
        return createMOMLoop(rootName, 0);
    }

    @NotNull private ManagedObjectManager disabled(final String x, final String rootName) {
        final String msg = x + " monitoring disabled. " + rootName + " will not be monitored";
        logger.log(Level.CONFIG, msg);
        return ManagedObjectManagerFactory.createNOOP();
    }

    private @NotNull ManagedObjectManager createMOMLoop(final String rootName, final int unique) {
        final boolean isFederated = AMXGlassfish.getGlassfishVersion() != null;
        ManagedObjectManager mom = createMOM(isFederated);
        mom = initMOM(mom);
        mom = createRoot(mom, rootName, unique);
        return mom;
    }

    private @NotNull ManagedObjectManager createMOM(final boolean isFederated) {
        try {
            return new RewritingMOM(isFederated ?
                ManagedObjectManagerFactory.createFederated(
                    AMXGlassfish.DEFAULT.serverMon(AMXGlassfish.DEFAULT.dasName()))
                :
                ManagedObjectManagerFactory.createStandalone("com.sun.metro"));
        } catch (Throwable t) {
            if (isFederated) {
                logger.log(Level.CONFIG, "Problem while attempting to federate with GlassFish AMX monitoring.  Trying standalone.", t);
                return createMOM(false);
            } else {
                logger.log(Level.WARNING, "Ignoring exception - starting up without monitoring", t);
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

            // Instead of GMBAL throwing an exception and logging
            // duplicate name, just have it return null.
            mom.suppressDuplicateRootReport(true);

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
            } catch (IOException e) {
                logger.log(Level.CONFIG, "Ignoring exception caught when closing unused ManagedObjectManager", e);
            }
            logger.log(Level.WARNING, "Ignoring exception - starting up without monitoring", t);
            return ManagedObjectManagerFactory.createNOOP();
        }
        return mom;
    }

    private ManagedObjectManager createRoot(final ManagedObjectManager mom, final String rootName, int unique) {
        final String name = rootName + (unique == 0 ? "" : "-" + String.valueOf(unique));
        try {
            final Object ignored = mom.createRoot(this, name);
            if (ignored != null) {
                logger.log(Level.INFO, "Metro monitoring rootname successfully set to: " + mom.getObjectName(mom.getRoot()));
                return mom;
            }
            try {
                mom.close();
            } catch (IOException e) {
                logger.log(Level.CONFIG, "Ignoring exception caught when closing unused ManagedObjectManager", e);
            }
            final String basemsg ="Duplicate Metro monitoring rootname: " + name + " : ";
            if (unique > maxUniqueEndpointRootNameRetries) {
                final String msg = basemsg + "Giving up.";
                logger.log(Level.INFO, msg);
                return ManagedObjectManagerFactory.createNOOP();
            }
            final String msg = basemsg + "Will try to make unique";
            logger.log(Level.CONFIG, msg);
            return createMOMLoop(rootName, ++unique);
        } catch (Throwable t) {
            logger.log(Level.WARNING, "Error while creating monitoring root with name: " + rootName, t);
            return ManagedObjectManagerFactory.createNOOP();
        }
    }

    public static void closeMOM(ManagedObjectManager mom) {
        try {
            final ObjectName name = mom.getObjectName(mom.getRoot());
            // The name is null when the MOM is a NOOP.
            if (name != null) {
                logger.log(Level.INFO, "Closing Metro monitoring root: " + name);
            }
            mom.close();
        } catch (java.io.IOException e) {
            logger.log(Level.WARNING, "Ignoring error when closing Managed Object Manager", e);
        }
    }

    private static Setting clientMonitoring          = Setting.NOT_SET;
    private static Setting endpointMonitoring        = Setting.NOT_SET;
    private static int     typelibDebug                     = -1;
    private static String  registrationDebug                = "NONE";
    private static boolean runtimeDebug                     = false;
    private static int     maxUniqueEndpointRootNameRetries = 100;
    private static final String monitorProperty = "com.sun.xml.ws.monitoring.";

    private static Setting propertyToSetting(String propName) {
        String s = System.getProperty(propName);
        if (s == null) {
            return Setting.NOT_SET;
        }
        s = s.toLowerCase();
        if (s.equals("false") || s.equals("off")) {
            return Setting.OFF;
        } else if (s.equals("true") || s.equals("on")) {
            return Setting.ON;
        }
        return Setting.NOT_SET;
    }

    static {
        try {
            endpointMonitoring = propertyToSetting(monitorProperty + "endpoint");

            clientMonitoring = propertyToSetting(monitorProperty + "client");

            Integer i = Integer.getInteger(monitorProperty + "typelibDebug");
            if (i != null) {
                typelibDebug = i;
            }

            String s = System.getProperty(monitorProperty + "registrationDebug");
            if (s != null) {
                registrationDebug = s.toUpperCase();
            }

            s = System.getProperty(monitorProperty + "runtimeDebug");
            if (s != null && s.toLowerCase().equals("true")) {
                runtimeDebug = true;
            }

            i = Integer.getInteger(monitorProperty + "maxUniqueEndpointRootNameRetries");
            if (i != null) {
                maxUniqueEndpointRootNameRetries = i;
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error while reading monitoring properties", e);
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

class RewritingMOM implements ManagedObjectManager
{
    private final ManagedObjectManager mom;

    private final static String gmbalQuotingCharsRegex = "\n|\\|\"|\\*|\\?|:|=|,";
    private final static String jmxQuotingCharsRegex   = ",|=|:|\"";
    private final static String replacementChar        = "-";

    RewritingMOM(final ManagedObjectManager mom) { this.mom = mom; }

    private String rewrite(final String x) {
        return x.replaceAll(gmbalQuotingCharsRegex, replacementChar);
    }

    // The interface

    public void suspendJMXRegistration() { mom.suspendJMXRegistration(); }
    public void resumeJMXRegistration()  { mom.resumeJMXRegistration(); }
    public GmbalMBean createRoot()       { return mom.createRoot(); }
    public GmbalMBean createRoot(Object root) { return mom.createRoot(root); }
    public GmbalMBean createRoot(Object root, String name) {
        return mom.createRoot(root, rewrite(name));
    }
    public Object getRoot() { return mom.getRoot(); }
    public GmbalMBean register(Object parent, Object obj, String name) {
        return mom.register(parent, obj, rewrite(name)); 
    }
    public GmbalMBean register(Object parent, Object obj) { return mom.register(parent, obj);}
    public GmbalMBean registerAtRoot(Object obj, String name) {
        return mom.registerAtRoot(obj, rewrite(name));
    }
    public GmbalMBean registerAtRoot(Object obj) { return mom.registerAtRoot(obj); }
    public void unregister(Object obj)           { mom.unregister(obj); }
    public ObjectName getObjectName(Object obj)  { return mom.getObjectName(obj); }
    public AMXClient getAMXClient(Object obj)    { return mom.getAMXClient(obj); }
    public Object getObject(ObjectName oname)    { return mom.getObject(oname); }
    public void stripPrefix(String... str)       { mom.stripPrefix(str); }
    public void stripPackagePrefix()             { mom.stripPackagePrefix(); }
    public String getDomain()                    { return mom.getDomain(); }
    public void setMBeanServer(MBeanServer server){mom.setMBeanServer(server); }
    public MBeanServer getMBeanServer()          { return mom.getMBeanServer(); }
    public void setResourceBundle(ResourceBundle rb) { mom.setResourceBundle(rb); }
    public ResourceBundle getResourceBundle()    { return mom.getResourceBundle(); }
    public void addAnnotation(AnnotatedElement element, Annotation annotation) { mom.addAnnotation(element, annotation); }
    public void setRegistrationDebug(RegistrationDebugLevel level) { mom.setRegistrationDebug(level); }
    public void setRuntimeDebug(boolean flag) { mom.setRuntimeDebug(flag); }
    public void setTypelibDebug(int level)    { mom.setTypelibDebug(level); }
    public String dumpSkeleton(Object obj)    { return mom.dumpSkeleton(obj); }
    public void suppressDuplicateRootReport(boolean suppressReport) { mom.suppressDuplicateRootReport(suppressReport); }
    public void close() throws IOException    { mom.close(); }
    public void setJMXRegistrationDebug(boolean x) { mom.setJMXRegistrationDebug(x); }
    public boolean isManagedObject(Object x)  { return mom.isManagedObject(x); }
}

// End of file.
