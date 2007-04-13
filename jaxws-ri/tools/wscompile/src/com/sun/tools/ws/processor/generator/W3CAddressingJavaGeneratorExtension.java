/*
 The contents of this file are subject to the terms
 of the Common Development and Distribution License
 (the "License").  You may not use this file except
 in compliance with the License.
 
 You can obtain a copy of the license at
 https://jwsdp.dev.java.net/CDDLv1.0.html
 See the License for the specific language governing
 permissions and limitations under the License.
 
 When distributing Covered Code, include this CDDL
 HEADER in each file and include the License file at
 https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 add the following below this CDDL HEADER, with the
 fields enclosed by brackets "[]" replaced with your
 own identifying information: Portions Copyright [yyyy]
 [name of copyright owner]
*/
/*
 $Id: W3CAddressingJavaGeneratorExtension.java,v 1.3 2007-04-13 00:32:41 jitu Exp $

 Copyright (c) 2006 Sun Microsystems, Inc.
 All rights reserved.
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
