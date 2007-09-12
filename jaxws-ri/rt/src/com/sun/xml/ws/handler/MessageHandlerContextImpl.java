package com.sun.xml.ws.handler;

import com.sun.istack.Nullable;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.handler.MessageHandlerContext;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.model.SEIModel;

import java.util.Set;

/**
 * @author Rama Pulavarthi
 */
public class MessageHandlerContextImpl extends MessageUpdatableContext implements MessageHandlerContext {
    private @Nullable SEIModel seiModel;
    private Set<String> roles;
    private WSBinding binding;

    public MessageHandlerContextImpl(@Nullable SEIModel seiModel, WSBinding binding, Packet packet, Set<String> roles) {
        super(packet);
        this.seiModel = seiModel;
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

    public @Nullable SEIModel getSEIModel() {
        return seiModel;
    }

    void updateMessage() {
       // Do Nothing
    }

    void setPacketMessage(Message newMessage) {
        setMessage(newMessage);
    }
}
