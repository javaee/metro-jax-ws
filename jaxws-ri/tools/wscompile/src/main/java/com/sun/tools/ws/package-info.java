/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2015 Oracle and/or its affiliates. All rights reserved.
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

/**
 * <h1>JAX-WS 2.1 Tools</h1>
 * This document describes the tools included with JAX-WS 2.0.1. 
 *
 * <h2>ANT Tasks</h2>
   <d1>
 *  <dt>{@link com.sun.tools.ws.ant.AnnotationProcessingTask AnnotationProcessing}
 *  <dd>An ANT task to invoke <a href="http://download.oracle.com/javase/6/docs/api/javax/annotation/processing/package-summary.html">Annotation Processing</a>.

 *  <dt>{@link com.sun.tools.ws.ant.WsGen2 WsGen}
 *  <dd>
 *    An ANT task to invoke {@link com.sun.tools.ws.WsGen WsGen}

 *  <dt>{@link com.sun.tools.ws.ant.WsImport2 WsImport}
 *  <dd>
 *    An ANT task to invoke {@link com.sun.tools.ws.WsImport WsImport}
 *
 *  </d1>       
 * <h2>Command-line Tools</h2>
   <d1>
 *  <dt><a href="http://download.oracle.com/javase/6/docs/api/javax/annotation/processing/package-summary.html">AP</a>
 <dd>A Java SE tool and framework for processing annotations. Annotation processing will invoke a JAX-WS AnnotationProcossor for
 *   processing Java source  files with javax.jws.* annotations and making them web services.
 *   Annotation processing will compile the Java source files and generate any additional classes needed to make an javax.jws.WebService
 *   annotated class a Web service.  
 *
 *  <dt>{@link com.sun.tools.ws.WsGen WsGen}
 *  <dd>Tool to process a compiled javax.jws.WebService annotated class and to generate the necessary classes to make 
 *  it a Web service.  

 *  <dt>{@link com.sun.tools.ws.ant.WsImport2 WsImport}
 *  <dd>
 *    Tool to import a WSDL and to generate an SEI (a javax.jws.WebService) interface that can be either implemented 
 *    on the server to build a web service, or can be used on the client to invoke the web service.
 *  </d1>
 * <h2>Implementation Classes</h2>
 *  <d1>
 *    <dt>{@link com.sun.tools.ws.processor.model.Model Model}
 *    <dd>The model is used to represent the entire Web Service.  The JAX-WS ProcessorActions can process
 *    this Model to generate Java artifacts such as the service interface.
 *
 *
 *    <dt>{@link com.sun.tools.ws.processor.modeler.Modeler Modeler}
 *    <dd>A Modeler is used to create a Model of a Web Service from a particular Web 
 *    Web Service description such as a WSDL
 *    file.
 *
 *    <dt>{@link com.sun.tools.ws.processor.modeler.wsdl.WSDLModeler WSDLModeler}
 *    <dd>The WSDLModeler processes a WSDL to create a Model.
 *
 *    <dt>{@link com.sun.tools.ws.processor.modeler.annotation.WebServiceAp WebServiceAp}
 *    <dd>WebServiceAp is a AnnotationProcessor for processing javax.jws.* and
 *    javax.xml.ws.* annotations. This class is used either by the WsGen (CompileTool) tool or 
 *    idirectly via the {@link com.sun.istack.ws.WSAP WSAP} when invoked by Annotation Processing.
 *   </d1>
 *
 * @ArchitectureDocument
 **/
package com.sun.tools.ws;
