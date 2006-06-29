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
 *
 * <P>This document describes the {@link com.sun.mirror.apt.AnnotationProcessor AnnotationProcessor}
 * included with JAX-WS 2.0.
 *
 * <p>The {@link com.sun.istack.ws.AnnotationProcessorFactoryImpl AnnoatationnProcessorFactoryImpl} class
 * tells the <a href="http://java.sun.com/j2se/1.5.0/docs/tooldocs/share/apt.html">APT</a>
 * framework that there exists an {@com.sun.mirror.apt.AnnotationProcessor AnnotationProcessor} 
 * ({@link com.sun.istak.ws.WSAP WSAP}) for for processing javax.jws.*, javax.jws.soap.*,
 *  and javax.xml.ws.* annotations.
 *
 * @ArchitectureDocument
*/
package com.sun.istack.ws;
