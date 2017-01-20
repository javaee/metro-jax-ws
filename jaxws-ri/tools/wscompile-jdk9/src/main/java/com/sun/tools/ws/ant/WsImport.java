/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
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

package com.sun.tools.ws.ant;

import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;

/**
 * Wrapper task to launch {@link WsImport2}.
 *
 * @author Kohsuke Kawaguchi
 */
public class WsImport extends WsImportBase {

    @Override
    protected void setupForkCommand(String className) {
        ClassLoader loader = this.getClass().getClassLoader();
        while (loader != null && !(loader instanceof AntClassLoader)) {
            loader = loader.getParent();
        }

        String antcp = loader != null
                //taskedef cp
                ? ((AntClassLoader) loader).getClasspath()
                //system classloader, ie. env CLASSPATH=...
                : System.getProperty("java.class.path");

        getCommandline().createClasspath(getProject()).append(new Path(getProject(), antcp));

        boolean addModules = true;
        String[] args = getJavacargs().getArguments();
        for (int i = 0; i < args.length; i++) {
            if ("-source".equals(args[i]) && 9 >= getVersion(args[i++])) {
                addModules = false;
                break;
            }
            if ("-target".equals(args[i]) && 9 >= getVersion(args[i++])) {
                addModules = false;
                break;
            }
            if ("-release".equals(args[i]) && 9 >= getVersion(args[i++])) {
                addModules = false;
                break;
            }
        }
        if (addModules) {
            getCommandline().createVmArgument().setLine("--add-modules java.xml.ws");
        }
        getCommandline().setClassname(className);
    }

    @Override
    public void setXendorsed(boolean xendorsed) {
        log("xendorsed attribute not supported", Project.MSG_WARN);
        //no-op
    }

    @Override
    public void execute() throws BuildException {
        super.execute();
    }

    private float getVersion(String s) {
        return Float.parseFloat(s);
    }

}
