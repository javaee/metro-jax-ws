/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
    public final void updatePacket() {
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
