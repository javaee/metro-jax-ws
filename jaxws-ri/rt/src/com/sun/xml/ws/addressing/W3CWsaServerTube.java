package com.sun.xml.ws.addressing;

import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.istack.NotNull;

/**
 * @author Rama Pulavarthi
 */
public class W3CWsaServerTube extends WsaServerTube{
    public W3CWsaServerTube(WSEndpoint endpoint, @NotNull WSDLPort wsdlPort, WSBinding binding, Tube next) {
        super(endpoint, wsdlPort, binding, next);
    }

    public W3CWsaServerTube(WsaServerTube that, TubeCloner cloner) {
        super(that, cloner);
    }

    protected Packet validateInboundHeaders(Packet packet) {
        return super.validateInboundHeaders(packet);
    }

    public W3CWsaServerTube copy(TubeCloner cloner) {
        return new W3CWsaServerTube(this, cloner);
    }

    @Override
    protected void checkMandatoryHeaders(Packet packet, boolean foundAction, boolean foundTo, boolean foundMessageId, boolean foundRelatesTo) {
        super.checkMandatoryHeaders(packet, foundAction, foundTo, foundMessageId, foundRelatesTo);  
    }
}
