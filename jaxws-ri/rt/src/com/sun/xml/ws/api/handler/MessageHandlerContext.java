package com.sun.xml.ws.api.handler;

import com.sun.istack.Nullable;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.model.SEIModel;
import javax.xml.ws.handler.MessageContext;
import java.util.Set;

/**
 * The <code>MessageHandlerContext</code> interface extends
 * <code>MessageContext</code> to provide easy access to the contained message.
 *
 * This context provides access to RI's <code>Message</code> model for efficient access
 * to various things like accessing headers etc. It also provides access to
 * binding information as <code>WSBinding</code>.
 *
 * @author Rama Pulavarthi
 * @since JAX-WS 2.1.3
 */
public interface MessageHandlerContext extends MessageContext {
    /**
     * Gets the message from this message context
     *
     * @return The contained message; returns <code>null</code> if no
     *         message is present in this message context
     */
    public Message getMessage();

    
    /**
     * Sets the message in this message context
     */
    public void setMessage(Message message);

    /**
     * @see javax.xml.ws.handler.soap.SOAPMessageContext#getRoles()
     */
     public Set<String> getRoles();


    /**
     * Provides access to <code>WSBinding</code> which can be used in various ways.
     * for example: <code>WSBinding#getSOAPVersion</code> to get SOAP version of the binding.
     *              <code>WSBinding#isFeatureEnabled(AddressingFeature)</code> to check if addressing is enabled
     */
    public WSBinding getWSBinding();

    /**
     * Provides access to <code>SEIModel</code>.
     */
    public @Nullable SEIModel getSEIModel();
   
}
