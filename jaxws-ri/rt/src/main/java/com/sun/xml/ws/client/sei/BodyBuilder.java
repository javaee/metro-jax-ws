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

package com.sun.xml.ws.client.sei;

import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Messages;
import com.sun.xml.ws.message.jaxb.JAXBMessage;
import com.sun.xml.ws.model.ParameterImpl;
import com.sun.xml.ws.model.WrapperParameter;
import com.sun.xml.ws.spi.db.BindingContext;
import com.sun.xml.ws.spi.db.XMLBridge;
import com.sun.xml.ws.spi.db.PropertyAccessor;
import com.sun.xml.ws.spi.db.WrapperComposite;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.ws.Holder;
import javax.xml.ws.WebServiceException;
import java.util.List;

/**
 * Builds a JAXB object that represents the payload.
 *
 * @see MessageFiller
 * @author Kohsuke Kawaguchi
 */
abstract class BodyBuilder {
    abstract Message createMessage(Object[] methodArgs);

    static final BodyBuilder EMPTY_SOAP11 = new Empty(SOAPVersion.SOAP_11);
    static final BodyBuilder EMPTY_SOAP12 = new Empty(SOAPVersion.SOAP_12);

    private static final class Empty extends BodyBuilder {
        private final SOAPVersion soapVersion;

        public Empty(SOAPVersion soapVersion) {
            this.soapVersion = soapVersion;
        }

        Message createMessage(Object[] methodArgs) {
            return Messages.createEmpty(soapVersion);
        }
    }

    /**
     * Base class for those {@link BodyBuilder}s that build a {@link Message}
     * from JAXB objects.
     */
    private static abstract class JAXB extends BodyBuilder {
        /**
         * This object determines the binding of the object returned
         * from {@link #build(Object[])}.
         */
        private final XMLBridge bridge;
        private final SOAPVersion soapVersion;

        protected JAXB(XMLBridge bridge, SOAPVersion soapVersion) {
            assert bridge!=null;
            this.bridge = bridge;
            this.soapVersion = soapVersion;
        }

        final Message createMessage(Object[] methodArgs) {
            return JAXBMessage.create( bridge, build(methodArgs), soapVersion );
        }

        /**
         * Builds a JAXB object that becomes the payload.
         */
        abstract Object build(Object[] methodArgs);
    }

    /**
     * Used to create a payload JAXB object just by taking
     * one of the parameters.
     */
    final static class Bare extends JAXB {
        /**
         * The index of the method invocation parameters that goes into the payload.
         */
        private final int methodPos;

        private final ValueGetter getter;

        /**
         * Creates a {@link BodyBuilder} from a bare parameter.
         */
        Bare(ParameterImpl p, SOAPVersion soapVersion, ValueGetter getter) {
            super(p.getXMLBridge(), soapVersion);
            this.methodPos = p.getIndex();
            this.getter = getter;
        }

        /**
         * Picks up an object from the method arguments and uses it.
         */
        Object build(Object[] methodArgs) {
            return getter.get(methodArgs[methodPos]);
        }
    }


    /**
     * Used to handle a 'wrapper' style request.
     * Common part of rpc/lit and doc/lit.
     */
    abstract static class Wrapped extends JAXB {

        /**
         * Where in the method argument list do they come from?
         */
        protected final int[] indices;

        /**
         * Abstracts away the {@link Holder} handling when touching method arguments.
         */
        protected final ValueGetter[] getters;

        /**
         * How does each wrapped parameter binds to XML?
         */
        protected XMLBridge[] parameterBridges;

        /**
         * List of Parameters packed in the body.
         * Only used for error diagnostics.
         */
        protected List<ParameterImpl> children;

        protected Wrapped(WrapperParameter wp, SOAPVersion soapVersion, ValueGetterFactory getter) {
            super(wp.getXMLBridge(), soapVersion);
            children = wp.getWrapperChildren();
            indices = new int[children.size()];
            getters = new ValueGetter[children.size()];
            for( int i=0; i<indices.length; i++ ) {
                ParameterImpl p = children.get(i);
                indices[i] = p.getIndex();
                getters[i] = getter.get(p);
            }
        }

