package com.sun.xml.ws.addressing;

import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.model.wsdl.WSDLBoundOperation;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.addressing.WSEndpointReference;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.addressing.model.MissingAddressingHeaderException;
import com.sun.xml.ws.addressing.model.InvalidAddressingHeaderException;
import static com.sun.xml.ws.addressing.W3CAddressingConstants.ONLY_NON_ANONYMOUS_ADDRESS_SUPPORTED;
import static com.sun.xml.ws.addressing.W3CAddressingConstants.ONLY_ANONYMOUS_ADDRESS_SUPPORTED;
import com.sun.xml.ws.resources.AddressingMessages;
import com.sun.istack.NotNull;
import com.sun.istack.Nullable;

import javax.xml.ws.soap.AddressingFeature;
import javax.xml.ws.WebServiceException;

/**
 * @author Rama Pulavarthi
 */
public class W3CWsaServerTube extends WsaServerTube{
    private final AddressingFeature af;

    public W3CWsaServerTube(WSEndpoint endpoint, @NotNull WSDLPort wsdlPort, WSBinding binding, Tube next) {
        super(endpoint, wsdlPort, binding, next);
        af = binding.getFeature(AddressingFeature.class);
    }

    public W3CWsaServerTube(W3CWsaServerTube that, TubeCloner cloner) {
        super(that, cloner);
        this.af = that.af;
    }

    @Override
    public W3CWsaServerTube copy(TubeCloner cloner) {
        return new W3CWsaServerTube(this, cloner);
    }

    @Override
    protected void checkMandatoryHeaders(
            Packet packet, boolean foundAction, boolean foundTo, boolean foundReplyTo,
            boolean foundFaultTo, boolean foundMessageId, boolean foundRelatesTo) {
        super.checkMandatoryHeaders(packet, foundAction, foundTo, foundReplyTo,
                foundFaultTo, foundMessageId, foundRelatesTo);

        // find Req/Response or Oneway using WSDLModel(if it is availabe)
        WSDLBoundOperation wbo = getWSDLBoundOperation(packet);
        // Taking care of protocol messages as they do not have any corresponding operations
        if (wbo != null) {
            // if two-way and no wsa:MessageID is found
            if (!wbo.getOperation().isOneWay() && !foundMessageId) {
                throw new MissingAddressingHeaderException(addressingVersion.messageIDTag,packet);
            }
        }

    }

    @Override
    protected boolean isAnonymousRequired(@Nullable WSDLBoundOperation wbo) {
        return (wbo!= null)? (getResponseRequirement(wbo) ==  AddressingFeature.Responses.ANONYMOUS):false;

    }

    private AddressingFeature.Responses getResponseRequirement(@Nullable WSDLBoundOperation wbo) {
        if (af.getResponses() == AddressingFeature.Responses.ALL && wbo != null) {
            //wsaw wsdl binding case will have some value set on wbo
            WSDLBoundOperation.ANONYMOUS anon = wbo.getAnonymous();
            if (wbo.getAnonymous() == WSDLBoundOperation.ANONYMOUS.required)
                return AddressingFeature.Responses.ANONYMOUS;
            else if (wbo.getAnonymous() == WSDLBoundOperation.ANONYMOUS.prohibited)
                return AddressingFeature.Responses.NON_ANONYMOUS;
            else
                return AddressingFeature.Responses.ALL;

        } else
            return af.getResponses();
    }

    @Override
    protected void checkAnonymousSemantics(WSDLBoundOperation wbo, WSEndpointReference replyTo, WSEndpointReference faultTo) {
        String replyToValue = null;
        String faultToValue = null;

        if (replyTo != null)
            replyToValue = replyTo.getAddress();

        if (faultTo != null)
            faultToValue = faultTo.getAddress();
        AddressingFeature.Responses responseRequirement = getResponseRequirement(wbo);

        switch (responseRequirement) {
            case NON_ANONYMOUS:
                if (replyToValue != null && replyToValue.equals(addressingVersion.anonymousUri))
                    throw new InvalidAddressingHeaderException(addressingVersion.replyToTag, ONLY_NON_ANONYMOUS_ADDRESS_SUPPORTED);

                if (faultToValue != null && faultToValue.equals(addressingVersion.anonymousUri))
                    throw new InvalidAddressingHeaderException(addressingVersion.faultToTag, ONLY_NON_ANONYMOUS_ADDRESS_SUPPORTED);
                break;
            case ANONYMOUS:
                if (replyToValue != null && !replyToValue.equals(addressingVersion.anonymousUri))
                    throw new InvalidAddressingHeaderException(addressingVersion.replyToTag, ONLY_ANONYMOUS_ADDRESS_SUPPORTED);

                if (faultToValue != null && !faultToValue.equals(addressingVersion.anonymousUri))
                    throw new InvalidAddressingHeaderException(addressingVersion.faultToTag, ONLY_ANONYMOUS_ADDRESS_SUPPORTED);
                break;
            default:
                // ALL: no check
        }
    }

}
