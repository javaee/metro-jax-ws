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
/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.sun.tools.ws.ant;

import com.sun.tools.ws.processor.modeler.annotation.WebServiceAp;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Javac;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * annotation processing task for use with the JAXWS project.
 */
public class AnnotationProcessingTask extends Javac {

    private boolean procOnly = false;
    private File sourceDestDir;

    private static final Pattern VERSION_PATTERN = Pattern.compile("^\\D+(\\d+(\\.?\\d+)?)$");

    /**
     * Get the sourceDestDir attribute (-s javac parameter)
     * The default value is null.
     *
     * @return directory where to place generated source files.
     */
    public File getSourceDestDir() {
        return sourceDestDir;
    }

    /**
     * Set the sourceDestDir attribute. (-s javac parameter)
     *
     * @param sourceDestDir directory where to place processor generated source files.
     */
    public void setSourceDestDir(File sourceDestDir) {
        this.sourceDestDir = sourceDestDir;
    }

    /**
     * Get the compile option for the ap compiler.
     * If this is true the "-proc:only" argument will be used.
     *
     * @return the value of the compile option.
     */
    public boolean isProcOnly() {
        return procOnly;
    }

    /**
     * Set the compile option for the ap compiler.
     * Default value is false.
     *
     * @param procOnly if true set the compile option.
     */
    public void setProcOnly(boolean procOnly) {
        this.procOnly = procOnly;
    }

    @Override
    protected void checkParameters() throws BuildException {
        super.checkParameters();
        if (sourceDestDir == null) {
            throw new BuildException("destination source directory must be set", getLocation());
        }
        if (!sourceDestDir.isDirectory()) {
            throw new BuildException("destination source directory \"" + sourceDestDir + "\" does not exist or is not a directory",
                    getLocation());
        }
        try {
            Matcher matcher = VERSION_PATTERN.matcher(super.getCompilerVersion());
            if (matcher.find()) {
                float version = Float.valueOf(matcher.group(1));
                if (version < 1.6) {
                    throw new BuildException("Annotation processing task requires Java 1.6+", getLocation());
                }
            }
        } catch (Exception e) {
            log("Can't check version for annotation processing task");
        }
    }

    /**
     * Performs a compile using the Javac externally.
     *
     * @throws BuildException if there is a problem.
     */
    public void execute() throws BuildException {
        ImplementationSpecificArgument argument = super.createCompilerArg();
        argument.setLine("-processor " + WebServiceAp.class.getName());
        argument = super.createCompilerArg();
        argument.setLine("-s " + sourceDestDir);
        if (procOnly) {
            argument = super.createCompilerArg();
            argument.setLine("-proc:only");
        }
        super.execute();
    }
}
