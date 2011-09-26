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

package com.sun.xml.ws.addressing;

import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import com.sun.xml.ws.addressing.model.ActionNotSupportedException;
import com.sun.xml.ws.addressing.model.InvalidAddressingHeaderException;
import com.sun.xml.ws.api.EndpointAddress;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.WSService;
import com.sun.xml.ws.api.client.WSPortInfo;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.addressing.WSEndpointReference;
import com.sun.xml.ws.api.message.HeaderList;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Messages;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.pipe.*;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.client.Stub;
import com.sun.xml.ws.developer.JAXWSProperties;
import com.sun.xml.ws.developer.WSBindingProvider;
import com.sun.xml.ws.fault.SOAPFaultBuilder;
import com.sun.xml.ws.message.FaultDetailHeader;
import com.sun.xml.ws.resources.AddressingMessages;
import com.sun.xml.ws.binding.BindingImpl;

import javax.xml.soap.SOAPFault;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Response;
import javax.xml.ws.WebServiceException;
import java.net.URI;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles WS-Addressing for the server.
 *
 * @author Rama Pulavarthi
 * @author Kohsuke Kawaguchi
 * @author Arun Gupta
 */
public class WsaServerTube extends WsaTube {
    private WSEndpoint endpoint;
    // store the replyTo/faultTo of the message currently being processed.
    // both will be set to non-null in processRequest
    private WSEndpointReference replyTo;
    private WSEndpointReference faultTo;
    private boolean isAnonymousRequired = false;
    // Used by subclasses to avoid this class closing the transport back
    // channel based on the ReplyTo/FaultTo addrs being non-anonymous. False
    // can be useful in cases where special back-channel handling is required.
    protected boolean isEarlyBackchannelCloseAllowed = true;
    
    /**
     * WSDLBoundOperation calculated on the Request payload.
     * Used for determining ReplyTo or Fault Action for non-anonymous responses     * 
     */
    private WSDLBoundOperation wbo;
    public WsaServerTube(WSEndpoint endpoint, @NotNull WSDLPort wsdlPort, WSBinding binding, Tube next) {
        super(wsdlPort, binding, next);
        this.endpoint = endpoint;

    }

    public WsaServerTube(WsaServerTube that, TubeCloner cloner) {
        super(that, cloner);
        endpoint = that.endpoint;
    }

    public WsaServerTube copy(TubeCloner cloner) {
        return new WsaServerTube(this, cloner);
    }

    @Override
    public @NotNull NextAction processRequest(Packet request) {
        Message msg = request.getMessage();
        if(msg==null)   return doInvoke(next,request); // hmm?

        // expose bunch of addressing related properties for advanced applications 
        request.addSatellite(new WsaPropertyBag(addressingVersion,soapVersion,request));

        // Store request ReplyTo and FaultTo in requestPacket.invocationProperties
        // so that they can be used after responsePacket is received.
        // These properties are used if a fault is thrown from the subsequent Pipe/Tubes.

        HeaderList hl = request.getMessage().getHeaders();
        String msgId;
        try {
            replyTo = hl.getReplyTo(addressingVersion, soapVersion);
            faultTo = hl.getFaultTo(addressingVersion, soapVersion);
            msgId = hl.getMessageID(addressingVersion, soapVersion);
        } catch (InvalidAddressingHeaderException e) {
            LOGGER.log(Level.WARNING,
                    addressingVersion.getInvalidMapText()+", Problem header:" + e.getProblemHeader()+ ", Reason: "+ e.getSubsubcode(),e);
            SOAPFault soapFault = helper.createInvalidAddressingHeaderFault(e, addressingVersion);
            // WS-A fault processing for one-way methods
            if ((wsdlPort!=null) && request.getMessage().isOneWay(wsdlPort)) {
                Packet response = request.createServerResponse(null, wsdlPort, null, binding);
                return doReturnWith(response);
            }

            Message m = Messages.create(soapFault);
            if (soapVersion == SOAPVersion.SOAP_11) {
                FaultDetailHeader s11FaultDetailHeader = new FaultDetailHeader(addressingVersion, addressingVersion.problemHeaderQNameTag.getLocalPart(), e.getProblemHeader());
                m.getHeaders().add(s11FaultDetailHeader);
            }

            Packet response = request.createServerResponse(m, wsdlPort, null, binding);
            return doReturnWith(response);
        }

        // defaulting
        if (replyTo == null)    replyTo = addressingVersion.anonymousEpr;
        if (faultTo == null)    faultTo = replyTo;

        // Save a copy into the packet such that we can save it with that
        // packet if we're going to deliver the response at a later time
        // (async from the request).
        request.put(WsaPropertyBag.WSA_REPLYTO_FROM_REQUEST, replyTo);
        request.put(WsaPropertyBag.WSA_FAULTTO_FROM_REQUEST, faultTo);
        request.put(WsaPropertyBag.WSA_MSGID_FROM_REQUEST, msgId);

        wbo = getWSDLBoundOperation(request);
        isAnonymousRequired = isAnonymousRequired(wbo);

        Packet p = validateInboundHeaders(request);
        // if one-way message and WS-A header processing fault has occurred,
        // then do no further processing
        if (p.getMessage() == null)
            // request message is invalid, exception is logged by now  and response is sent back  with null message
            return doReturnWith(p);

        // if we find an error in addressing header, just turn around the direction here
        if (p.getMessage().isFault()) {
            // close the transportBackChannel if we know that
            // we'll never use them
            if (isEarlyBackchannelCloseAllowed &&
                !(isAnonymousRequired) &&
                    !faultTo.isAnonymous() && request.transportBackChannel != null) {
                request.transportBackChannel.close();
            }
            return processResponse(p);
        }
        // close the transportBackChannel if we know that
        // we'll never use them
        if (isEarlyBackchannelCloseAllowed &&
            !(isAnonymousRequired) &&
                !replyTo.isAnonymous() && !faultTo.isAnonymous() &&
                request.transportBackChannel != null) {
            request.transportBackChannel.close();
        }
        return doInvoke(next,p);
    }

