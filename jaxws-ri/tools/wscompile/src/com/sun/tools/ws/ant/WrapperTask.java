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

package com.sun.tools.ws.ant;

import com.sun.istack.tools.ProtectedTask;
import com.sun.tools.ws.Invoker;
import com.sun.tools.ws.resources.WscompileMessages;
import com.sun.tools.xjc.api.util.ToolsJarNotFoundException;
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

    private boolean doEndorsedMagic = false;

    /**
     * Set to true to perform the endorsed directory override so that
     * Ant tasks can run on JavaSE 6. 
     */
    public void setXendorsed(boolean f) {
        this.doEndorsedMagic = f;
    }

    protected String getCoreClassName() {
        return getClass().getName()+'2';
    }

    protected ClassLoader createClassLoader() throws ClassNotFoundException, IOException {
        try {
            ClassLoader cl = getClass().getClassLoader();
            if(doEndorsedMagic) {
                return Invoker.createClassLoader(cl);
            } else {
                // check if we are indeed loading JAX-WS 2.2 API
                if(Invoker.checkIfLoading22API())
                    return cl;

                if(Service.class.getClassLoader()==null)
                    throw new BuildException(WscompileMessages.WRAPPER_TASK_NEED_ENDORSED(getTaskName()));
                else {
                    // check if we are indeed loading JAX-WS 2.1 API
                    if(Invoker.checkIfLoading21API())
                       throw new BuildException(WscompileMessages.WRAPPER_TASK_LOADING_21_API(Which.which(Service.class)));
                    else
                        throw new BuildException(WscompileMessages.WRAPPER_TASK_LOADING_20_API(Which.which(Service.class)));
                }
            }
        } catch (ToolsJarNotFoundException e) {
            throw new ClassNotFoundException(e.getMessage(),e);
        }
    }
}
