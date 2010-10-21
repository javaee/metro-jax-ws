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
       "JAXB-RT-API" [label="JAXB runtime API"]; "JAXB XJC API"; StAX; SAAJ;

       // modules
       node [style=filled,color=lightpink];
       "JAX-WS"; Tools; Runtime; SPI; "Annotation Processor";

       "JAX-WS" -> Tools;
       "JAX-WS" -> Runtime;
       "JAX-WS" -> SPI;
       "JAX-WS" -> "Annotation Processor";
       APT -> "Annotation Processor" -> Tools -> APT;
       Tools -> "JAXB XJC API";
       Runtime -> SAAJ;
       Runtime -> StAX
       Runtime -> "JAXB-RT-API";
       SPI -> tools;
       SPI -> Runtime;
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

