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

package com.sun.tools.ws.processor.model;

/**
 *
 * @author WS Development Team
 */
public interface ModelProperties {

    //to set WSDL_MODELER_NAME from inside WSDLModeler
    public static final String WSDL_MODELER_NAME =
        "com.sun.xml.ws.processor.modeler.wsdl.WSDLModeler";
    public static final String PROPERTY_PARAM_MESSAGE_PART_NAME =
        "com.sun.xml.ws.processor.model.ParamMessagePartName";
    public static final String PROPERTY_ANONYMOUS_TYPE_NAME =
        "com.sun.xml.ws.processor.model.AnonymousTypeName";
    public static final String PROPERTY_ANONYMOUS_ARRAY_TYPE_NAME =
        "com.sun.xml.ws.processor.model.AnonymousArrayTypeName";
    public static final String PROPERTY_ANONYMOUS_ARRAY_JAVA_TYPE =
        "com.sun.xml.ws.processor.model.AnonymousArrayJavaType";

    public static final String PROPERTY_PTIE_CLASS_NAME =
        "com.sun.xml.ws.processor.model.PtieClassName";
    public static final String PROPERTY_EPTFF_CLASS_NAME =
        "com.sun.xml.ws.processor.model.EPTFFClassName";
    public static final String PROPERTY_SED_CLASS_NAME =
        "com.sun.xml.ws.processor.model.SEDClassName";
        public static final String PROPERTY_WSDL_PORT_NAME =
        "com.sun.xml.ws.processor.model.WSDLPortName";
    public static final String PROPERTY_WSDL_PORT_TYPE_NAME =
        "com.sun.xml.ws.processor.model.WSDLPortTypeName";
    public static final String PROPERTY_WSDL_BINDING_NAME =
        "com.sun.xml.ws.processor.model.WSDLBindingName";
    public static final String PROPERTY_WSDL_MESSAGE_NAME =
        "com.sun.xml.ws.processor.model.WSDLMessageName";
    public static final String PROPERTY_MODELER_NAME =
        "com.sun.xml.ws.processor.model.ModelerName";
    public static final String PROPERTY_STUB_CLASS_NAME =
        "com.sun.xml.ws.processor.model.StubClassName";
    public static final String PROPERTY_STUB_OLD_CLASS_NAME =
        "com.sun.xml.ws.processor.model.StubOldClassName";
    public static final String PROPERTY_DELEGATE_CLASS_NAME =
        "com.sun.xml.ws.processor.model.DelegateClassName";
    public static final String PROPERTY_CLIENT_ENCODER_DECODER_CLASS_NAME =
        "com.sun.xml.ws.processor.model.ClientEncoderClassName";
    public static final String PROPERTY_CLIENT_CONTACTINFOLIST_CLASS_NAME =
        "com.sun.xml.ws.processor.model.ClientContactInfoListClassName";
    public static final String PROPERTY_TIE_CLASS_NAME =
        "com.sun.xml.ws.processor.model.TieClassName";
    public static final String PROPERTY_JAVA_PORT_NAME =
        "com.sun.xml.ws.processor.model.JavaPortName";
}
