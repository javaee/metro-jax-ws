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
