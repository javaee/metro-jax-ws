/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package com.sun.xml.ws.spi.db;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;


import com.oracle.webservices.api.databinding.DatabindingModeFeature;
import com.sun.xml.ws.db.glassfish.JAXBRIContextFactory;
import com.sun.xml.ws.util.ServiceConfigurationError;
import com.sun.xml.ws.util.ServiceFinder;

/**
 * BindingContextFactory
 * 
 * @author shih-chang.chen@oracle.com
 */
abstract public class BindingContextFactory {
    public static final String DefaultDatabindingMode = DatabindingModeFeature.GLASSFISH_JAXB;
    public static final String JAXB_CONTEXT_FACTORY_PROPERTY = BindingContextFactory.class.getName();
    public static final Logger LOGGER = Logger.getLogger(BindingContextFactory.class.getName());

    // This iterator adds exception checking for proper logging.
    public static Iterator<BindingContextFactory> serviceIterator() {
        final ServiceFinder<BindingContextFactory> sf = ServiceFinder
                .find(BindingContextFactory.class);
        final Iterator<BindingContextFactory> ibcf = sf.iterator();

        return new Iterator<BindingContextFactory>() {
            private BindingContextFactory bcf;

            public boolean hasNext() {
                while (true) {
                    try {
                        if (ibcf.hasNext()) {
                            bcf = ibcf.next();
                            return true;
                        } else
                            return false;
                    } catch (ServiceConfigurationError e) {
                        LOGGER.warning("skipping factory: ServiceConfigurationError: "
                                + e.getMessage());
                    } catch (NoClassDefFoundError ncdfe) {
                        LOGGER.fine("skipping factory: NoClassDefFoundError: "
                                + ncdfe.getMessage());
                    }
                }
            }

            public BindingContextFactory next() {
                if (LOGGER.isLoggable(Level.FINER))
                    LOGGER.finer("SPI found provider: " +
                            bcf.getClass().getName());
                return bcf;
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    static private List<BindingContextFactory> factories() {
        List<BindingContextFactory> factories = new java.util.ArrayList<BindingContextFactory>();
        Iterator<BindingContextFactory> ibcf = serviceIterator();
        while (ibcf.hasNext())
            factories.add(ibcf.next());
        
        // There should always be at least one factory available.
        if (factories.isEmpty()) {
            if (LOGGER.isLoggable(Level.FINER))
                LOGGER.log(Level.FINER, "No SPI providers for BindingContextFactory found, adding: "
                        + JAXBRIContextFactory.class.getName()); 
            factories.add(new JAXBRIContextFactory());
        }
        return factories;
    }

	abstract protected BindingContext newContext(JAXBContext context);
	
	abstract protected BindingContext newContext(BindingInfo bi);
	
	/**
	 * Check to see if the BindingContextFactory is for the databinding mode/flavor. The
	 * String parameter can be the package name of the JAXBContext implementation as well.
	 * @param databinding mode/flavor or the package name of the JAXBContext implementation.
	 * @return
	 */
	abstract protected boolean isFor(String databinding);		

	/**
	 * @deprecated - Does jaxws need this?
	 */
	abstract protected BindingContext getContext(Marshaller m);

    static private BindingContextFactory getFactory(String mode) {
        for (BindingContextFactory f: factories()) {
            if (f.isFor(mode))
                return f;
        }
        return null;
    }

	static public BindingContext create(JAXBContext context) throws DatabindingException { 
		return getJAXBFactory(context).newContext(context);
	}
	
    static public BindingContext create(BindingInfo bi) {
        // Any mode configured in AbstractSEIModelImpl trumps all.
        // System property comes next, then SPI-located.
        String mode = bi.getDatabindingMode();
        if (mode != null) {
            if (LOGGER.isLoggable(Level.FINE))
                LOGGER.log(Level.FINE, "Using SEI-configured databindng mode: "
                        + mode);
        } else if ((mode = System.getProperty("BindingContextFactory")) != null) {
            // The following is left for backward compatibility and should
            // eventually be removed.
            bi.setDatabindingMode(mode);
            if (LOGGER.isLoggable(Level.FINE))
                LOGGER.log(Level.FINE, "Using databindng: " + mode
                        + " based on 'BindingContextFactory' System property");
        } else if ((mode = System.getProperty(JAXB_CONTEXT_FACTORY_PROPERTY)) != null) {
            bi.setDatabindingMode(mode);
            if (LOGGER.isLoggable(Level.FINE))
                LOGGER.log(Level.FINE, "Using databindng: " + mode
                        + " based on '" + JAXB_CONTEXT_FACTORY_PROPERTY
                        + "' System property");
        } else {
            // Find a default provider.  Note we always ensure the list is always non-empty.
            BindingContext factory = getBindingContextFromSpi(factories(), bi);
            if (factory != null) return factory;
            // Should never get here as the list is non-empty.
            LOGGER.log(Level.SEVERE, "No Binding Context Factories found.");
            throw new DatabindingException("No Binding Context Factories found.");
        }
        BindingContextFactory f = getFactory(mode);
        if (f != null)
            return f.newContext(bi);
        LOGGER.severe("Unknown Databinding mode: " + mode);
        throw new DatabindingException("Unknown Databinding mode: " + mode);
    }

    /**
     * Creates JAXB bindingContext with one of the provided factories.
     * To filter appropriate factory {@link BindingContextFactory#isFor(String)} method is used.
     * Currently known 2 appropriate factories: JAXB RI and MOXY.
     * In case no suitable factory is found we are trying to create context with any given factory.
     *
     * @param factories given collection of factories.
     * @param bindingInfo will be used to create bindingContext.
     * @return Created context or null. Null will be returned if we were not able to create context with any given factory.
     */
    private static BindingContext getBindingContextFromSpi(List<BindingContextFactory> factories, BindingInfo bindingInfo) {
        List<BindingContextFactory> fallback = new ArrayList<BindingContextFactory>();
        BindingContext result;
        for (BindingContextFactory factory : factories) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "Found SPI-determined databindng mode: " + factory.getClass().getName());
            }
            if (factory.isFor("org.eclipse.persistence.jaxb") || factory.isFor("com.sun.xml.bind.v2.runtime")) { // filter (JAXB RI || MOXy) implementation
                result = factory.newContext(bindingInfo);
                if (result != null) {
                    return result;
                }
            } else {
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE, "Skipped -> not JAXB.");
                }
                fallback.add(factory);
            }
        }
        for (BindingContextFactory factory : fallback) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "Fallback. Creating from: " + factory.getClass().getName());
            }
            result = getContextOrNullIfError(factory, bindingInfo);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    /**
     * Factory creates new context bases on provided bindingInfo.
     * @param factory given factory.
     * @param bindingInfo to be used to create context.
     * @return Created context or null. Null will be returned if an error happened during the creation process.
     */
    private static BindingContext getContextOrNullIfError(BindingContextFactory factory, BindingInfo bindingInfo) {
        try {
            return factory.newContext(bindingInfo);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);
            return null;
        }
    }

    static public boolean isContextSupported(Object o) {
	    if (o == null) return false;
		String pkgName = o.getClass().getPackage().getName();
		for (BindingContextFactory f: factories()) if (f.isFor(pkgName)) return true;
		return false;
	}
	
	static BindingContextFactory getJAXBFactory(Object o) {
		String pkgName = o.getClass().getPackage().getName();
		BindingContextFactory f = getFactory(pkgName);
		if (f != null) return f;
		throw new DatabindingException("Unknown JAXBContext implementation: " + o.getClass());
		
	}
	
	/**
	 * @deprecated - Does jaxws need this?
	 */
	static public BindingContext getBindingContext(Marshaller m) {
		return getJAXBFactory(m).getContext(m);
	}
}
