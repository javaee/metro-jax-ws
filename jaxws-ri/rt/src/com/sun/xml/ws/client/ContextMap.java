/*
 * $Id: ContextMap.java,v 1.11 2005-09-09 07:21:04 vivekp Exp $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */
package com.sun.xml.ws.client;

import com.sun.xml.ws.encoding.soap.SOAPConstants;
import com.sun.xml.ws.util.Version;

import javax.xml.bind.JAXBContext;
import javax.xml.ws.BindingProvider;
import static javax.xml.ws.BindingProvider.*;
import javax.xml.ws.Dispatch;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public abstract class ContextMap extends HashMap<Object, Object>
    implements BindingProviderProperties {

    protected static List _knownProperties;

    protected HashMap _allowedValues;
    protected HashMap _allowedClass;


    protected java.lang.String[] ALLOWED_ENCODING = {"", SOAPConstants.URI_ENCODING};
    protected java.lang.String[] ALLOWED_OPERATION_STYLE = {"document", "rpc"};
    protected java.lang.Boolean[] ALLOWED_SESSION_MAINTAINED =
        {Boolean.TRUE, Boolean.FALSE};
    protected java.lang.Boolean[] ALLOWED_SOAPACTION_USE =
        {Boolean.TRUE, Boolean.FALSE};


    protected BindingProvider _owner;
    protected PortInfoBase portInfo;
    protected Version version;

    public abstract ContextMap copy();

    void init() {

        _allowedValues = new HashMap<String, Object>();

        _allowedValues.put(SESSION_MAINTAIN_PROPERTY, ALLOWED_SESSION_MAINTAINED);
        _allowedValues.put(SOAPACTION_USE_PROPERTY, ALLOWED_SOAPACTION_USE);

        //JAXWS 2.0 defined
        _allowedClass = new HashMap<String, Class>();
        _allowedClass.put(USERNAME_PROPERTY, java.lang.String.class);
        _allowedClass.put(PASSWORD_PROPERTY, java.lang.String.class);
        _allowedClass.put(ENDPOINT_ADDRESS_PROPERTY, java.lang.String.class);
        _allowedClass.put(SESSION_MAINTAIN_PROPERTY, java.lang.Boolean.class);
        _allowedClass.put(SOAPACTION_USE_PROPERTY, java.lang.Boolean.class);
        _allowedClass.put(SOAPACTION_URI_PROPERTY, java.lang.String.class);

        //now defined in jaxwscontext
        _allowedClass.put(BindingProviderProperties.JAXB_CONTEXT_PROPERTY, JAXBContext.class);

        List<java.lang.String> temp = new ArrayList<java.lang.String>();
        //JAXWS 2.0 defined
        temp.add(USERNAME_PROPERTY);
        temp.add(PASSWORD_PROPERTY);
        temp.add(ENDPOINT_ADDRESS_PROPERTY);
        temp.add(SESSION_MAINTAIN_PROPERTY);
        temp.add(SOAPACTION_USE_PROPERTY);
        temp.add(SOAPACTION_URI_PROPERTY);

        temp.add(BindingProviderProperties.JAXB_CONTEXT_PROPERTY);
        //implementation specific
        temp.add(BindingProviderProperties.ACCEPT_ENCODING_PROPERTY);
        temp.add(BindingProviderProperties.CLIENT_TRANSPORT_FACTORY);
        //used to get stub in runtime for handler chain
        temp.add(BindingProviderProperties.JAXWS_CLIENT_HANDLE_PROPERTY);
        temp.add(BindingProviderProperties.JAXWS_CLIENT_HANDLE_PROPERTY);

        //JAXRPC 1.0 - 1.1 DEFINED - implementation specific
        temp.add(BindingProviderProperties.HTTP_COOKIE_JAR);
        temp.add(BindingProviderProperties.ONE_WAY_OPERATION);
        temp.add(BindingProviderProperties.HTTP_STATUS_CODE);
        temp.add(BindingProviderProperties.HOSTNAME_VERIFICATION_PROPERTY);
        temp.add(BindingProviderProperties.REDIRECT_REQUEST_PROPERTY);
        temp.add(BindingProviderProperties.SECURITY_CONTEXT);
        temp.add(BindingProviderProperties.SET_ATTACHMENT_PROPERTY);
        temp.add(BindingProviderProperties.GET_ATTACHMENT_PROPERTY);
        //Tod:check with mark regarding property modification
        //_knownProperties = Collections.unmodifiableSet(temp);

        temp.add(MessageContext.MESSAGE_ATTACHMENTS);
        temp.add(MessageContext.WSDL_DESCRIPTION);
        temp.add(MessageContext.WSDL_INTERFACE);
        temp.add(MessageContext.WSDL_OPERATION);
        temp.add(MessageContext.WSDL_PORT);
        temp.add(MessageContext.WSDL_SERVICE);

        // Content negotiation property for FI -- "none", "pessimistic", "optimistic"
        temp.add(BindingProviderProperties.CONTENT_NEGOTIATION_PROPERTY);
        temp.add(BindingProviderProperties.MTOM_THRESHOLOD_VALUE);
        _knownProperties = new ArrayList(temp);
    }

    //used for dispatch
    public ContextMap(PortInfoBase info, BindingProvider provider) {
        init();
        _owner = provider;
        if (info != null) {
            //put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
            //    info.getTargetEndpoint());
            this.portInfo = info;
        }
    }

    //may not need this
    public ContextMap(Object owner) {
        this(null, (BindingProvider) owner);

    }

    boolean doValidation() {
        if (_owner != null) {
            if (_owner instanceof BindingProvider)
                return true;
        }
        return false;
    }

    public Object put(Object name, Object value) {
        if (doValidation()) {
            validateProperty((String) name, value, true);
            return super.put((Object) name, value);
        }
        return null;
    }

    public Object get(Object name) {
        if (doValidation()) {
            validateProperty((String) name, null, false);
            return super.get(name);
        }
        return null;
    }

    public Iterator getPropertyNames() {
        return keySet().iterator();
    }


    public Object remove(Object name) {
        if (doValidation()) {
            validateProperty((java.lang.String) name, null, false);
            return super.remove(name);
        }
        return null;
    }

    private boolean isKnownProperty(String name) {
        boolean found = false;
        Iterator iter = _knownProperties.iterator();
        while (iter.hasNext()) {
            String knownName = (String) iter.next();
            if (knownName.equals(name)) {
                found = true;
                break;
            }
        }
        return found;
    }

    private boolean isAllowedValue(String name, Object value) {
        if (value == null)
            return false;

        Object[] values = (Object[]) _allowedValues.get(name);
        if (values != null) {
            boolean allowed = false;
            for (Object o : values) {
                if (STRING_CLASS.isInstance(o) && (STRING_CLASS.isInstance(value))) {
                    if (((java.lang.String) o).equalsIgnoreCase((java.lang.String) value)) {
                        allowed = true;
                        break;
                    }
                } else if (BOOLEAN_CLASS.isInstance(o) && (BOOLEAN_CLASS.isInstance(value))) {
                    if (Boolean.FALSE.equals(o) || Boolean.TRUE.equals(o)) {
                        allowed = true;
                        break;
                    }
                } else {
                    //log this
                }
            }
            return allowed;
        }
        return true;
    }


    private boolean isAllowedClass(String propName, Object value) {

        Class allowedClass = (Class) _allowedClass.get(propName);
        if (allowedClass != null) {
            return (allowedClass.isInstance(value)) ? true : false;
        }
        return true;
    }

    private boolean isDynamic(String name) {
        return true;
    }

    private void validateProperty(String name, Object value, boolean isSetter) {
        if (name == null)
            throw new WebServiceException(name + " is a User-defined property - property name can not be null. ",
                new IllegalArgumentException("Name of property is null.  This is an invalid property name. "));


        if (!isKnownProperty(name)) {
            //do validation check on not "javax.xml.ws."
            if (name.startsWith("javax.xml.ws"))
                throw new WebServiceException(name + " is a User-defined property - can not start with javax.xml.ws. package",
                    new IllegalArgumentException("can not start with javax.xml.ws. package"));                                            //let's check the propertyContext
        }

        //is it alreadySet
        //Object currentPropValue = get(name);
        //if (currentPropValue != null) {
        //  if (!isDynamic(name))
        //      throw new WebServiceException("Property bound to Binding Instance",
        //          new IllegalArgumentException("Cannot overwrite the Static Property"));
        //}

        if (isSetter) {
            if (!isAllowedClass(name, value))
                throw new WebServiceException(value + " is Not Allowed Class for property " + name,
                    new IllegalArgumentException("Not Allowed Class for property"));

            if (!isAllowedValue(name, value))
                throw new WebServiceException(value + " is Not Allowed Value for property " + name,
                    new IllegalArgumentException("Not Allowed value"));
        }

    }

    private static final Class STRING_CLASS = String.class;
    private static final Class BOOLEAN_CLASS = Boolean.class;
    private static final Class ENDPOINT_IF_BASE_CLASS = EndpointIFBase.class;
    private static final Class DISPATCH_CLASS = Dispatch.class;
    private static final Class PROXY_CLASS = Proxy.class;     
    private final static Class JAXBCONTEXT_CLASS =
        JAXBContext.class;
    private static Class CLIENT_TRANSPORT_FACTORY_CLASS = ClientTransportFactory.class;

    public static enum StyleAndUse {
        RPC_LITERAL,
        DOCLIT_WRAPPER_STYLE, DOCLIT_NONWRAPPER_STYLE
    }

}

