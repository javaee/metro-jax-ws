/**
 * <h1>JAX-WS 2.0 Tools</h1>
 * This document describes the tools included with JAX-WS 2.0. 
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
      CompileTool; "WSAP"; WebServiceAP; Processor; Modeler; ProcessorActions; 

       // aps
#       node [style=filled,color=lightpink];
#       "JAX-WS"; tools; runtime; SPI; "Annotation Processor";

       "Apt ANT Task" -> APT;
       "WsGen ANT Task" -> wsgen -> CompileTool;
       "WsImport ANT Task" -> wsimport -> CompileTool;
       
       CompileTool -> APT -> WSAP -> WebServiceAP;
       CompileTool -> Processor;
       CompileTool -> Modeler;
       CompileTool -> ProcessorActions;
       CompileTool -> WebServiceAP;
 
       Modeler -> WSDLModeler;
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

 *  <dt>{@link com.sun.tools.ws.ant.WsGen WsGen}
 *  <dd>
 *    An ANT task to invoke {@link com.sun.tools.ws.WsGen WsGen}
 
 *  <dt>{@link com.sun.tools.ws.ant.WsImport WsImport}
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
 
 *  <dt>{@link com.sun.tools.ws.ant.WsImport WsImport}
 *  <dd>
 *    Tool to import a WSDL and to generate an SEI (a javax.jws.WebService) interface that can be either implemented 
 *    on the server to build a web service, or can be used on the client to invoke the web service.
 *  </d1>
 * <h2>Implementation Classes</h2>
 *  <d1>
      <dt>{@link com.sun.tools.ws.wscompile.CompileTool CompileTool}
 *    <dd> This is the main implementation class for both WsGen and WsImport. 
 * 
 * 
 * <dt>{@link com.sun.tools.ws.processor.Processor Processor}
 *    <dd>This abstract class is used to process a particular {@link com.sun.tools.ws.processor.config.Configuration
 *    Configuration} to build a {@link com.sun.tools.ws.processor.model Model} and to run
 *   {@link com.sun.tools.ws.processor.ProcessorAction ProcessorActions} on that model.

 *    <dt>{@link com.sun.tools.ws.processor.model.Model Model}
 *    <dd>The model is used to represent the entire Web Service.  The JAX-WS ProcessorActions can process
 *    this Model to generate Java artifacts such as the service interface.
 *
      <dt>{@link com.sun.tools.ws.processor.ProcessorAction ProcessorActions}
 *    <dd>A ProcessorAction is used to perform some operation on a Model object such as
 *    generating a Java source file.
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
 *    <dt>{@link com.sun.istack.ws.WSAP WSAP}
 *    <dd>This is the entry point for the WebServiceAP when APT is invoked on a SEI
 *    annotated with the javax.jws.WebService annotation.
 *   </d1>
 *
 * @ArchitectureDocument
 **/
package com.sun.tools.ws;
