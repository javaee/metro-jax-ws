/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.spi.db;

import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import com.sun.xml.ws.api.databinding.DatabindingModeFeature;
import com.sun.xml.ws.util.ServiceFinder;

/**
 * BindingContextFactory
 * 
 * @author shih-chang.chen@oracle.com
 */
abstract public class BindingContextFactory {
//	static final String GlassfishJAXB   = "glassfish.jaxb";
//	static final String EclipseLinkJAXB = "eclipselink.jaxb";
	static final public String DefaultDatabindingMode = DatabindingModeFeature.GLASSFISH_JAXB;
	//TODO the factoryImpls should be loaded from a config file/object:
//	List<BindingContextFactory> factoryImpls;
	static private List<BindingContextFactory> factories() {
	    List<BindingContextFactory> factories = new java.util.ArrayList<BindingContextFactory>();
        for (BindingContextFactory bcf : ServiceFinder.find(BindingContextFactory.class)) {
            factories.add(bcf);
        }
        return factories;
//		factoryImpls.put(GlassfishJAXB,   "com.sun.xml.ws.db.glassfish.JAXBRIContextFactory");
//		factoryImpls.put(EclipseLinkJAXB, "com.sun.xml.ws.db.toplink.JAXBContextFactory");
	}
	
//	/**
//	 * Maps the JAXBContext impl package name to databindingMode string.
//	 */
//	static Map<String, String> jaxbImpls;
//	static {
//		jaxbImpls = new java.util.HashMap<String, String>();
//		jaxbImpls.put("com.sun.xml.bind.v2.runtime", GlassfishJAXB);
//		jaxbImpls.put("org.eclipse.persistence.jaxb", EclipseLinkJAXB);
//	}

	abstract protected BindingContext newContext(JAXBContext context);
	
	abstract protected BindingContext newContext(BindingInfo bi);
	
	/**
	 * Check to see if the BindingContextFactory is for the databinding mode/flavor. The
	 * String parameter can be the package name of the JAXBContext implementation as well.
	 * @param databinding mode/flavor or the package name of the JAXBContext implementation.
	 * @return
	 */
	abstract protected boolean isFor(String str);		

	/**
	 * @deprecated - Does jaxws need this?
	 */
	abstract protected BindingContext getContext(Marshaller m);
	
	
	@SuppressWarnings("unchecked")
	static private BindingContextFactory getFactory(String mode) {
		for (BindingContextFactory f: factories()) if (f.isFor(mode)) return f;
		return null;
	}

	static public BindingContext create(JAXBContext context) throws DatabindingException { 
		return getJAXBFactory(context).newContext(context);
	}
	
	static public BindingContext create(BindingInfo bi) {
		String mode = bi.getDatabindingMode();
		if (mode == null) mode = System.getProperty("BindingContextFactory");
		if (mode == null) mode = DefaultDatabindingMode;
		BindingContextFactory f = getFactory(mode);
		if (f != null) return f.newContext(bi);
		throw new DatabindingException("Unknown Databinding mode: " + mode);
	}
	
	static public boolean isContextSupported(Object o) {
	    if (o == null) return false;
		String pkgName = o.getClass().getPackage().getName();
//		if (factoryImpls == null) init();
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

    /**
     * Creates a new {@link BindingContext}.
     *
     * <p>
     * {@link JAXBContext#newInstance(Class[]) JAXBContext.newInstance()} methods may
     * return other JAXB providers that are not compatible with the JAX-RPC RI.
     * This method guarantees that the JAX-WS RI will finds the JAXB RI.
     *
     * @param classes
     *      Classes to be bound. See {@link JAXBContext#newInstance(Class[])} for the meaning.
     * @param typeRefs
     *      See {@link #TYPE_REFERENCES} for the meaning of this parameter.
     *      Can be null.
     * @param subclassReplacements
     *      See {@link #SUBCLASS_REPLACEMENTS} for the meaning of this parameter.
     *      Can be null.
     * @param defaultNamespaceRemap
     *      See {@link #DEFAULT_NAMESPACE_REMAP} for the meaning of this parameter.
     *      Can be null (and should be null for ordinary use of JAXB.)
     * @param c14nSupport
     *      See {@link #CANONICALIZATION_SUPPORT} for the meaning of this parameter.
     * @param ar
     *      See {@link #ANNOTATION_READER} for the meaning of this parameter.
     *      Can be null.
     * @since JAXB 2.1 EA2
     */
//    public static BindingContext newInstance(@NotNull Class[] classes, 
//       @Nullable Collection<TypeInfo> typeRefs, 
//       @Nullable Map<Class,Class> subclassReplacements, 
//       @Nullable String defaultNamespaceRemap, boolean c14nSupport, 
//       @Nullable RuntimeAnnotationReader ar) throws JAXBException {
//        return ContextFactory.createContext(classes, typeRefs, subclassReplacements, 
//                defaultNamespaceRemap, c14nSupport, ar, false, false, false);
//    }
//
//    /**
//     * @deprecated
//     *      Compatibility with older versions.
//     */
//    public static BindingContext newInstance(@NotNull Class[] classes,
//        @Nullable Collection<TypeInfo> typeRefs,
//        @Nullable String defaultNamespaceRemap, boolean c14nSupport ) throws JAXBException {
//        return newInstance(classes,typeRefs, Collections.<Class,Class>emptyMap(), 
//                defaultNamespaceRemap,c14nSupport,null);
//    }
}
