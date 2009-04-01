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

package com.sun.tools.ws;

import com.sun.istack.tools.MaskingClassLoader;
import com.sun.istack.tools.ParallelWorldClassLoader;
import com.sun.tools.ws.resources.WscompileMessages;
import com.sun.tools.xjc.api.util.ToolsJarNotFoundException;
import com.sun.xml.bind.util.Which;

import javax.xml.ws.Service;
import javax.xml.ws.WebServiceFeature;
import javax.xml.namespace.QName;
import java.io.File;
import java.io.OutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Invokes JAX-WS tools in a special class loader that can pick up APT classes,
 * even if it's not available in the tool launcher classpath.
 *
 * @author Kohsuke Kawaguchi
 */
public final class Invoker {
    static int invoke(String mainClass, String[] args) throws Throwable {
        // use the platform default proxy if available.
        // see sun.net.spi.DefaultProxySelector for details.
        if(!noSystemProxies) {
            try {
                System.setProperty("java.net.useSystemProxies","true");
            } catch (SecurityException e) {
                // failing to set this property isn't fatal
            }
        }

        ClassLoader oldcc = Thread.currentThread().getContextClassLoader();
        try {
            ClassLoader cl = Invoker.class.getClassLoader();
            if(Arrays.asList(args).contains("-Xendorsed"))
                cl = createClassLoader(cl); // perform JDK6 workaround hack
            else {
                if(!checkIfLoading21API()) {
                    if(Service.class.getClassLoader()==null)
                        System.err.println(WscompileMessages.INVOKER_NEED_ENDORSED());
                    else
                        System.err.println(WscompileMessages.WRAPPER_TASK_LOADING_20_API(Which.which(Service.class)));
                    return -1;
                }
                //find and load tools.jar
                List<URL> urls = new ArrayList<URL>();
                findToolsJar(cl, urls);

                if(urls.size() > 0){
                    List<String> mask = new ArrayList<String>(Arrays.asList(maskedPackages));

                    // first create a protected area so that we load JAXB/WS 2.1 API
                    // and everything that depends on them inside
                    cl = new MaskingClassLoader(cl,mask);

                    // then this classloader loads the API and tools.jar
                    cl = new URLClassLoader(urls.toArray(new URL[urls.size()]), cl);

                    // finally load the rest of the RI. The actual class files are loaded from ancestors
                    cl = new ParallelWorldClassLoader(cl,"");
                }

            }
            
            Thread.currentThread().setContextClassLoader(cl);

            Class compileTool = cl.loadClass(mainClass);
            Constructor ctor = compileTool.getConstructor(OutputStream.class);
            Object tool = ctor.newInstance(System.out);
            Method runMethod = compileTool.getMethod("run",String[].class);
            boolean r = (Boolean)runMethod.invoke(tool,new Object[]{args});
            return r ? 0 : 1;
        } catch (ToolsJarNotFoundException e) {
            System.err.println(e.getMessage());
        } catch (InvocationTargetException e) {
            throw e.getCause();
        } catch(ClassNotFoundException e){
            throw e;
        }finally {
            Thread.currentThread().setContextClassLoader(oldcc);
        }

        return -1;
    }

    /**
     * Returns true if the RI appears to be loading the JAX-WS 2.1 API.
     */
    public static boolean checkIfLoading21API() {
        try {
            Service.class.getMethod("getPort",Class.class, WebServiceFeature[].class);
            // yup. things look good.
            return true;
        } catch (NoSuchMethodException e) {
        } catch (LinkageError e) {
        }
        // nope
        return false;
    }

    /**
    * Returns true if the RI appears to be loading the JAX-WS 2.2 API.
    */
   public static boolean checkIfLoading22API() {
       try {
           Service.class.getMethod("create",java.net.URL.class, QName.class, WebServiceFeature[].class);
           // yup. things look good.
           return true;
       } catch (NoSuchMethodException e) {
       } catch (LinkageError e) {
       }
       // nope
       return false;
   }


    /**
     * Creates a classloader that can load JAXB/WS 2.1 API and tools.jar,
     * and then return a classloader that can RI classes, which can see all those APIs and tools.jar.  
     */
    public static ClassLoader createClassLoader(ClassLoader cl) throws ClassNotFoundException, IOException, ToolsJarNotFoundException {

        URL[] urls = findIstack22APIs(cl);
        if(urls.length==0)
            return cl;  // we seem to be able to load everything already. no need for the hack

        List<String> mask = new ArrayList<String>(Arrays.asList(maskedPackages));
        if(urls.length>1) {
            // we need to load 2.1 API from side. so add them to the mask
            mask.add("javax.xml.bind.");
            mask.add("javax.xml.ws.");
        }

        // first create a protected area so that we load JAXB/WS 2.1 API
        // and everything that depends on them inside
        cl = new MaskingClassLoader(cl,mask);

        // then this classloader loads the API and tools.jar
        cl = new URLClassLoader(urls, cl);

        // finally load the rest of the RI. The actual class files are loaded from ancestors
        cl = new ParallelWorldClassLoader(cl,"");

        return cl;
    }

