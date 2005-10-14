/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */
package com.sun.xml.ws.handler;
import com.sun.pept.ept.MessageInfo;
import com.sun.xml.ws.encoding.jaxb.JAXBBeanInfo;
import com.sun.xml.ws.encoding.jaxb.JAXBTypeSerializer;
import com.sun.xml.ws.encoding.soap.SOAPEPTFactory;
import com.sun.xml.ws.encoding.soap.internal.InternalMessage;
import java.util.Collections;
import java.util.HashSet;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of SOAPMessageContext. This class is used at runtime
 * to pass to the handlers for processing soap messages.
 *
 * @see MessageContextImpl
 *
 * @author WS Development Team
 */
public class SOAPMessageContextImpl implements SOAPMessageContext {

    private SOAPHandlerContext handlerCtxt;
    private MessageContext ctxt;
    private Set<URI> roles;
    private static Map<String, Class> allowedTypes = null;
    private boolean failure;
    private static final Set<String> KNOWN_PROPERTIES;
    
    static {
        allowedTypes = new HashMap<String, Class>();
        allowedTypes.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, String.class);
        allowedTypes.put(BindingProvider.PASSWORD_PROPERTY, String.class);
        allowedTypes.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, Boolean.class);
        allowedTypes.put(BindingProvider.SOAPACTION_URI_PROPERTY, String.class);
        allowedTypes.put(BindingProvider.SOAPACTION_USE_PROPERTY, Boolean.class);
        allowedTypes.put(BindingProvider.USERNAME_PROPERTY, String.class);
        
        allowedTypes.put(MessageContext.HTTP_REQUEST_HEADERS, Map.class);
        allowedTypes.put(MessageContext.HTTP_REQUEST_METHOD, String.class);
        allowedTypes.put(MessageContext.HTTP_RESPONSE_CODE, Integer.class);
        allowedTypes.put(MessageContext.HTTP_RESPONSE_HEADERS, Map.class);
        allowedTypes.put(MessageContext.MESSAGE_ATTACHMENTS, Map.class);
        allowedTypes.put(MessageContext.MESSAGE_OUTBOUND_PROPERTY, Boolean.class);
        allowedTypes.put(MessageContext.SERVLET_CONTEXT, ServletContext.class);
        allowedTypes.put(MessageContext.SERVLET_REQUEST, HttpServletRequest.class);
        allowedTypes.put(MessageContext.SERVLET_RESPONSE, HttpServletResponse.class);
        allowedTypes.put(MessageContext.SERVLET_SESSION, HttpSession.class);
        allowedTypes.put(MessageContext.WSDL_DESCRIPTION, URI.class);
        allowedTypes.put(MessageContext.WSDL_SERVICE, QName.class);
        allowedTypes.put(MessageContext.WSDL_PORT, QName.class);
        allowedTypes.put(MessageContext.WSDL_INTERFACE, QName.class);
        allowedTypes.put(MessageContext.WSDL_OPERATION, QName.class);
        allowedTypes.put(MessageContext.MESSAGE_OUTBOUND_PROPERTY, Boolean.class);
        
        Set<String> temp = new HashSet<String>();
        temp.add(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
        temp.add(BindingProvider.PASSWORD_PROPERTY);
        temp.add(BindingProvider.SESSION_MAINTAIN_PROPERTY);
        temp.add(BindingProvider.SOAPACTION_URI_PROPERTY);
        temp.add(BindingProvider.SOAPACTION_USE_PROPERTY);
        temp.add(BindingProvider.USERNAME_PROPERTY);
        
        temp.add(MessageContext.HTTP_REQUEST_HEADERS);
        temp.add(MessageContext.HTTP_REQUEST_METHOD);
        temp.add(MessageContext.HTTP_RESPONSE_CODE);
        temp.add(MessageContext.HTTP_RESPONSE_HEADERS);
        temp.add(MessageContext.MESSAGE_ATTACHMENTS);
        temp.add(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        temp.add(MessageContext.SERVLET_CONTEXT);
        temp.add(MessageContext.SERVLET_REQUEST);
        temp.add(MessageContext.SERVLET_RESPONSE);
        temp.add(MessageContext.SERVLET_SESSION);
        temp.add(MessageContext.WSDL_DESCRIPTION);
        temp.add(MessageContext.WSDL_SERVICE);
        temp.add(MessageContext.WSDL_PORT);
        temp.add(MessageContext.WSDL_INTERFACE);
        temp.add(MessageContext.WSDL_OPERATION);
        KNOWN_PROPERTIES = Collections.unmodifiableSet(temp);
    }

    public SOAPMessageContextImpl(SOAPHandlerContext handlerCtxt) {
        this.handlerCtxt = handlerCtxt;
        this.ctxt = handlerCtxt.getMessageContext();
//        if (allowedTypes == null) {
//        }
    }

    public SOAPMessage getMessage() {
        SOAPMessage soap = handlerCtxt.getSOAPMessage();
        InternalMessage intr = handlerCtxt.getInternalMessage();
        if (intr == null && soap != null) {
            // Not much to do
        } else if (intr != null && soap != null) {
            // Overlay BodyBlock of InternalMessage on top of existing SOAPMessage
            MessageInfo messageInfo = handlerCtxt.getMessageInfo();
            SOAPEPTFactory eptf = (SOAPEPTFactory)messageInfo.getEPTFactory();
            soap = eptf.getSOAPEncoder().toSOAPMessage(intr, soap);
            setMessage(soap);        // It also sets InernalMessage to null
        } else if (intr != null && soap == null) {
            // Convert InternalMessage to a SOAPMessage
            MessageInfo messageInfo = handlerCtxt.getMessageInfo();
            SOAPEPTFactory eptf = (SOAPEPTFactory)messageInfo.getEPTFactory();
            soap = eptf.getSOAPEncoder().toSOAPMessage(intr, messageInfo);
            setMessage(soap);        // It also sets InernalMessage to null
        } else {
            throw new WebServiceException("Don't have SOAPMessage");
        }
        return soap;
    }

    public void setMessage(SOAPMessage soapMessage) {
        handlerCtxt.setSOAPMessage(soapMessage);
        // current InternalMessage is not valid anymore. So reset it.
        handlerCtxt.setInternalMessage(null);
    }

    /*
     * TODO: take care of allRoles parameter
     *
     */
    public Object[] getHeaders(QName header, JAXBContext jaxbContext, boolean allRoles) {
        try {
            List beanList = new ArrayList();
            SOAPMessage msg = getMessage();
            Name name = SOAPFactory.newInstance().createName(header.getLocalPart(),
                    header.getPrefix(), header.getNamespaceURI());
            SOAPHeader sHeader = msg.getSOAPHeader();
            if (sHeader == null) {
                return new Object[0];
            }
            Iterator i = sHeader.getChildElements(name);
            while(i.hasNext()) {
                SOAPHeaderElement child = (SOAPHeaderElement)i.next();
                Source source = new DOMSource(child);
                beanList.add(JAXBTypeSerializer.deserialize(source, jaxbContext));
            }
            return beanList.toArray();
        } catch(Exception e) {
            throw new WebServiceException(e);
        }
    }

    public Set<URI> getRoles() {
        return roles;
    }

    void setRoles(Set<URI> roles) {
        this.roles = roles;
    }

    private void validateProperty(String name, Object value) {
        if (name == null)
            throw new WebServiceException(name + " is a User-defined property - property name can not be null. ",
                new IllegalArgumentException("Name of property is null.  This is an invalid property name. "));

        if (allowedTypes.containsKey(name)) {
            Class clazz = allowedTypes.get(name);
            if (!(clazz.isInstance(value)))
                throw new HandlerException("handler.messageContext.invalid.class",
                        new Object[] { value, name });
        }

        if (!KNOWN_PROPERTIES.contains(name)) {
            //do validation check on not "javax.xml.ws."
            if (name.startsWith("javax.xml.ws"))
                throw new WebServiceException(name + " is a User-defined property - can not start with javax.xml.ws. package",
                    new IllegalArgumentException("can not start with javax.xml.ws. package"));                                            //let's check the propertyContext
        }
    }

    public void setScope(String name, Scope scope) {
        ctxt.setScope(name, scope);
    }

    public Scope getScope(String name) {
        return ctxt.getScope(name);
    }

    /* java.util.Map methods below here */
    
    public void clear() {
        ctxt.clear();
    }

    public boolean containsKey(Object obj) {
        return ctxt.containsKey(obj);
    }

    public boolean containsValue(Object obj) {
        return ctxt.containsValue(obj);
    }

    public Set<Entry<String, Object>> entrySet() {
        return ctxt.entrySet();
    }

    public Object get(Object obj) {
        return ctxt.get(obj);
    }

    public boolean isEmpty() {
        return ctxt.isEmpty();
    }

    public Set<String> keySet() {
        return ctxt.keySet();
    }

    public Object put(String str, Object obj) {
        validateProperty(str, obj);
        return ctxt.put(str, obj);
    }

    public void putAll(Map<? extends String, ? extends Object> map) {
        for (String key : map.keySet()) {
            validateProperty(key, map.get(key));
        }
        ctxt.putAll(map);
    }

    public Object remove(Object obj) {
        return ctxt.remove(obj);
    }

    public int size() {
        return ctxt.size();
    }

    public Collection<Object> values() {
        return ctxt.values();
    }
    
}
    