        /**
         * Packs a bunch of arguments into a {@link WrapperComposite}.
         */
        protected WrapperComposite buildWrapperComposite(Object[] methodArgs) {
            WrapperComposite cs = new WrapperComposite();
            cs.bridges = parameterBridges;
            cs.values = new Object[parameterBridges.length];

            // fill in wrapped parameters from methodArgs
            for( int i=indices.length-1; i>=0; i-- ) {
                Object arg = getters[i].get(methodArgs[indices[i]]);
                if(arg==null) {
                    throw new WebServiceException("Method Parameter: "+
                        children.get(i).getName()+" cannot be null. This is BP 1.1 R2211 violation.");
                }
                cs.values[i] = arg;
            }

            return cs;
        }
    }

    /**
     * Used to create a payload JAXB object by wrapping
     * multiple parameters into one "wrapper bean".
     */
    final static class DocLit extends Wrapped {
        /**
         * How does each wrapped parameter binds to XML?
         */
        private final PropertyAccessor[] accessors;

        /**
         * Wrapper bean.
         */
        private final Class wrapper;
        
        /**
         * Needed to get wrapper instantiation method.
         */
        private BindingContext bindingContext;
        private boolean dynamicWrapper;        

        /**
         * Creates a {@link BodyBuilder} from a {@link WrapperParameter}.
         */
        DocLit(WrapperParameter wp, SOAPVersion soapVersion, ValueGetterFactory getter) {
            super(wp, soapVersion, getter);
            bindingContext = wp.getOwner().getBindingContext();
            wrapper = (Class)wp.getXMLBridge().getTypeInfo().type;     
            dynamicWrapper = WrapperComposite.class.equals(wrapper);
            parameterBridges = new XMLBridge[children.size()];
            accessors = new PropertyAccessor[children.size()];
            for( int i=0; i<accessors.length; i++ ) {
                ParameterImpl p = children.get(i);
                QName name = p.getName();
                if (dynamicWrapper) {
                    parameterBridges[i] = children.get(i).getInlinedRepeatedElementBridge();
                    if (parameterBridges[i] == null) parameterBridges[i] = children.get(i).getXMLBridge();
                } else {
                    try {
                        accessors[i] = p.getOwner().getBindingContext().getElementPropertyAccessor(
                            wrapper, name.getNamespaceURI(), name.getLocalPart() );
                    } catch (JAXBException e) {
                        throw new WebServiceException(  // TODO: i18n
                            wrapper+" do not have a property of the name "+name,e);
                    }
                }
            }

        }

        /**
         * Packs a bunch of arguments into a {@link WrapperComposite}.
         */
        Object build(Object[] methodArgs) {
            if (dynamicWrapper) return buildWrapperComposite(methodArgs);
            try {
                //Object bean = wrapper.newInstance();
                Object bean = bindingContext.newWrapperInstace(wrapper);

                // fill in wrapped parameters from methodArgs
                for( int i=indices.length-1; i>=0; i-- ) {
                    accessors[i].set(bean,getters[i].get(methodArgs[indices[i]]));
                }

                return bean;
            } catch (InstantiationException e) {
                // this is irrecoverable
                Error x = new InstantiationError(e.getMessage());
                x.initCause(e);
                throw x;
            } catch (IllegalAccessException e) {
                // this is irrecoverable
                Error x = new IllegalAccessError(e.getMessage());
                x.initCause(e);
                throw x;
            } catch (com.sun.xml.ws.spi.db.DatabindingException e) {
                // this can happen when the set method throw a checked exception or something like that
                throw new WebServiceException(e);    // TODO:i18n
            }
        }
    }


    /**
     * Used to create a payload JAXB object by wrapping
     * multiple parameters into a {@link WrapperComposite}.
     *
     * <p>
     * This is used for rpc/lit, as we don't have a wrapper bean for it.
     * (TODO: Why don't we have a wrapper bean for this, when doc/lit does!?)
     */
    final static class RpcLit extends Wrapped {

        /**
         * Creates a {@link BodyBuilder} from a {@link WrapperParameter}.
         */
        RpcLit(WrapperParameter wp, SOAPVersion soapVersion, ValueGetterFactory getter) {
            super(wp, soapVersion, getter);
            // we'll use CompositeStructure to pack requests
            assert wp.getTypeInfo().type==WrapperComposite.class;

            parameterBridges = new XMLBridge[children.size()];
            for( int i=0; i<parameterBridges.length; i++ )
                parameterBridges[i] = children.get(i).getXMLBridge();
        }

        Object build(Object[] methodArgs) {
            return buildWrapperComposite(methodArgs);
        }
    }
}
