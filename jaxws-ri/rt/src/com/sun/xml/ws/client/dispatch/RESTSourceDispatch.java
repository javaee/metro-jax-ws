package com.sun.xml.ws.client.dispatch;

import com.sun.xml.ws.api.addressing.WSEndpointReference;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Messages;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.binding.BindingImpl;
import com.sun.xml.ws.client.WSServiceDelegate;
import com.sun.xml.ws.encoding.xml.XMLMessage;
import com.sun.xml.ws.message.source.PayloadSourceMessage;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service.Mode;
import java.io.IOException;

/**
 * {@link Dispatch} implementation for {@link Source} and XML/HTTP binding.
 *
 * @author Kathy Walsh
 * @author Kohsuke Kawaguchi
 * @see SOAPSourceDispatch
 */
final class RESTSourceDispatch extends DispatchImpl<Source> {

    public RESTSourceDispatch(QName port, Mode mode, WSServiceDelegate owner, Tube pipe, BindingImpl binding, WSEndpointReference epr) {
        super(port, mode, owner, pipe, binding, epr);
        assert isXMLHttp(binding);
    }

    @Override
    Source toReturnValue(Packet response) {
        Message msg = response.getMessage();
        try {
            return new StreamSource(XMLMessage.getDataSource(msg).getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    Packet createPacket(Source msg) {
        Message message;

        if(msg==null)
            message = Messages.createEmpty(soapVersion);
        else
            message = new PayloadSourceMessage(null, msg, setOutboundAttachments(), soapVersion);

        return new Packet(message);
    }
}
