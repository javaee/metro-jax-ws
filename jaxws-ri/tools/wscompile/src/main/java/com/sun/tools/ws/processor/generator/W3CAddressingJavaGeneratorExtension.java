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

package com.sun.tools.ws.processor.generator;

import com.sun.codemodel.JAnnotationArrayMember;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JMethod;
import com.sun.tools.ws.api.TJavaGeneratorExtension;
import com.sun.tools.ws.api.wsdl.TWSDLOperation;
import com.sun.tools.ws.wsdl.document.Fault;
import com.sun.tools.ws.wsdl.document.Operation;

import javax.xml.ws.Action;
import javax.xml.ws.FaultAction;
import java.util.Map;

/**
 * This Java Generator extension generates @Action annotation on web methods if an explicit wsam:Action value is specified
 * in the wsdl definitions.
 *
 * @author Arun Gupta
 */
public class W3CAddressingJavaGeneratorExtension extends TJavaGeneratorExtension {
    @Override
    public void writeMethodAnnotations(TWSDLOperation two, JMethod jMethod) {
        JAnnotationUse actionAnn = null;

        if (!(two instanceof Operation))
            return;

        Operation o = ((Operation)two);

        // explicit input action
        if (o.getInput().getAction() != null && !o.getInput().getAction().equals("")) {
            // explicitly specified
            actionAnn = jMethod.annotate(Action.class);
            actionAnn.param("input", o.getInput().getAction());
        }

        // explicit output action
        if (o.getOutput() != null && o.getOutput().getAction() != null && !o.getOutput().getAction().equals("")) {
            // explicitly specified
            if (actionAnn == null)
                actionAnn = jMethod.annotate(Action.class);

            actionAnn.param("output", o.getOutput().getAction());
        }

        // explicit fault action
        if (o.getFaults() != null && o.getFaults().size() > 0) {
            Map<String, JClass> map = o.getFaults();
            JAnnotationArrayMember jam = null;

            for (Fault f : o.faults()) {
                if (f.getAction() == null)
                    continue;

                if (f.getAction().equals(""))
                    continue;

                if (actionAnn == null) {
                    actionAnn = jMethod.annotate(Action.class);
                }
                if (jam == null) {
                    jam = actionAnn.paramArray("fault");
                }
                final JAnnotationUse faAnn = jam.annotate(FaultAction.class);
                faAnn.param("className", map.get(f.getName()));
                faAnn.param("value", f.getAction());
            }
        }
    }
}
