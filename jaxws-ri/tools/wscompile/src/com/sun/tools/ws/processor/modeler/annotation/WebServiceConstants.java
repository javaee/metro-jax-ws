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
package com.sun.tools.ws.processor.modeler.annotation;


/**
 *
 * @author  dkohlert
 */
public interface WebServiceConstants { //extends RmiConstants {

    public static final String RETURN                       = "return";
    public static final String RETURN_CAPPED                = "Return";
    public static final String RETURN_VALUE                 = "_return";
    public static final String SERVICE                      = "Service";
    public static final String PD                           = ".";
    public static final String JAXWS                        = "jaxws";
    public static final String JAXWS_PACKAGE_PD             = JAXWS+PD;
    public static final String PD_JAXWS_PACKAGE_PD          = PD+JAXWS+PD;
    public static final String BEAN                         = "Bean";
    public static final String GET_PREFIX                   = "get";
    public static final String IS_PREFIX                    = "is";
    public static final String FAULT_INFO                   = "faultInfo";
    public static final String GET_FAULT_INFO               = "getFaultInfo";
    public static final String HTTP_PREFIX                  = "http://";
    public static final String JAVA_LANG_OBJECT             = "java.lang.Object";
    public static final String EMTPY_NAMESPACE_ID           = "";
    

    public static final char SIGC_INNERCLASS  = '$';
    public static final char SIGC_UNDERSCORE  = '_';
    
    public static final String DOT = ".";    
    public static final String PORT = "WSDLPort";
    public static final String BINDING = "Binding";
    public static final String RESPONSE = "Response";
    
    /*
     * Identifiers potentially useful for all Generators
     */
    public static final String EXCEPTION_CLASSNAME =
        java.lang.Exception.class.getName();
    public static final String REMOTE_CLASSNAME =
        java.rmi.Remote.class.getName();
    public static final String REMOTE_EXCEPTION_CLASSNAME =
        java.rmi.RemoteException.class.getName();
    public static final String RUNTIME_EXCEPTION_CLASSNAME =
        java.lang.RuntimeException.class.getName();
    public static final String SERIALIZABLE_CLASSNAME =
        java.io.Serializable.class.getName();
    public static final String HOLDER_CLASSNAME =
        javax.xml.ws.Holder.class.getName();
    public static final String COLLECTION_CLASSNAME =
        java.util.Collection.class.getName();    
    public static final String MAP_CLASSNAME =
        java.util.Map.class.getName();    
    

    // 181 constants
    public static final String WEBSERVICE_NAMESPACE         = "http://www.bea.com/xml/ns/jws";
    public static final String HANDLER_CONFIG               = "handler-config";
    public static final String HANDLER_CHAIN                = "handler-chain";
    public static final String HANDLER_CHAIN_NAME           = "handler-chain-name";
    public static final String HANDLER                      = "handler";
    public static final String HANDLER_NAME                 = "handler-name";
    public static final String HANDLER_CLASS                = "handler-class";
    public static final String INIT_PARAM                   = "init-param";
    public static final String SOAP_ROLE                    = "soap-role";
    public static final String SOAP_HEADER                  = "soap-header";
    public static final String PARAM_NAME                   = "param-name";
    public static final String PARAM_VALUE                  = "param-value";
}
