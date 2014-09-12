/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2014 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.istack.tools.ProtectedTask;
import com.sun.tools.ws.Invoker;
import com.sun.tools.ws.wscompile.Options;
import com.sun.tools.ws.resources.WscompileMessages;
import com.sun.xml.bind.util.Which;
import org.apache.tools.ant.BuildException;

import javax.xml.ws.Service;
import java.io.IOException;

/**
 * Wrapper task to launch real implementations of the task in a classloader that can work
 * even in JavaSE 6.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class WrapperTask extends ProtectedTask {

    /**
     * Set to true to perform the endorsed directory override so that
     * Ant tasks can run on JavaSE 6.
     */
    private boolean doEndorsedMagic = false;

    protected String getCoreClassName() {
        return getClass().getName()+'2';
    }

    private String targetVersionAttribute;


    @Override
    public void setDynamicAttribute(String name, String value) throws BuildException {
        super.setDynamicAttribute(name,value);
        if(name.equals("target"))
            targetVersionAttribute = value;
        else if(name.equals("xendorsed"))
            this.doEndorsedMagic = Boolean.valueOf(value);

    }

    protected ClassLoader createClassLoader() throws ClassNotFoundException, IOException {
        ClassLoader cl = getClass().getClassLoader();
        if (doEndorsedMagic) {
            return Invoker.createClassLoader(cl);
        } else {
            Options.Target targetVersion;
            if (targetVersionAttribute != null) {
                targetVersion = Options.Target.parse(targetVersionAttribute);
            } else {
                targetVersion = Options.Target.getDefault();
            }
            Options.Target loadedVersion = Options.Target.getLoadedAPIVersion();
            //Check if the target version is supported by the loaded API version
            if (loadedVersion.isLaterThan(targetVersion)) {
                return cl;
            } else {
                if (Service.class.getClassLoader() == null)
                    throw new BuildException(WscompileMessages.WRAPPER_TASK_NEED_ENDORSED(loadedVersion.getVersion(), targetVersion.getVersion(), getTaskName()));
                else {
                    throw new BuildException(WscompileMessages.WRAPPER_TASK_LOADING_INCORRECT_API(loadedVersion.getVersion(), Which.which(Service.class), targetVersion.getVersion()));
                }
            }
        }
    }
}