    /**
     * Creates a classloader for loading JAXB/WS 2.1 jar and tools.jar
     */
    private static URL[] findIstack21APIs(ClassLoader cl) throws ClassNotFoundException, MalformedURLException, ToolsJarNotFoundException {
        List<URL> urls = new ArrayList<URL>();

        if(Service.class.getClassLoader()==null) {
            // JAX-WS API is loaded from bootstrap classloader
            URL res = cl.getResource("javax/xml/ws/EndpointReference.class");
            if(res==null)
                throw new ClassNotFoundException("There's no JAX-WS 2.1 API in the classpath");
            urls.add(ParallelWorldClassLoader.toJarUrl(res));

            res = cl.getResource("javax/xml/bind/annotation/XmlSeeAlso.class");
            if(res==null)
                throw new ClassNotFoundException("There's no JAXB 2.1 API in the classpath");
            urls.add(ParallelWorldClassLoader.toJarUrl(res));
        }

        findToolsJar(cl, urls);

        return urls.toArray(new URL[urls.size()]);
    }
    /**
     * Creates a classloader for loading JAXB/WS 2.2 jar and tools.jar
     */
    private static URL[] findIstack22APIs(ClassLoader cl) throws ClassNotFoundException, IOException, ToolsJarNotFoundException {
        List<URL> urls = new ArrayList<URL>();

        if(Service.class.getClassLoader()==null) {
            // JAX-WS API is loaded from bootstrap classloader
            URL res = cl.getResource("javax/xml/ws/EndpointContext.class");
            if(res==null)
                throw new ClassNotFoundException("There's no JAX-WS 2.2 API in the classpath");
            urls.add(ParallelWorldClassLoader.toJarUrl(res));
            //TODO do we need to laod JAXB 2.2 API? if so look for JAXBPermission added in 2.2
            res = cl.getResource("javax/xml/bind/annotation/XmlSeeAlso.class");
            if(res==null)
                throw new ClassNotFoundException("There's no JAXB 2.1 API in the classpath");
            urls.add(ParallelWorldClassLoader.toJarUrl(res));
        }

        findToolsJar(cl, urls);

        return urls.toArray(new URL[urls.size()]);
    }

    private static void findToolsJar(ClassLoader cl, List<URL> urls) throws ToolsJarNotFoundException, MalformedURLException {
        try {
            Class.forName("com.sun.tools.javac.Main",false,cl);
            Class.forName("com.sun.tools.apt.Main",false,cl);
            // we can already load them in the parent class loader.
            // so no need to look for tools.jar.
            // this happens when we are run inside IDE/Ant, or
            // in Mac OS.
        } catch (ClassNotFoundException e) {
            // otherwise try to find tools.jar
            File jreHome = new File(System.getProperty("java.home"));
            File toolsJar = new File( jreHome.getParent(), "lib/tools.jar" );

            if (!toolsJar.exists()) {
                throw new ToolsJarNotFoundException(toolsJar);
            }
            urls.add(toolsJar.toURL());
        }
    }

    /**
     * The list of package prefixes we want the
     * {@link MaskingClassLoader} to prevent the parent
     * classLoader from loading
     */
    public static String[] maskedPackages = new String[]{
        "com.sun.istack.tools.",
        "com.sun.tools.jxc.",
        "com.sun.tools.xjc.",
        "com.sun.tools.ws.",
        "com.sun.codemodel.",
        "com.sun.relaxng.",
        "com.sun.xml.xsom.",
        "com.sun.xml.bind.",
        "com.sun.xml.ws."
    };

    /**
     * Escape hatch to work around IBM JDK problem.
     * See http://www-128.ibm.com/developerworks/forums/dw_thread.jsp?nav=false&forum=367&thread=164718&cat=10
     */
    public static boolean noSystemProxies = false;

    static {
        try {
            noSystemProxies = Boolean.getBoolean(Invoker.class.getName()+".noSystemProxies");
        } catch(SecurityException e) {
            // ignore
        }
    }
}