    protected boolean isAnonymousRequired(@Nullable WSDLBoundOperation wbo) {
        //this requirement can only be specified in W3C case, Override this in W3C case.
        return false;
    }

    protected void checkAnonymousSemantics(WSDLBoundOperation wbo, WSEndpointReference replyTo, WSEndpointReference faultTo) {
        //this requirement can only be specified in W3C case, Override this in W3C case.
    }

    @Override
    public @NotNull NextAction processException(Throwable t) {
    	Packet response = Fiber.current().getPacket();
        return processResponse(response.createServerResponse(
        		SOAPFaultBuilder.createSOAPFaultMessage(soapVersion, null, t), 
        		wsdlPort, response.endpoint.getSEIModel(), binding));
    }
    
    @Override
    public @NotNull NextAction processResponse(Packet response) {
        Message msg = response.getMessage();
        if (msg ==null)
            return doReturnWith(response);  // one way message. Nothing to see here. Move on.

        String to = msg.getHeaders().getTo(addressingVersion, soapVersion);
        if (to != null) {
        	replyTo = faultTo = new WSEndpointReference(to, addressingVersion);
        }
        
        if (replyTo == null) {
            // This is an async response or we're not processing the response in
            // the same tube instance as we processed the request. Get the ReplyTo
            // now, from the properties we stored into the request packet. We
            // assume anyone that interrupted the request->response flow will have
            // saved the ReplyTo and put it back into the packet for our use.
            replyTo = (WSEndpointReference)response.
                get(WsaPropertyBag.WSA_REPLYTO_FROM_REQUEST);
        }

        if (faultTo == null) {
            // This is an async response or we're not processing the response in
            // the same tube instance as we processed the request. Get the FaultTo
            // now, from the properties we stored into the request packet. We
            // assume anyone that interrupted the request->response flow will have
            // saved the FaultTo and put it back into the packet for our use.
            faultTo = (WSEndpointReference)response.
                get(WsaPropertyBag.WSA_FAULTTO_FROM_REQUEST);
        }

        WSEndpointReference target = msg.isFault()?faultTo:replyTo;
        
        if (target == null && response.proxy instanceof Stub) {
        	target = ((Stub) response.proxy).getWSEndpointReference();
        }

        if(target.isAnonymous() || isAnonymousRequired )
            // the response will go back the back channel. most common case
            return doReturnWith(response);

        if(target.isNone()) {
            // the caller doesn't want to hear about it, so proceed like one-way
            response.setMessage(null);
            return doReturnWith(response);
        }

        // send the response to this EPR. This *can* generate a response, and
        // if so, we leave it to Fiber.CompletionCallback to deliver it to
        // whoever started this fiber. The 'new' fiber is linked into the old
        // Fiber's completion callback.
        processNonAnonymousReply(response, target);

        // then we'll proceed the rest like one-way.
        response = response.copy(false);
        return doReturnWith(response);
    }

