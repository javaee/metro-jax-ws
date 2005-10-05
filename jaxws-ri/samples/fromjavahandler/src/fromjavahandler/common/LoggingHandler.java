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
package fromjavahandler.common;


import java.io.PrintStream;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

/*
 * This simple SOAPHandler will output the contents of incoming
 * and outgoing messages.
 */
public class LoggingHandler implements SOAPHandler<SOAPMessageContext> {
    
    // change this to redirect output if desired
    private static PrintStream out = System.out;
    
    public Set<QName> getHeaders () {
        return null;
    }
    
    public boolean handleMessage (SOAPMessageContext smc) {
        logToSystemOut (smc);
        return true;
    }
    
    public boolean handleFault (SOAPMessageContext smc) {
        logToSystemOut (smc);
        return true;
    }
    
    // nothing to clean up
    public void close (MessageContext messageContext) {
    }
    
    /*
     * Check the MESSAGE_OUTBOUND_PROPERTY in the context
     * to see if this is an outgoing or incoming message.
     * Write a brief message to the print stream and
     * output the message. The writeTo() method can throw
     * SOAPException or IOException
     */
    private void logToSystemOut (SOAPMessageContext smc) {
        Boolean outboundProperty = (Boolean)
        smc.get (MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        
        if (outboundProperty.booleanValue ()) {
            out.println ("\nOutbound message:");
        } else {
            out.println ("\nInbound message:");
        }
        
        SOAPMessage message = smc.getMessage ();
        try {
            message.writeTo (out);
            out.println ("");   // just to add a newline
        } catch (Exception e) {
            out.println ("Exception in handler: " + e);
        }
    }
}
