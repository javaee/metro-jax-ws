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
 * Transport implementations that work inside the single JVM.
 * Useful for testing.
 *
 * <p>
 * Transports implemented in this package work off the exploded war file
 * image in the file system &mdash; it should have the same file layout
 * that you deploy into, say, Tomcat. They then look for <tt>WEB-INF/sun-jaxws.xml</tt>
 * to determine what services are in the application, and then deploy
 * them in a servlet-like environment.
 *
 * <p>
 * This package comes with two transports. One is the legacy
 * {@link LocalTransportFactory "local" transport}, which effectively
 * deploys a new service instance every time you create a new proxy/dispatch.
 * This is not only waste of computation, but it prevents services of the same
 * application from talking with each other.
 *
 * <p>
 * {@link InVmTransportFactory The "in-vm" transport} is the modern version
 * of the local transport that fixes this problem. You first deploy a new
 * application by using {@link InVmServer},
 * {@link InVmServer#getAddress() obtain its address}, configure the JAX-WS RI
 * with that endpoint, then use that to talk to the running service.
 */
package com.sun.xml.ws.transport.local;
