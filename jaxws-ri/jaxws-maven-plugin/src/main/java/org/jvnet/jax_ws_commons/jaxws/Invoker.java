/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jvnet.jax_ws_commons.jaxws;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author lukas
 */
public final class Invoker {

    public static void main(String... args) throws Exception {
        int idx = 1;
        String c = args[2];
        if ("-pathfile".equals(args[1])) {
            Properties p = new Properties();
            File pathFile = new File(args[2]);
            pathFile.deleteOnExit();
            p.load(new FileInputStream(pathFile));
            c = p.getProperty("cp");
            idx = 3;
        }
        List<URL> cp = new ArrayList<URL>();
        for (String s: c.split(File.pathSeparator)) {
            try {
                URL f = new File(s).toURI().toURL();
                cp.add(f);
            } catch (MalformedURLException ex) {
                Logger.getLogger(Invoker.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        URLClassLoader cl = new URLClassLoader(cp.toArray(new URL[cp.size()]));
        String[] wsargs = new String[args.length - idx];
        System.arraycopy(args, idx, wsargs, 0, args.length - idx);
        ClassLoader orig = Thread.currentThread().getContextClassLoader();
        String origJcp = System.getProperty("java.class.path");
        Thread.currentThread().setContextClassLoader(cl);
        System.setProperty("java.class.path", c);
        try {
            Class<?> compileTool = cl.loadClass(args[0]);
            Constructor<?> ctor = compileTool.getConstructor(OutputStream.class);
            Object tool = ctor.newInstance(System.out);
            Method runMethod = compileTool.getMethod("run", String[].class);
            System.exit((Boolean) runMethod.invoke(tool, new Object[]{wsargs}) ? 0 : 1);
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(Invoker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(Invoker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Invoker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(Invoker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(Invoker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(Invoker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Exception rex = new RuntimeException();
            rex.initCause(ex);
            throw ex;
        } finally {
            Thread.currentThread().setContextClassLoader(orig);
            System.setProperty("java.class.path", origJcp);
        }
    }
}
