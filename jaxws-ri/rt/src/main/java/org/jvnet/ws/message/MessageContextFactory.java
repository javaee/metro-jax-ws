/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2012 Oracle and/or its affiliates. All rights reserved.
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

package org.jvnet.ws.message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import com.sun.istack.Nullable;
import com.sun.xml.ws.api.SOAPVersion; // TODO leaking RI APIs
import com.sun.xml.ws.encoding.ContentTypeImpl;

import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.ws.WebServiceFeature;

import org.jvnet.ws.EnvelopeStyle;
import org.jvnet.ws.EnvelopeStyle.Style;

@Deprecated
public abstract class MessageContextFactory extends com.oracle.webservices.api.message.MessageContextFactory
{   
    protected abstract MessageContextFactory newFactory(WebServiceFeature ... f);
    
    public abstract MessageContext createContext();

    public abstract MessageContext createContext(SOAPMessage m);
    
    public abstract MessageContext createContext(Source m);
    
    public abstract MessageContext createContext(Source m, EnvelopeStyle.Style envelopeStyle);
    
    public abstract MessageContext createContext(InputStream in, String contentType) throws IOException;

    /**
     * @deprecated http://java.net/jira/browse/JAX_WS-1077
     */
    @Deprecated 
    public abstract MessageContext createContext(InputStream in, MimeHeaders headers) throws IOException;
    
    static public MessageContextFactory createFactory(WebServiceFeature ... f) {
        return createFactory(null, f);
    }
    
    static public MessageContextFactory createFactory(ClassLoader cl, WebServiceFeature ...f) {
        return wrap(
        		com.oracle.webservices.api.message.MessageContextFactory.createFactory(cl, f));
    }  
    
    private static final class MCWrapper implements MessageContext {
    	private com.oracle.webservices.api.message.MessageContext mc;
    	
    	public MCWrapper(com.oracle.webservices.api.message.MessageContext mc) {
    		this.mc = mc;
    	}

		@Override
		public void addSatellite(Class keyClass, com.oracle.webservices.api.message.PropertySet satellite) {
			mc.addSatellite(keyClass, satellite);
		}

		@Override
		public @Nullable
		<T extends com.oracle.webservices.api.message.PropertySet> T getSatellite(Class<T> satelliteClass) {
			return mc.getSatellite(satelliteClass);
		}
		
	    public Map<Class<? extends com.oracle.webservices.api.message.PropertySet>, com.oracle.webservices.api.message.PropertySet> getSatellites() {
	        return mc.getSatellites();
	    }


		@Override
		public void addSatellite(com.oracle.webservices.api.message.PropertySet satellite) {
			mc.addSatellite(satellite);
		}

		@Override
		public void removeSatellite(com.oracle.webservices.api.message.PropertySet satellite) {
			mc.removeSatellite(satellite);
		}

		@Override
		public void copySatelliteInto(
				com.oracle.webservices.api.message.MessageContext r) {
			mc.copySatelliteInto(r);
		}

		@Override
		public boolean containsKey(Object key) {
			return mc.containsKey(key);
		}

		@Override
		public Object get(Object key) {
			return mc.get(key);
		}

		@Override
		public Object put(String key, Object value) {
			return mc.put(key, value);
		}

		@Override
		public boolean supports(Object key) {
			return mc.supports(key);
		}

		@Override
		public Object remove(Object key) {
			return mc.remove(key);
		}

		@Override
		@Deprecated
		public Map<String, Object> createMapView() {
			return mc.createMapView();
		}

		@Override
		public Map<String, Object> asMap() {
			return mc.asMap();
		}

		@Override
		public SOAPMessage getAsSOAPMessage() throws SOAPException {
			return mc.getAsSOAPMessage();
		}

		@Override
		public SOAPMessage getSOAPMessage() throws SOAPException {
			return mc.getSOAPMessage();
		}

		@Override
		public void addSatellite(PropertySet satellite) {
			mc.addSatellite(satellite);
		}

		@Override
		public void removeSatellite(PropertySet satellite) {
			mc.removeSatellite(satellite);
		}

