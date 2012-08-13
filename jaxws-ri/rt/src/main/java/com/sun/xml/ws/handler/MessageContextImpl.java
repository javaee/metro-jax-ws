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

package com.sun.xml.ws.handler;

import com.sun.xml.ws.api.message.Attachment;
import com.sun.xml.ws.api.message.AttachmentSet;
import com.sun.xml.ws.api.message.Packet;

import javax.activation.DataHandler;
import javax.xml.ws.handler.MessageContext;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jvnet.ws.message.ReadOnlyPropertyException;

/**
 *
 * @author WS Development Team
 */

class MessageContextImpl implements MessageContext {
    private Map<String,Object> fallbackMap = null;
    private Set<String> handlerScopeProps;
    Packet packet;


    void fallback() {
        if(fallbackMap == null) {
            fallbackMap = new HashMap<String,Object>();
            fallbackMap.putAll(packet.createMapView());
            fallbackMap.putAll(packet.invocationProperties);
        }
    }
    /** Creates a new instance of MessageContextImpl */
    public MessageContextImpl(Packet packet) {
        this.packet = packet;
        handlerScopeProps =  packet.getHandlerScopePropertyNames(false);
    }
    protected void updatePacket() {
        throw new UnsupportedOperationException("wrong call");
    }

    public void setScope(String name, Scope scope) {
        if(!containsKey(name))
            throw new IllegalArgumentException("Property " + name + " does not exist.");
        if(scope == Scope.APPLICATION) {
            handlerScopeProps.remove(name);
        } else {
            handlerScopeProps.add(name);

        }
    }

    public Scope getScope(String name) {
        if(!containsKey(name))
            throw new IllegalArgumentException("Property " + name + " does not exist.");
        if(handlerScopeProps.contains(name)) {
            return Scope.HANDLER;
        } else {
            return Scope.APPLICATION;
        }
    }

    public int size() {
        fallback();
        return fallbackMap.size();
    }

    public boolean isEmpty() {
        fallback();
        return fallbackMap.isEmpty();
    }

    public boolean containsKey(Object key) {
        if(fallbackMap == null) {
            if(packet.supports(key))
                return true;
            return packet.invocationProperties.containsKey(key);
        } else {
            fallback();
            return fallbackMap.containsKey(key);
        }
    }

    public boolean containsValue(Object value) {
        fallback();
        return fallbackMap.containsValue(value);
    }

    public Object put(String key, Object value) {
        if (fallbackMap == null) {
            if (packet.supports(key)) {
                return packet.put(key, value);     // strongly typed
            }
            if (!packet.invocationProperties.containsKey(key)) {
                //New property, default to Scope.HANDLER
                handlerScopeProps.add(key);
            }
            return packet.invocationProperties.put(key, value);

        } else {
            fallback();
            if (!fallbackMap.containsKey(key)) {
                //new property, default to Scope.HANDLER
                handlerScopeProps.add(key);
            }
            return fallbackMap.put(key, value);
        }
    }
    public Object get(Object key) {
        if(key == null)
            return null;
        Object value;
        if(fallbackMap == null) {
            if (packet.supports(key)) {
                value =  packet.get(key);    // strongly typed
            } else {
                value = packet.invocationProperties.get(key);
            }
        } else {
            fallback();
            value = fallbackMap.get(key);
        }
        //add the attachments from the Message to the corresponding attachment property
        if(key.equals(MessageContext.OUTBOUND_MESSAGE_ATTACHMENTS) ||
            key.equals(MessageContext.INBOUND_MESSAGE_ATTACHMENTS)){
            Map<String, DataHandler> atts = (Map<String, DataHandler>) value;
            if(atts == null)
                atts = new HashMap<String, DataHandler>();
            AttachmentSet attSet = packet.getMessage().getAttachments();
            for(Attachment att : attSet){
                String cid = att.getContentId();
                if (cid.indexOf("@jaxws.sun.com") == -1) {
                    Object a = atts.get(cid);
                    if (a == null) {
                        a = atts.get("<" + cid + ">");
                        if (a == null) atts.put(att.getContentId(), att.asDataHandler());
                    }
                } else {
                    atts.put(att.getContentId(), att.asDataHandler());
                }
            }
            return atts;
        }
        return value;
    }

    public void putAll(Map<? extends String, ? extends Object> t) {
        fallback();
        for(String key: t.keySet()) {
            if(!fallbackMap.containsKey(key)) {
                //new property, default to Scope.HANDLER
                handlerScopeProps.add(key);
            }
        }
        fallbackMap.putAll(t);
    }

    public void clear() {
        fallback();
        fallbackMap.clear();
    }
    public Object remove(Object key){
        fallback();
        handlerScopeProps.remove(key);
        return fallbackMap.remove(key);
    }
    public Set<String> keySet() {
        fallback();
        return fallbackMap.keySet();
    }
    public Set<Map.Entry<String, Object>> entrySet(){
        fallback();
        return fallbackMap.entrySet();
    }
    public Collection<Object> values() {
        fallback();
        return fallbackMap.values();
    }


    /**
     * Fill a {@link Packet} with values of this {@link MessageContext}.
     */
    void fill(Packet packet) {
        if(fallbackMap != null) {
            for (Entry<String, Object> entry : fallbackMap.entrySet()) {
                String key = entry.getKey();
                if (packet.supports(key)) {
                    try {
                        packet.put(key, entry.getValue());
                    } catch (ReadOnlyPropertyException e) {
                        // Nothing to do
                    }
                } else {
                    packet.invocationProperties.put(key, entry.getValue());
                }
            }

            //Remove properties which are removed by user.
            packet.createMapView().keySet().retainAll(fallbackMap.keySet());
            packet.invocationProperties.keySet().retainAll(fallbackMap.keySet());
        }
    }

}