    /**
     * Send a response to a non-anonymous address. Also closes the transport back channel
     * of {@link Packet} if it's not closed already.
     *
     * <p>
     * TODO: ideally we should be doing this by creating a new fiber.
     *
     * @param packet
     *      The response from our server, which will be delivered to the destination.
     * @param target
     *      Where do we send the packet to?
     */
    private void processNonAnonymousReply(final Packet packet, WSEndpointReference target) {
        // at this point we know we won't be sending anything back through the back channel,
        // so close it first to let the client go.
        if (packet.transportBackChannel != null)
            packet.transportBackChannel.close();

        if ((wsdlPort!=null) && packet.getMessage().isOneWay(wsdlPort)) {
            // one way message but with replyTo. I believe this is a hack for WS-TX - KK.
            LOGGER.fine(AddressingMessages.NON_ANONYMOUS_RESPONSE_ONEWAY());
            return;
        }

        EndpointAddress adrs;
        try {
             adrs = new EndpointAddress(URI.create(target.getAddress()));
        } catch (NullPointerException e) {
            throw new WebServiceException(e);
        } catch (IllegalArgumentException e) {
            throw new WebServiceException(e);
        }

        packet.endpointAddress = adrs;
        // MTU: If we're not sending a response that corresponds to a WSDL op,
        //      then take whatever soapAction is set on the packet (as allowing
        //      helper.getOutputAction() will only result in a bogus 'unset'
        //      action value.
        if (wbo != null || packet.soapAction == null) {
          String action = packet.getMessage().isFault() ?
                  helper.getFaultAction(wbo, packet) :
                  helper.getOutputAction(wbo);
          //set the SOAPAction, as its got to be same as wsa:Action
          if (packet.soapAction == null ||
              (action != null &&
               !action.equals(AddressingVersion.UNSET_OUTPUT_ACTION))) {
            packet.soapAction = action;
          }
        }
        packet.expectReply = false;

        // Link completion of the current fiber to the new fiber that will
        // deliver the async response. This allows access to the response
        // packet that may be generated by sending a new message for the
        // current async response.

        Fiber currentFiber = Fiber.current();
        final Fiber.CompletionCallback currentFiberCallback =
            currentFiber.getCompletionCallback();
        
        Fiber.CompletionCallback fiberCallback = null;
		if (currentFiberCallback != null) {
	          fiberCallback = new Fiber.CompletionCallback() {
	          public void onCompletion(@NotNull Packet response) {
	            currentFiberCallback.onCompletion(response);
	          }
	
	          public void onCompletion(@NotNull Throwable error) {
	            currentFiberCallback.onCompletion(error);
	          }
	        };
	        currentFiber.setCompletionCallback(null);
        }

        // we need to assemble a pipeline to talk to this endpoint.
        // TODO: what to pass as WSService?
        Tube transport = TransportTubeFactory.create(Thread.currentThread().getContextClassLoader(),
            new ClientTubeAssemblerContext(adrs, wsdlPort, (WSService) null, binding,endpoint.getContainer(),((BindingImpl)binding).createCodec(),null,null));
        Fiber fiber = currentFiber.owner.createFiber();
        fiber.start(transport, packet, fiberCallback);
    }

    @Override
    protected void validateAction(Packet packet) {
        //There may not be a WSDL operation.  There may not even be a WSDL.
        //For instance this may be a RM CreateSequence message.
        WSDLBoundOperation wsdlBoundOperation = getWSDLBoundOperation(packet);

        if (wsdlBoundOperation == null)
            return;

        String gotA = packet.getMessage().getHeaders().getAction(addressingVersion, soapVersion);

        if (gotA == null)
            throw new WebServiceException(AddressingMessages.VALIDATION_SERVER_NULL_ACTION());

        String expected = helper.getInputAction(packet);
        String soapAction = helper.getSOAPAction(packet);
        if (helper.isInputActionDefault(packet) && (soapAction != null && !soapAction.equals("")))
            expected = soapAction;

        if (expected != null && !gotA.equals(expected)) {
            throw new ActionNotSupportedException(gotA);
        }
    }

    @Override
    protected void checkMessageAddressingProperties(Packet packet) {
        super.checkMessageAddressingProperties(packet);

        // wsaw:Anonymous validation
        WSDLBoundOperation wsdlBoundOperation = getWSDLBoundOperation(packet);
        checkAnonymousSemantics(wsdlBoundOperation, replyTo, faultTo);
         // check if addresses are valid
        checkNonAnonymousAddresses(replyTo,faultTo);
    }

    private void checkNonAnonymousAddresses(WSEndpointReference replyTo, WSEndpointReference faultTo) {
        if (!replyTo.isAnonymous()) {
            try {
                new EndpointAddress(URI.create(replyTo.getAddress()));
            } catch (Exception e) {
                throw new InvalidAddressingHeaderException(addressingVersion.replyToTag, addressingVersion.invalidAddressTag);
            } 
        }
        //for now only validate ReplyTo
        /*
        if (!faultTo.isAnonymous()) {
            try {
                new EndpointAddress(URI.create(faultTo.getAddress()));
            } catch (IllegalArgumentException e) {
                throw new InvalidAddressingHeaderException(addressingVersion.faultToTag, addressingVersion.invalidAddressTag);
            }
        }
        */

    }
    
    /**
     * @deprecated
     *      Use {@link JAXWSProperties#ADDRESSING_MESSAGEID}.
     */
    public static final String REQUEST_MESSAGE_ID = "com.sun.xml.ws.addressing.request.messageID";

    private static final Logger LOGGER = Logger.getLogger(WsaServerTube.class.getName());
}
