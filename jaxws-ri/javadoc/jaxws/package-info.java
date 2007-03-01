/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */

/**
 * <h1>JAX-WS RI Architecture Document</h1>.
 *
 * <h2>JAX-WS RI Major Modules and Libraries</h2>
 * {@DotDiagram
     digraph G {
       // tools
       APT; 
       // Products
       node [style=filled,color=lightyellow];
       "Java EE";
 
       // libraries
       node [style=filled,color=lightblue];
       "JAXB-RT-API" [label="JAXB runtime API"]; "JAXB XJC API";  SAAJ; 

       // modules
       node [style=filled,color=lightpink];
       "JAX-WS"; tools; runtime; SPI; "Annotation Processor";

       "JAX-WS" -> tools;
       "JAX-WS" -> runtime;
       "JAX-WS" -> SPI;
       "JAX-WS" -> "Annotation Processor";
       APT -> "Annotation Processor" -> tools -> APT;
       tools -> "JAXB XJC API";
       runtime -> SAAJ;
       runtime -> "JAXB-RT-API";
       SPI -> tools;
       SPI -> runtime;
       "Java EE" -> SPI;
     }
 * }
 * <div align=right>
 * <b>Legend:</b> blue: external library, pink: module, white: external tool, yellow: external products
 * </div>
 * 
 *
 * <h2>Modules</h2>
 * <p>
 * The JAX-WS RI consists of the following major modules.
 *
 * <dl>
 *  <dt>{@link com.sun.xml.ws runtime}
 *  <dd>
 *    runtime module is available at application runtime and provide the actual
 *    core Web Services framework.
 *
 *  <dt>{@link com.sun.tools.ws tools}
 *  <dd>
 *    Tools for converting WSDLs and Java source/class files to Web Services.
 *
 *  <dt><a href="http://java.sun.com/j2se/1.5.0/docs/tooldocs/share/apt.html">APT</a>
 *  <dd>A Java SE tool and framework for processing annotations.
 *
 *  <dt>{@link com.sun.istack.ws Annotation Processor}
 *  <dd>
 *    An APT {@link com.sun.mirror.apt.AnnotationProcessor AnnotationProcessor} for 
 *    processing Java source files with javax.jws.* annotations and making them web services.
 *
 *  <dt>{@link com.sun.xml.ws.spi.runtime Runtime SPI}
 *  <dd>
 *    A part of JAX-WS that defines the contract between the JAX-WS RI runtime and
 *    Java EE.

 *  <dt>{@link com.sun.tools.ws.spi Tools SPI}
 *  <dd>
 *    A part of JAX-WS that defines the contract between the JAX-WS RI tools and
 *    Java EE.
 *
 *  <dt>{@link com.sun.tools.xjc.api JAXB XJC-API}
 *  <dd>
 *    The schema compiler.
 *
 *  <dt>{@link com.sun.xml.bind.api JAXB runtime-API}</a>
 *  <dd>
 *    A part of the JAXB runtime that defines the contract between the JAXB RI and
 *    the JAX-WS RI.
 *
 * </dl>
 *
 * <h2>About This Document</h2>
 * <p>
 * See {@link jaxb.MetaArchitectureDocument} for how to contribute to this document.  Althought this document refers to 
 *      JAXB, JAX-WS is using the same mechanism.
 *
 * @ArchitectureDocument
 */
package jaxws;

import javax.xml.ws.Binding;

