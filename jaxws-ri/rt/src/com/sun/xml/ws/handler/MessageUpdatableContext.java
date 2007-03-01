/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */
package com.sun.xml.ws.handler;

import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import javax.xml.ws.handler.MessageContext;

/**
 * The class represents a MessageContext(Properties) and also allows the Message to be modified.
 * This is extended by SOAPMessageContextImpl and LogicalMessageContextImpl.
 *
 * @author WS Development Team
 */
public abstract class MessageUpdatableContext implements MessageContext {
    final Packet packet;
    private MessageContextImpl ctxt;
    /** Creates a new instance of MessageUpdatableContext */
    public MessageUpdatableContext(Packet packet) {
        ctxt = new MessageContextImpl(packet);
        this.packet = packet;
    }
    
    /**
     * Fill a {@link Packet} with values of this {@link MessageContext}.
     */
    private void fill(Packet packet) {
        ctxt.fill(packet);
    }
    /**
     * Updates Message in the packet with user modifications
     */
    abstract void updateMessage(); 
    
    /**
     * Updates Message in the packet with user modifications
     * returns the new packet's message
     */
    Message getPacketMessage(){
        updateMessage();
        return packet.getMessage();
    }
    
    /**
     * Sets Message in the packet
     * Any user modifications done on previous Message are lost. 
     */
    abstract void setPacketMessage(Message newMessage);
    
    /**
     * Updates the complete packet with user modfications to the message and 
     * properties cahnges in MessageContext
     */
    final void updatePacket() {
        updateMessage();
        fill(packet);
    }
    
    MessageContextImpl getMessageContext() {
        return ctxt;
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
        return ctxt.put(str, obj);
    }

    public void putAll(Map<? extends String, ? extends Object> map) {
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
