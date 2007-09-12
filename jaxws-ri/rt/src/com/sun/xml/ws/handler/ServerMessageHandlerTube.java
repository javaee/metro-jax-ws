package com.sun.xml.ws.handler;

import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.handler.MessageHandler;
import com.sun.xml.ws.api.message.Attachment;
import com.sun.xml.ws.api.message.AttachmentSet;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.api.pipe.helper.AbstractFilterTubeImpl;
import com.sun.xml.ws.binding.BindingImpl;
import com.sun.xml.ws.client.HandlerConfiguration;
import com.sun.xml.ws.message.DataHandlerAttachment;

import javax.activation.DataHandler;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;
import java.util.*;

/**
 * @author Rama Pulavarthi
 */
public class ServerMessageHandlerTube extends HandlerTube{
    private SEIModel seiModel;
    private WSBinding binding;
    private List<MessageHandler> messageHandlers;
    private Set<String> roles;

    // Handle to LogicalHandlerTube means its used on SERVER-SIDE

    /**
     * This constructor is used on client-side where, LogicalHandlerTube is created
     * first and then a SOAPHandlerTube is created with a handler to that
     * LogicalHandlerTube.
     * With this handle, SOAPHandlerTube can call LogicalHandlerTube.closeHandlers()
     */
    public ServerMessageHandlerTube(SEIModel seiModel, WSBinding binding, Tube next, HandlerTube cousinTube) {
        super(next, cousinTube);
        this.seiModel = seiModel;
        this.binding = binding;
        setUpProcessorOnce();
    }

    /**
     * Copy constructor for {@link com.sun.xml.ws.api.pipe.Tube#copy(com.sun.xml.ws.api.pipe.TubeCloner)}.
     */
    private ServerMessageHandlerTube(ServerMessageHandlerTube that, TubeCloner cloner) {
        super(that, cloner);
        this.binding = that.binding;
        setUpProcessorOnce();
    }

    boolean isHandlerChainEmpty() {
        return messageHandlers.isEmpty();
    }

    private void setUpProcessorOnce() {
        messageHandlers = new ArrayList<MessageHandler>();
        HandlerConfiguration handlerConfig = ((BindingImpl) binding).getHandlerConfig();
        List<MessageHandler> msgHandlersSnapShot= handlerConfig.getMessageHandlers();
        if (!msgHandlersSnapShot.isEmpty()) {
            messageHandlers.addAll(msgHandlersSnapShot);
            roles = new HashSet<String>();
            roles.addAll(handlerConfig.getRoles());
            processor = new SOAPHandlerProcessor(false, this, binding, messageHandlers);
        }
    }

    void callHandlersOnResponse(MessageUpdatableContext context, boolean handleFault) {
        //Lets copy all the MessageContext.OUTBOUND_ATTACHMENT_PROPERTY to the message
        Map<String, DataHandler> atts = (Map<String, DataHandler>) context.get(MessageContext.OUTBOUND_MESSAGE_ATTACHMENTS);
        AttachmentSet attSet = packet.getMessage().getAttachments();
        for(String cid : atts.keySet()){
            if (attSet.get(cid) == null) { // Otherwise we would be adding attachments twice
                Attachment att = new DataHandlerAttachment(cid, atts.get(cid));
                attSet.add(att);
            }
        }

        try {
            //SERVER-SIDE
            processor.callHandlersResponse(HandlerProcessor.Direction.OUTBOUND, context, handleFault);

        } catch (WebServiceException wse) {
            //no rewrapping
            throw wse;
        } catch (RuntimeException re) {
            throw re;

        }
    }

    boolean callHandlersOnRequest(MessageUpdatableContext context, boolean isOneWay) {
        boolean handlerResult;
        try {
            //SERVER-SIDE
            handlerResult = processor.callHandlersRequest(HandlerProcessor.Direction.INBOUND, context, !isOneWay);

        } catch (RuntimeException re) {
            remedyActionTaken = true;
            throw re;

        }
        if (!handlerResult) {
            remedyActionTaken = true;
        }
        return handlerResult;
    }

    void setUpProcessor() {
        // Do nothing, Processor is setup in the constructor.
    }

    MessageUpdatableContext getContext(Packet packet) {
       MessageHandlerContextImpl context = new MessageHandlerContextImpl(seiModel, binding, packet, roles);
       return context;
    }

   /**
     * Close SOAPHandlers first and then LogicalHandlers on Client
     * Close LogicalHandlers first and then SOAPHandlers on Server
     */
    public void close(MessageContext msgContext) {
        //assuming cousinTube is called if requestProcessingSucessful is true
        if (requestProcessingSucessful) {
            if (cousinTube != null) {
                // Close LogicalHandlerTube
                cousinTube.closeCall(msgContext);
            }
        }
        if (processor != null)
            closeMessageHandlers(msgContext);

    }

    /**
     * This is called from cousinTube.
     * Close this Tube's handlers.
     */
    public void closeCall(MessageContext msgContext) {
        closeMessageHandlers(msgContext);
    }

    //TODO:
    private void closeMessageHandlers(MessageContext msgContext) {
        if (processor == null)
            return;
        if (remedyActionTaken) {
            //Close only invoked handlers in the chain
            //SERVER-SIDE
            processor.closeHandlers(msgContext, processor.getIndex(), messageHandlers.size() - 1);
            processor.setIndex(-1);
            //reset remedyActionTaken
            remedyActionTaken = false;
        } else {
            //Close all handlers in the chain
            //SERVER-SIDE
            processor.closeHandlers(msgContext, 0, messageHandlers.size() - 1);

        }
    }

    public AbstractFilterTubeImpl copy(TubeCloner cloner) {
        return new ServerMessageHandlerTube(this, cloner);
    }
}
