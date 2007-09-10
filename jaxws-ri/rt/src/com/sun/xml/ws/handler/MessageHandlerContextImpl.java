package com.sun.xml.ws.handler;

import com.sun.xml.ws.api.handler.MessageHandlerContext;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.message.HeaderList;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.addressing.WsaPropertyBag;
import com.sun.istack.NotNull;
import org.w3c.dom.Element;

import java.util.List;
import java.util.Set;

/**
 * @author Rama Pulavarthi
 */
public class MessageHandlerContextImpl extends MessageUpdatableContext implements MessageHandlerContext {
    private Set<String> roles;
    private WSBinding binding;

    public MessageHandlerContextImpl(WSBinding binding, Packet packet, Set<String> roles) {
        super(packet);
        this.binding = binding;
        this.roles = roles; 
    }
    public Message getMessage() {
        return packet.getMessage();
    }

    public void setMessage(Message message) {
        packet.setMessage(message);
    }

    public Set<String> getRoles() {
        return roles;
    }

    public WSBinding getWSBinding() {
        return binding;
    }

    void updateMessage() {
       // Do Nothing
    }

    void setPacketMessage(Message newMessage) {
        setMessage(newMessage);
    }
}
