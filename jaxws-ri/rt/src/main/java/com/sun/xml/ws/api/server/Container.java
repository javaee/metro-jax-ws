/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2013 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.api.server;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.Component;
import com.sun.xml.ws.api.ComponentEx;
import com.sun.xml.ws.api.ComponentRegistry;

/**
 * Root of the SPI implemented by the container
 * (such as application server.)
 *
 * <p>
 * Often technologies that are built on top of JAX-WS
 * (such as Tango) needs to negotiate private contracts between
 * them and the container. This interface allows such technologies
 * to query the negotiated SPI by using the {@link #getSPI(Class)}.
 *
 * <p>
 * For example, if a security pipe needs to get some information
 * from a container, they can do the following:
 * <ol>
 *  <li>Negotiate an interface with the container and define it.
 *      (let's call it <tt>ContainerSecuritySPI</tt>.)
 *  <li>The container will implement <tt>ContainerSecuritySPI</tt>.
 *  <li>At the runtime, a security pipe gets
 *      {@link WSEndpoint} and then to {@link Container}.
 *  <li>It calls <tt>container.getSPI(ContainerSecuritySPI.class)</tt>
 *  <li>The container returns an instance of <tt>ContainerSecuritySPI</tt>.
 *  <li>The security pipe talks to the container through this SPI.
 * </ol>
 *
 * <p>
 * This protects JAX-WS from worrying about the details of such contracts,
 * while still providing the necessary service of hooking up those parties.
 *
 * <p>
 * Technologies that run inside JAX-WS server runtime can access this object through
 * {@link WSEndpoint#getContainer()}. In the client runtime, it can be accessed from
 * {@link ContainerResolver#getContainer()}
 *
 * @author Kohsuke Kawaguchi
 * @see WSEndpoint
 */
public abstract class Container implements ComponentRegistry, ComponentEx {
	private final Set<Component> components = new CopyOnWriteArraySet<Component>();
	
    /**
     * For derived classes.
     */
    protected Container() {
    }

    /**
     * Constant that represents a "no {@link Container}",
     * which always returns null from {@link #getSPI(Class)}. 
     */
    public static final Container NONE = new NoneContainer();
    
    private static final class NoneContainer extends Container {
    }
    
    public <S> S getSPI(Class<S> spiType) {
        if (components == null) return null;
    	for (Component c : components) {
    		S s = c.getSPI(spiType);
    		if (s != null)
    			return s;
    	}
        return null;
    }
    
	public Set<Component> getComponents() {
		return components;
	}

	public @NotNull <E> Iterable<E> getIterableSPI(Class<E> spiType) {
    	E item = getSPI(spiType);
    	if (item != null) {
    		Collection<E> c = Collections.singletonList(item);
    		return c;
    	}
    	return Collections.emptySet();
    }
}