		@Override
		public void copySatelliteInto(MessageContext otherMessageContext) {
			mc.copySatelliteInto(otherMessageContext);
		}

		@Override
		public void addSatellite(Class keyClass, PropertySet satellite) {
			mc.addSatellite(keyClass, satellite);
		}
    	
		@Override
		public ContentType writeTo(OutputStream out) throws IOException {
			return new ContentType.DepContentTypeImpl((ContentTypeImpl)mc.writeTo(out));
		}

		@Override
		public ContentType getContentType() {
			return new ContentType.DepContentTypeImpl((ContentTypeImpl)mc.getContentType());
		}

    }
    
    public static org.jvnet.ws.message.MessageContextFactory wrap(
    		com.oracle.webservices.api.message.MessageContextFactory mcf) {
    	if (mcf instanceof MessageContextFactory)
    		return (MessageContextFactory) mcf;
    	return new MCFWrapper(mcf);
    }
    
    public static org.jvnet.ws.message.MessageContext wrap(
    		com.oracle.webservices.api.message.MessageContext mc) {
    	if (mc instanceof MessageContext)
    		return (MessageContext) mc;
    	return new MCWrapper(mc);
    }
    
    private static final class MCFWrapper extends MessageContextFactory {
    	private com.oracle.webservices.api.message.MessageContextFactory mcf;
    	
    	public MCFWrapper(com.oracle.webservices.api.message.MessageContextFactory mcf) {
    		this.mcf = mcf;
    	}

		@Override
		protected MessageContextFactory newFactory(WebServiceFeature... f) {
			throw new UnsupportedOperationException(); // Intentionally not supported
		}

		@Override
		public MessageContext createContext() {
			return wrap(mcf.createContext());
		}

		@Override
		public MessageContext createContext(SOAPMessage m) {
			return wrap(mcf.createContext(m));
		}

		@Override
		public MessageContext createContext(Source m) {
			return wrap(mcf.createContext(m));
		}

		@Override
		public MessageContext createContext(Source m, Style envelopeStyle) {
			return wrap(mcf.createContext(m, envelopeStyle));
		}

		@Override
		public MessageContext createContext(Source m, com.oracle.webservices.api.EnvelopeStyle.Style envelopeStyle) {
			return wrap(mcf.createContext(m, envelopeStyle));
		}

		@Override
		public MessageContext createContext(InputStream in, String contentType)
				throws IOException {
			return wrap(mcf.createContext(in, contentType));
		}

		@Override
		@Deprecated
		public MessageContext createContext(InputStream in, MimeHeaders headers)
				throws IOException {
			return wrap(mcf.createContext(in, headers));
		}

		@Override
		@Deprecated
		public MessageContext doCreate() {
			return wrap(mcf.doCreate());

		}

		@Override
		@Deprecated
		public MessageContext doCreate(SOAPMessage m) {
			return wrap(mcf.doCreate(m));
		}

		@Override
		@Deprecated
		public MessageContext doCreate(Source x, SOAPVersion soapVersion) {
			return wrap(mcf.doCreate(x, soapVersion));
		}
    	
    }

    @Deprecated
    public abstract MessageContext doCreate();

    @Deprecated
    public abstract MessageContext doCreate(SOAPMessage m);

    //public abstract MessageContext doCreate(InputStream x);

    @Deprecated
    public abstract MessageContext doCreate(Source x, SOAPVersion soapVersion);

    @Deprecated
    public static MessageContext create(final ClassLoader... classLoader) {
        return wrap(
        		com.oracle.webservices.api.message.MessageContextFactory.create(classLoader));
    }

    @Deprecated
    public static MessageContext create(final SOAPMessage m, final ClassLoader... classLoader) {
        return wrap(
        		com.oracle.webservices.api.message.MessageContextFactory.create(m, classLoader));
    }

    @Deprecated
    public static MessageContext create(final Source m, final SOAPVersion v, final ClassLoader... classLoader) {
        return wrap(
        		com.oracle.webservices.api.message.MessageContextFactory.create(m, v, classLoader));
    }
}

