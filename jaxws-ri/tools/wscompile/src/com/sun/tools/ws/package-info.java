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

/**
 * <h1>JAX-WS 2.1 Tools</h1>
 * This document describes the tools included with JAX-WS 2.0.1. 
 *
 * {@DotDiagram
     digraph G {
       // external tools
       APT;
 
       // ANT tasks
       node [style=filled,color=lightyellow];
       "WsGen ANT Task"; "WsImport ANT Task"; "Apt ANT Task";

      // commandline
       node [style=filled,color=lightpink];
       wsgen; wsimport;
 
       // libraries
      node [style=filled,color=lightblue];
      WsimportTool; WsgenTool;"WSAP"; WebServiceAP; WSDLModeler;WSDLParser;SeiGenerator;ServiceGenerator;ExceptionGenerator;"JAXB XJC APIs";CodeModel;

       // aps
#       node [style=filled,color=lightpink];
#       "JAX-WS"; tools; runtime; SPI; "Annotation Processor";

       "Apt ANT Task" -> APT;
       "WsGen ANT Task" -> wsgen -> WsgenTool;
       "WsImport ANT Task" -> wsimport -> WsimportTool;
       
       WsgenTool -> APT -> WSAP -> WebServiceAP;
       WsimportTool -> WSDLModeler;
       WSDLModeler->WSDLParser;
       WSDLModeler->"JAXB XJC APIs"
       WsimportTool->SeiGenerator->CodeModel;
       WsimportTool->ServiceGenerator->CodeModel;
       WsimportTool->ExceptionGenerator->CodeModel;
       WebServiceAP->CodeModel
     }
 * }
 * <div align=right>
 * <b>Legend:</b> blue: implementation classes, pink: command-line toosl, white: external tool, yellow: ANT tasks
 * </div>
 *
 * <h2>ANT Tasks</h2>
   <d1>   
 *  <dt>{@link com.sun.tools.ws.ant.Apt Apt}
 *  <dd>An ANT task to invoke <a href="http://java.sun.com/j2se/1.5.0/docs/tooldocs/share/apt.html">APT</a>.

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
 *  <dt><a href="http://java.sun.com/j2se/1.5.0/docs/tooldocs/share/apt.html">APT</a>
     <dd>A Java SE tool and framework for processing annotations. APT will invoke a JAX-WS AnnotationProcossor for 
 *   processing Java source  files with javax.jws.* annotations and making them web services.
 *   APT will compile the Java source files and generate any additional classes needed to make an javax.jws.WebService 
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
 *    <dt>{@link com.sun.tools.ws.processor.modeler.annotation.WebServiceAP WebServiceAP}
 *    <dd>WebServiceAP is a APT AnnotationProcessor for processing javax.jws.* and 
 *    javax.xml.ws.* annotations. This class is used either by the WsGen (CompileTool) tool or 
 *    idirectly via the {@link com.sun.istack.ws.WSAP WSAP} when invoked by APT.
 *
 *    <dt>{@link com.sun.istack.ws.AnnotationProcessorFactoryImpl WSAP}
 *    <dd>This is the entry point for the WebServiceAP when APT is invoked on a SEI
 *    annotated with the javax.jws.WebService annotation.
 *   </d1>
 *
 * @ArchitectureDocument
 **/
package com.sun.tools.ws;
