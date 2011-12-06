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

package com.sun.xml.ws.db;

import java.io.File;
import java.util.Map;

import org.jvnet.ws.databinding.Databinding.WSDLGenerator;

import com.sun.xml.ws.api.databinding.Databinding;
import com.sun.xml.ws.api.databinding.DatabindingConfig;
import com.sun.xml.ws.api.databinding.WSDLGenInfo;
import com.sun.xml.ws.spi.db.DatabindingProvider;

/**
 * DatabindingProviderImpl is the  default JAXWS implementation of DatabindingProvider
 * 
 * @author shih-chang.chen@oracle.com
 */
public class DatabindingProviderImpl implements DatabindingProvider {
    static final private String CachedDatabinding = "com.sun.xml.ws.db.DatabindingProviderImpl";
	Map<String, Object> properties;
	
	public void init(Map<String, Object> p) {
		properties = p;
	}
	
	DatabindingImpl getCachedDatabindingImpl(DatabindingConfig config) {
	    Object object = config.properties().get(CachedDatabinding);
	    return (object != null && object instanceof DatabindingImpl)? (DatabindingImpl)object : null;
	}

	public Databinding create(DatabindingConfig config) {
	    DatabindingImpl impl = getCachedDatabindingImpl(config);
	    if (impl == null) {
	        impl = new DatabindingImpl(this, config);
	        config.properties().put(CachedDatabinding, impl);
	    }
		return impl;
	}

    public WSDLGenerator wsdlGen(DatabindingConfig config) {
        DatabindingImpl impl = (DatabindingImpl)create(config);
        return new JaxwsWsdlGen(impl);
    }

    public boolean isFor(String databindingMode) {
        //This is the default one, so it always return true
        return true;
    }

    static public class JaxwsWsdlGen implements Databinding.WSDLGenerator {
        DatabindingImpl databinding;
        WSDLGenInfo wsdlGenInfo;
        
        JaxwsWsdlGen(DatabindingImpl impl) {
            databinding = impl;
            wsdlGenInfo = new WSDLGenInfo();
        }
        
        public WSDLGenerator inlineSchema(boolean inline) {
            wsdlGenInfo.setInlineSchemas(inline); 
            return this;
        }

        public WSDLGenerator property(String name, Object value) {
            // TODO Auto-generated method stub
            return null;
        }

        public void generate(WSDLResolver wsdlResolver) {
//            wsdlGenInfo.setWsdlResolver(wsdlResolver);
            databinding.generateWSDL(wsdlGenInfo);
        }
        
        public void generate(File outputDir, String name) {
            // TODO Auto-generated method stub
            databinding.generateWSDL(wsdlGenInfo);            
        }        
    }
}
