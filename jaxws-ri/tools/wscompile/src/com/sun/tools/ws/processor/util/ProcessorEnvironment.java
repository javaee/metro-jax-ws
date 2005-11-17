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

package com.sun.tools.ws.processor.util;

import java.net.URLClassLoader;
import java.util.Iterator;

import com.sun.tools.ws.processor.generator.Names;
import com.sun.xml.ws.util.localization.Localizable;

import com.sun.mirror.apt.Filer;

/**
 *
 * @author WS Development Team
 */
public interface ProcessorEnvironment {

    /*
     * Flags
     */
    int F_VERBOSE       = 1 << 0;
    int F_WARNINGS      = 1 << 1;

    /**
     * Set the environment flags
     */
    public void setFlags(int flags);

    /**
     * Get the environment flags
     */
    public int getFlags();

    /**
     * Get the ClassPath.
     */
    public String getClassPath();

    /**
     * Is verbose turned on
     */
    public boolean verbose();

    /**
     * Remember a generated file and its type so that it
     * can be removed later, if appropriate.
     */
    public void addGeneratedFile(GeneratedFileInfo file);

    public Filer getFiler();
    public void setFiler(Filer filer);

    /**
     * Delete all the generated files made during the execution of this
     * environment (those that have been registered with the "addGeneratedFile"
     * method)
     */
    public void deleteGeneratedFiles();

    /**
     * Get a URLClassLoader from using the classpath
     */
    public URLClassLoader getClassLoader();

    public Iterator getGeneratedFiles();

    /**
     * Release resources, if any.
     */
    public void shutdown();

    public void error(Localizable msg);

    public void warn(Localizable msg);

    public void info(Localizable msg);

    public void printStackTrace(Throwable t);

    public Names getNames();

    public int getErrorCount();
    public int getWarningCount();
}
